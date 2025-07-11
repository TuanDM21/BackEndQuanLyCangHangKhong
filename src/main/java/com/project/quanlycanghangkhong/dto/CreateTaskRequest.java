package com.project.quanlycanghangkhong.dto;

import java.util.List;

public class CreateTaskRequest {
    private String content;
    private String instructions;
    private String notes;
    private List<AssignmentRequest> assignments;
    
    // MỚI: Chỉ hỗ trợ attachment trực tiếp (thay thế hoàn toàn documents)
    private List<Integer> attachmentIds; // Gán attachment trực tiếp vào task

    // Getters and setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<AssignmentRequest> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<AssignmentRequest> assignments) {
        this.assignments = assignments;
    }

    /**
     * Lấy danh sách ID attachment để gán trực tiếp vào task
     * THAY ĐỔI LOGIC NGHIỆP VỤ: Thay thế cách tiếp cận dựa trên document bằng việc gán attachment trực tiếp
     * @return Danh sách ID attachment để gán vào task
     */
    public List<Integer> getAttachmentIds() {
        return attachmentIds;
    }

    /**
     * Đặt danh sách ID attachment để gán trực tiếp vào task
     * THAY ĐỔI LOGIC NGHIỆP VỤ: Thay thế cách tiếp cận documentIds và newDocuments cũ
     * @param attachmentIds Danh sách ID attachment hiện có để gán vào task
     */
    public void setAttachmentIds(List<Integer> attachmentIds) {
        this.attachmentIds = attachmentIds;
    }
}