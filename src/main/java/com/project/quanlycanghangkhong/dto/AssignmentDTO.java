package com.project.quanlycanghangkhong.dto;

import java.util.Date;
import com.project.quanlycanghangkhong.model.AssignmentStatus;

public class AssignmentDTO {
    private Integer assignmentId;
    private Integer taskId;
    private String recipientType; // 'team', 'unit', 'user'
    private Date assignedAt;
    private Date dueAt;
    private Date completedAt;
    private AssignmentStatus status;
    private String note;
    private UserDTO assignedByUser;
    private UserDTO completedByUser;
    private UserDTO recipientUser;
    private Integer recipientId; // Id của team hoặc unit nếu recipientType là 'team' hoặc 'unit'

    public Integer getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Integer assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }

    public Date getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Date assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Date getDueAt() {
        return dueAt;
    }

    public void setDueAt(Date dueAt) {
        this.dueAt = dueAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
        this.status = status;
    }

    public UserDTO getAssignedByUser() {
        return assignedByUser;
    }

    public void setAssignedByUser(UserDTO assignedByUser) {
        this.assignedByUser = assignedByUser;
    }

    public UserDTO getCompletedByUser() {
        return completedByUser;
    }

    public void setCompletedByUser(UserDTO completedByUser) {
        this.completedByUser = completedByUser;
    }

    public UserDTO getRecipientUser() {
        return recipientUser;
    }

    public void setRecipientUser(UserDTO recipientUser) {
        this.recipientUser = recipientUser;
    }

    public Integer getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Integer recipientId) {
        this.recipientId = recipientId;
    }
}