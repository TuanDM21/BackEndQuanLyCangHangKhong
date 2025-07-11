package com.project.quanlycanghangkhong.service.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import com.project.quanlycanghangkhong.dao.UserDAO;
import com.project.quanlycanghangkhong.dto.DTOConverter;
import com.project.quanlycanghangkhong.dto.UserDTO;
import com.project.quanlycanghangkhong.dto.response.ApiResponseCustom;
import com.project.quanlycanghangkhong.model.User;
import com.project.quanlycanghangkhong.repository.UserRepository;
import com.project.quanlycanghangkhong.service.UserService;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDAO userDAO;

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAllWithPermissions();
        return users.stream()
                .map(DTOConverter::convertUser)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Integer id, User user) {
        return userRepository.findById(id).map(existingUser -> {
            existingUser.setName(user.getName());
            existingUser.setEmail(user.getEmail());
            existingUser.setPassword(user.getPassword());
            return userRepository.save(existingUser);
        }).orElse(null);
    }

    @Override
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<UserDTO> filterUsers(Integer teamId, Integer unitId, String searchText) {
        try {
            List<User> users = userDAO.findUsersByCriteria(teamId, unitId, searchText);
            return users.stream()
                    .map(DTOConverter::convertUser)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching users", e);
        }
    }

    @Override
    public ApiResponseCustom<UserDTO> getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmailWithPermissions(email).orElseThrow(() -> new RuntimeException("User not found"));
        UserDTO dto = DTOConverter.convertUser(user); // Sử dụng DTOConverter đã được cập nhật
        return ApiResponseCustom.success(dto);
    }

    @Override
    public List<UserDTO> searchUsersByKeyword(String keyword) {
        List<User> users = userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseWithPermissions(keyword, keyword);
        return users.stream()
                .map(DTOConverter::convertUser)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getUsersByRoles(List<String> roleNames) {
        List<User> users = userRepository.findByRoleNamesWithPermissions(roleNames);
        return users.stream().map(DTOConverter::convertUser).collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getAssignableUsersForCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email).orElse(null);
        if (currentUser == null || currentUser.getRole() == null) return List.of();
        String role = currentUser.getRole().getRoleName();
        List<User> result;
        switch (role) {
            case "DIRECTOR":
            case "VICE_DIRECTOR":
                // Giao cho tất cả trừ DIRECTOR
                result = userRepository.findAll().stream()
                    .filter(u -> !u.getId().equals(currentUser.getId()))
                    .filter(u -> u.getRole() != null && !"DIRECTOR".equals(u.getRole().getRoleName()))
                    .collect(Collectors.toList());
                break;
            case "TEAM_LEAD":
                // Giao cho tất cả user thuộc team
                if (currentUser.getTeam() == null) return List.of();
                Integer teamId = currentUser.getTeam().getId();
                result = userRepository.findAll().stream()
                    .filter(u -> !u.getId().equals(currentUser.getId()))
                    .filter(u -> u.getTeam() != null && u.getTeam().getId().equals(teamId))
                    .collect(Collectors.toList());
                break;
            case "TEAM_VICE_LEAD":
                // Không giao cho TEAM_LEAD, chỉ giao cho VICE_TEAM_LEAD và các user khác cùng team
                if (currentUser.getTeam() == null) return List.of();
                Integer teamIdVice = currentUser.getTeam().getId();
                System.out.println("[DEBUG] VICE_TEAM_LEAD: teamId=" + teamIdVice);
                result = userRepository.findAll().stream()
                    .filter(u -> !u.getId().equals(currentUser.getId()))
                    .filter(u -> u.getTeam() != null && u.getTeam().getId().equals(teamIdVice))
                    .filter(u -> {
                        String r = u.getRole() != null ? u.getRole().getRoleName() : "";
                        boolean ok = !"TEAM_LEAD".equals(r);
                        if (ok) System.out.println("[DEBUG] VICE_TEAM_LEAD có thể giao cho: " + u.getName() + " (role=" + r + ", team=" + (u.getTeam()!=null?u.getTeam().getId():"-") + ", unit=" + (u.getUnit()!=null?u.getUnit().getId():"-") + ")");
                        return ok;
                    })
                    .collect(Collectors.toList());
                System.out.println("[DEBUG] VICE_TEAM_LEAD tổng số user có thể giao: " + result.size());
                break;
            case "UNIT_LEAD":
                // Giao cho user cùng team và cùng unit, bao gồm cả VICE_UNIT_LEAD
                if (currentUser.getTeam() == null || currentUser.getUnit() == null) return List.of();
                Integer tId = currentUser.getTeam().getId();
                Integer uId = currentUser.getUnit().getId();
                result = userRepository.findAll().stream()
                    .filter(u -> !u.getId().equals(currentUser.getId()))
                    .filter(u -> u.getTeam() != null && u.getTeam().getId().equals(tId))
                    .filter(u -> u.getUnit() != null && u.getUnit().getId().equals(uId))
                    .filter(u -> {
                        String r = u.getRole() != null ? u.getRole().getRoleName() : "";
                        return "VICE_UNIT_LEAD".equals(r) || "UNIT_LEAD".equals(r) || "MEMBER".equals(r) || "OFFICE".equals(r);
                    })
                    .collect(Collectors.toList());
                break;
            case "UNIT_VICE_LEAD":
                // Giao cho user cùng team và cùng unit, chỉ cho VICE_UNIT_LEAD, MEMBER, OFFICE (không cho UNIT_LEAD)
                if (currentUser.getTeam() == null || currentUser.getUnit() == null) return List.of();
                Integer tIdV = currentUser.getTeam().getId();
                Integer uIdV = currentUser.getUnit().getId();
                System.out.println("[DEBUG] VICE_UNIT_LEAD: teamId=" + tIdV + ", unitId=" + uIdV);
                result = userRepository.findAll().stream()
                    .filter(u -> !u.getId().equals(currentUser.getId()))
                    .filter(u -> u.getTeam() != null && u.getTeam().getId().equals(tIdV))
                    .filter(u -> u.getUnit() != null && u.getUnit().getId().equals(uIdV))
                    .filter(u -> {
                        String r = u.getRole() != null ? u.getRole().getRoleName() : "";
                        boolean ok = "VICE_UNIT_LEAD".equals(r) || "MEMBER".equals(r) || "OFFICE".equals(r);
                        if (ok) System.out.println("[DEBUG] VICE_UNIT_LEAD có thể giao cho: " + u.getName() + " (role=" + r + ", team=" + (u.getTeam()!=null?u.getTeam().getId():"-") + ", unit=" + (u.getUnit()!=null?u.getUnit().getId():"-") + ")");
                        return ok;
                    })
                    .collect(Collectors.toList());
                System.out.println("[DEBUG] VICE_UNIT_LEAD tổng số user có thể giao: " + result.size());
                break;
            case "MEMBER":
            case "OFFICE":
                // Giao cho user cùng team và cùng unit, chỉ cho MEMBER, OFFICE
                if (currentUser.getTeam() == null || currentUser.getUnit() == null) return List.of();
                Integer teamIdM = currentUser.getTeam().getId();
                Integer unitIdM = currentUser.getUnit().getId();
                result = userRepository.findAll().stream()
                    .filter(u -> !u.getId().equals(currentUser.getId()))
                    .filter(u -> u.getTeam() != null && u.getTeam().getId().equals(teamIdM))
                    .filter(u -> u.getUnit() != null && u.getUnit().getId().equals(unitIdM))
                    .filter(u -> {
                        String r = u.getRole() != null ? u.getRole().getRoleName() : "";
                        return "MEMBER".equals(r) || "OFFICE".equals(r);
                    })
                    .collect(Collectors.toList());
                break;
            default:
                result = List.of();
        }
        return result.stream().map(DTOConverter::convertUser).collect(Collectors.toList());
    }
}
