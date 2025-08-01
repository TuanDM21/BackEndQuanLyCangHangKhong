package com.project.quanlycanghangkhong.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.quanlycanghangkhong.dto.TeamDTO;
import com.project.quanlycanghangkhong.model.Team;
import com.project.quanlycanghangkhong.service.TeamService;
import com.project.quanlycanghangkhong.dto.ApiResponse;
import com.project.quanlycanghangkhong.dto.response.teams.ApiAllTeamsResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/teams")
@Tag(name = "Team Management", description = "APIs for managing teams")
public class TeamController {

	@Autowired
	private TeamService teamService;

	@GetMapping
	@Operation(summary = "Get all teams", description = "Retrieve a list of all teams")
	@ApiResponses(value = {
	    @io.swagger.v3.oas.annotations.responses.ApiResponse(
	        responseCode = "200",
	        description = "Successfully retrieved all teams",
	        content = @Content(schema = @Schema(implementation = ApiAllTeamsResponse.class))
	    )
	})
	public ResponseEntity<ApiAllTeamsResponse> getAllTeams() {
		List<TeamDTO> dtos = teamService.getAllTeams();
		ApiAllTeamsResponse response = new ApiAllTeamsResponse();
		response.setMessage("Thành công");
		response.setStatusCode(200);
		response.setData(dtos);
		response.setSuccess(true);
		return ResponseEntity.ok(response);
	}

	@PostMapping
	@Operation(summary = "Create a new team", description = "Create a new team with the provided details")
	@ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team created successfully", content = @Content(schema = @Schema(implementation = TeamDTO.class))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
	})
	public ResponseEntity<ApiResponse<TeamDTO>> createTeam(@RequestBody TeamDTO teamDTO) {
		Team team = new Team();
		team.setTeamName(teamDTO.getTeamName());
		Team createdTeam = teamService.createTeam(team);
		TeamDTO dto = new TeamDTO(createdTeam.getId(), createdTeam.getTeamName());
		return ResponseEntity.ok(new ApiResponse<>("Thành công", 200, dto, true));
	}

	@GetMapping("/assignable")
	@Operation(summary = "Get assignable teams", description = "Lấy danh sách team mà user hiện tại có thể giao việc cho theo phân quyền")
	public ResponseEntity<ApiAllTeamsResponse> getAssignableTeams() {
		List<TeamDTO> dtos = teamService.getAssignableTeamsForCurrentUser();
		ApiAllTeamsResponse response = new ApiAllTeamsResponse();
		response.setMessage("Thành công");
		response.setStatusCode(200);
		response.setData(dtos);
		response.setSuccess(true);
		return ResponseEntity.ok(response);
	}
}
