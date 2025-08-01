package com.project.quanlycanghangkhong.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import com.project.quanlycanghangkhong.dto.DTOConverter;
import com.project.quanlycanghangkhong.dto.UnitDTO;
import com.project.quanlycanghangkhong.model.Unit;
import com.project.quanlycanghangkhong.model.User;
import com.project.quanlycanghangkhong.repository.UnitRepository;
import com.project.quanlycanghangkhong.repository.UserRepository;
import com.project.quanlycanghangkhong.service.UnitService;

@Service
public class UnitServiceImpl implements UnitService {
    private final UnitRepository unitRepository;
    private final UserRepository userRepository;

    public UnitServiceImpl(UnitRepository unitRepository, UserRepository userRepository) {
        this.unitRepository = unitRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<UnitDTO> getUnitsByTeam(Integer teamId) {
        List<Unit> units = unitRepository.findByTeam_Id(teamId);
        // Hoặc custom query: findAllByTeamId(teamId)

        return units.stream()
                .map(DTOConverter::convertUnit)
                .collect(Collectors.toList());
    }

    @Override
    public Unit getUnitById(Integer id) {
        return unitRepository.findById(id).orElseThrow(() -> new RuntimeException("Unit not found"));
    }

    @Override
    public Unit createUnit(Unit unit) {
        return unitRepository.save(unit);
    }

    @Override
    public void deleteUnit(Integer id) {
        unitRepository.deleteById(id);
    }

    @Override
    public List<UnitDTO> getAllUnits() {
        List<Unit> units = unitRepository.findAll();
        return units.stream()
                .map(DTOConverter::convertUnit)
                .collect(Collectors.toList());
    }

    @Override
    public List<UnitDTO> getAssignableUnitsForCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email).orElse(null);
        if (currentUser == null || currentUser.getRole() == null) return List.of();
        
        String role = currentUser.getRole().getRoleName();
        List<Unit> assignableUnits;
        
        switch (role) {
            case "DIRECTOR":
            case "VICE_DIRECTOR":
                // Có thể giao cho tất cả units
                assignableUnits = unitRepository.findAll();
                break;
            case "TEAM_LEAD":
            case "TEAM_VICE_LEAD":
                // Có thể giao cho tất cả units trong team của mình
                if (currentUser.getTeam() != null) {
                    assignableUnits = unitRepository.findByTeam_Id(currentUser.getTeam().getId());
                } else {
                    assignableUnits = List.of();
                }
                break;
            case "UNIT_LEAD":
            case "UNIT_VICE_LEAD":
            case "MEMBER":
            case "OFFICE":
                // Chỉ có thể giao cho unit của mình
                if (currentUser.getUnit() != null) {
                    assignableUnits = List.of(currentUser.getUnit());
                } else {
                    assignableUnits = List.of();
                }
                break;
            default:
                assignableUnits = List.of();
        }
        
        return assignableUnits.stream()
                .map(DTOConverter::convertUnit)
                .collect(Collectors.toList());
    }
}
