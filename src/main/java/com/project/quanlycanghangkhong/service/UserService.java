package com.project.quanlycanghangkhong.service;

import java.util.List;
import java.util.Optional;

import com.project.quanlycanghangkhong.dto.UserDTO;
import com.project.quanlycanghangkhong.dto.response.ApiResponseCustom;
import com.project.quanlycanghangkhong.model.User;

public interface UserService {
    List<UserDTO> getAllUsers();

    Optional<User> getUserById(Integer id);

    User createUser(User user);

    User updateUser(Integer id, User user);

    void deleteUser(Integer id);

    List<UserDTO> filterUsers(Integer teamId, Integer unitId, String searchText);

    ApiResponseCustom<UserDTO> getCurrentUser();

    List<UserDTO> searchUsersByKeyword(String keyword);

    List<UserDTO> getUsersByRoles(List<String> roleNames);

    /**
     * Lấy danh sách user mà user hiện tại có thể giao việc cho (theo phân quyền).
     */
    List<UserDTO> getAssignableUsersForCurrentUser();
}
