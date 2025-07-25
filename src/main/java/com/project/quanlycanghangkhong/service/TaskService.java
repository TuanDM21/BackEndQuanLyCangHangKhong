package com.project.quanlycanghangkhong.service;

import com.project.quanlycanghangkhong.dto.CreateTaskRequest;
import com.project.quanlycanghangkhong.dto.CreateSubtaskRequest;
import com.project.quanlycanghangkhong.dto.TaskDTO;
import com.project.quanlycanghangkhong.dto.TaskDetailDTO;
import com.project.quanlycanghangkhong.dto.UpdateTaskDTO;
import com.project.quanlycanghangkhong.dto.AttachmentDTO;
import java.util.List;

public interface TaskService {
    /**
     * Tạo task với assignment và attachment trực tiếp
     * THAY ĐỔI LOGIC NGHIỆP VỤ: Thay thế cách tiếp cận dựa trên document bằng việc gán attachment trực tiếp
     * @param request Yêu cầu tạo task với danh sách ID attachment
     * @return DTO task đã tạo
     */
    TaskDTO createTaskWithAssignmentsAndAttachments(CreateTaskRequest request);
    TaskDTO createTask(TaskDTO taskDTO);
    TaskDTO updateTask(Integer id, UpdateTaskDTO updateTaskDTO);
    void deleteTask(Integer id);
    void bulkDeleteTasks(List<Integer> taskIds);
    TaskDTO getTaskById(Integer id);
    List<TaskDTO> getAllTasks();
    TaskDetailDTO getTaskDetailById(Integer id);
    List<TaskDetailDTO> getAllTaskDetails();
    void updateTaskStatus(com.project.quanlycanghangkhong.model.Task task);
    
    // Method mới để lấy task theo loại
    List<TaskDetailDTO> getMyTasks(String type);
    
    // MÔ HÌNH ADJACENCY LIST: Các method subtask cho cấu trúc phân cấp
    
    /**
     * Tạo subtask dưới một task cha trong mô hình Adjacency List
     * @param parentId ID task cha (từ path parameter)
     * @param request Yêu cầu tạo subtask (không chứa parentId)
     * @return DTO subtask đã tạo
     */
    TaskDTO createSubtask(Integer parentId, CreateSubtaskRequest request);
    
    /**
     * Lấy tất cả subtask (task con) của một task cha trong mô hình Adjacency List
     * @param parentId ID task cha
     * @return Danh sách task con
    /**
     * Lấy tất cả subtask (task con) của một task cha trong mô hình Adjacency List
     * @param parentId ID task cha
     * @return Danh sách task con
     */
    List<TaskDetailDTO> getSubtasks(Integer parentId);
    
    /**
     * Lấy tất cả task gốc (task không có cha) trong mô hình Adjacency List
     * @return Danh sách task gốc
     */
    List<TaskDetailDTO> getRootTasks();
    
    // === ATTACHMENT MANAGEMENT ===
    // Attachment chỉ được quản lý thông qua createTask và updateTask
    // Đã loại bỏ assignAttachmentsToTask và removeAttachmentsFromTask
    
    /**
     * Lấy tất cả attachment được liên kết trực tiếp với một task
     * THAY ĐỔI LOGIC NGHIỆP VỤ: Truy vấn quan hệ task-attachment trực tiếp
     * @param taskId ID Task
     * @return Danh sách attachment được liên kết trực tiếp với task
     */
    List<AttachmentDTO> getTaskAttachments(Integer taskId);
    
    // ============== SEARCH & FILTER METHODS ==============
    
    /**
     * Tìm kiếm task theo title
     * @param title Từ khóa tìm kiếm
     * @return Danh sách task match
     */
    List<TaskDetailDTO> searchTasksByTitle(String title);
    
    /**
     * Lọc task theo priority
     * @param priority Priority level
     * @return Danh sách task có priority cụ thể
     */
    List<TaskDetailDTO> getTasksByPriority(com.project.quanlycanghangkhong.model.TaskPriority priority);
    
    /**
     * Tìm kiếm task theo title hoặc content
     * @param keyword Từ khóa tìm kiếm
     * @return Danh sách task match
     */
    List<TaskDetailDTO> searchTasks(String keyword);
}