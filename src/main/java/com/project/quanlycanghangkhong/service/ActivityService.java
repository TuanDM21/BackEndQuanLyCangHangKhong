package com.project.quanlycanghangkhong.service;

import com.project.quanlycanghangkhong.dto.ActivityDTO;
import com.project.quanlycanghangkhong.dto.ActivityParticipantDTO;

import java.util.List;

public interface ActivityService {
    ActivityDTO createActivity(ActivityDTO dto);
    ActivityDTO updateActivity(Long id, ActivityDTO dto);
    void deleteActivity(Long id);
    ActivityDTO getActivity(Long id);
    List<ActivityDTO> searchActivities(String name, String location);
    List<ActivityDTO> getAllActivities();
    List<ActivityDTO> searchActivitiesByMonthYear(int month, int year);
    // Participants
    List<ActivityParticipantDTO> addParticipants(Long activityId, List<ActivityParticipantDTO> participants);
    void removeParticipant(Long activityId, String participantType, Long participantId);
    List<ActivityDTO> getActivitiesForUser(Integer userId);
}
