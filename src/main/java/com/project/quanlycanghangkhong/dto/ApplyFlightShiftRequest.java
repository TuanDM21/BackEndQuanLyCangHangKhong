package com.project.quanlycanghangkhong.dto;

import java.util.List;

public class ApplyFlightShiftRequest {
    private Long flightId; // Nếu flightId của Flight được định nghĩa là Long
    private List<Integer> userIds; // Sử dụng Integer cho User id

    public Long getFlightId() {
        return flightId;
    }
    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }
    public List<Integer> getUserIds() {
        return userIds;
    }
    public void setUserIds(List<Integer> userIds) {
        this.userIds = userIds;
    }
}
