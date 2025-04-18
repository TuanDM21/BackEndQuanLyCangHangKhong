package com.project.quanlycanghangkhong.dto;

import com.project.quanlycanghangkhong.model.User;

public class UserDTO {
	private Integer id;
	private String name;
	private String email;
	private String roleName;
	private String teamName;
	private String unitName;
	private Integer roleId;
	private Integer teamId;
	private Integer unitId;

	public UserDTO(User user) {
		// Handle basic properties
		this.id = user.getId();
		this.name = user.getName();
		this.email = user.getEmail();

		// Initialize all properties that might be null to avoid NPEs later
		this.roleName = null;
		this.teamName = null;
		this.unitName = null;
		this.roleId = null;
		this.teamId = null;
		this.unitId = null;

		// Safely access nested properties
		if (user.getRole() != null) {
			this.roleName = user.getRole().getRoleName();
			this.roleId = user.getRole().getId();
		}

		if (user.getTeam() != null) {
			this.teamName = user.getTeam().getTeamName();
			this.teamId = user.getTeam().getId();
		}

		if (user.getUnit() != null) {
			this.unitName = user.getUnit().getUnitName();
			this.unitId = user.getUnit().getId();
		}
	}

	public UserDTO(Integer id, String name, String email, Integer roleId, Integer teamId, Integer unitId) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.roleId = roleId;
		this.teamId = teamId;
		this.unitId = unitId;
		// Initialize other properties to null
		this.roleName = null;
		this.teamName = null;
		this.unitName = null;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public Integer getTeamId() {
		return teamId;
	}

	public void setTeamId(Integer teamId) {
		this.teamId = teamId;
	}

	public Integer getUnitId() {
		return unitId;
	}

	public void setUnitId(Integer unitId) {
		this.unitId = unitId;
	}

	// Getter & Setter
}
