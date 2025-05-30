package com.project.quanlycanghangkhong.dto;

import java.util.Date;

public class CreateAssignmentRequest {
    private String recipientType; // 'team', 'unit', 'user'
    private Integer recipientId; // Id của team, unit hoặc user
    private Date dueAt;
    private String note;
    // Đã xoá taskId, chỉ nhận taskId ở object cha

    public String getRecipientType() { return recipientType; }
    public void setRecipientType(String recipientType) { this.recipientType = recipientType; }
    public Integer getRecipientId() { return recipientId; }
    public void setRecipientId(Integer recipientId) { this.recipientId = recipientId; }
    public Date getDueAt() { return dueAt; }
    public void setDueAt(Date dueAt) { this.dueAt = dueAt; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}