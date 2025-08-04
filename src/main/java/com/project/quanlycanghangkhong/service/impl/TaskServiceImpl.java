package com.project.quanlycanghangkhong.service.impl;

import com.project.quanlycanghangkhong.model.Task;
import com.project.quanlycanghangkhong.model.User;
import com.project.quanlycanghangkhong.repository.TaskRepository;
import com.project.quanlycanghangkhong.repository.UserRepository;
import com.project.quanlycanghangkhong.service.TaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.project.quanlycanghangkhong.dto.*;
import com.project.quanlycanghangkhong.model.*;
import com.project.quanlycanghangkhong.repository.*;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setContent(task.getContent());
        dto.setInstructions(task.getInstructions());
        dto.setNotes(task.getNotes());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        dto.setCreatedBy(task.getCreatedBy() != null ? task.getCreatedBy().getId() : null);
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        return dto;
    }

    private Task convertToEntity(TaskDTO dto) {
        Task task = new Task();
        task.setId(dto.getId());
        task.setTitle(dto.getTitle());
        task.setContent(dto.getContent());
        task.setInstructions(dto.getInstructions());
        task.setNotes(dto.getNotes());
        task.setCreatedAt(dto.getCreatedAt());
        task.setUpdatedAt(dto.getUpdatedAt());
        task.setStatus(dto.getStatus());
        task.setPriority(dto.getPriority());
        if (dto.getCreatedBy() != null) {
            Optional<User> userOpt = userRepository.findById(dto.getCreatedBy());
            userOpt.ifPresent(task::setCreatedBy);
        } else {
            task.setCreatedBy(null);
        }
        return task;
    }

    @Override
    public TaskDTO createTask(TaskDTO taskDTO) {
        Task task = convertToEntity(taskDTO);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);
        return convertToDTO(saved);
    }

    @Transactional
    @Override
    /**
     * Tạo task với assignment và attachment trực tiếp
     * THAY ĐỔI LOGIC NGHIỆP VỤ: Thay thế hoàn toàn logic dựa trên document bằng attachment trực tiếp
     */
    public TaskDTO createTaskWithAssignmentsAndAttachments(CreateTaskRequest request) {
        // Lấy user hiện tại từ SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication != null ? authentication.getName() : null;
        User creator = (email != null) ? userRepository.findByEmail(email).orElse(null) : null;

        // Tạo Task
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setContent(request.getContent());
        task.setInstructions(request.getInstructions());
        task.setNotes(request.getNotes());
        task.setPriority(request.getPriority() != null ? request.getPriority() : com.project.quanlycanghangkhong.model.TaskPriority.NORMAL);
        task.setStatus(com.project.quanlycanghangkhong.model.TaskStatus.OPEN); // ✅ Đảm bảo status được set
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        if (creator != null) task.setCreatedBy(creator);
        Task savedTask = taskRepository.save(task);

        // MỚI: Gán attachment trực tiếp vào task (THAY THẾ hoàn toàn logic document)
        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            List<Attachment> attachments = attachmentRepository.findAllByIdIn(request.getAttachmentIds());
            for (Attachment attachment : attachments) {
                if (!attachment.isDeleted()) {
                    attachment.setTask(savedTask);
                }
            }
            attachmentRepository.saveAll(attachments);
        }

        // Tạo Assignment
        if (request.getAssignments() != null) {
            for (AssignmentRequest a : request.getAssignments()) {
                Assignment assignment = new Assignment();
                assignment.setTask(savedTask);
                assignment.setRecipientType(a.getRecipientType());
                assignment.setRecipientId(a.getRecipientId());
                assignment.setNote(a.getNote());
                assignment.setAssignedAt(LocalDateTime.now());
                assignment.setAssignedBy(creator); // Đảm bảo luôn set người giao việc
                assignment.setStatus(AssignmentStatus.WORKING);
                if (a.getDueAt() != null) {
                    assignment.setDueAt(new java.sql.Timestamp(a.getDueAt().getTime()).toLocalDateTime());
                }
                assignmentRepository.save(assignment);
            }
            // ✅ FIX: Cập nhật trạng thái task sau khi tạo assignments
            updateTaskStatus(savedTask);
        }

        return convertToDTO(savedTask);
    }

    @Override
    @Transactional
    public TaskDTO updateTask(Integer id, UpdateTaskDTO updateTaskDTO) {
        Optional<Task> optionalTask = taskRepository.findById(id);
        if (optionalTask.isPresent()) {
            Task task = optionalTask.get();
            
            // Cập nhật thông tin cơ bản
            if (updateTaskDTO.getTitle() != null) {
                task.setTitle(updateTaskDTO.getTitle());
            }
            task.setContent(updateTaskDTO.getContent());
            task.setInstructions(updateTaskDTO.getInstructions());
            task.setNotes(updateTaskDTO.getNotes());
            if (updateTaskDTO.getPriority() != null) {
                task.setPriority(updateTaskDTO.getPriority());
            }
            task.setUpdatedAt(LocalDateTime.now());
            
            // MỚI: Cập nhật attachment list
            if (updateTaskDTO.getAttachmentIds() != null) {
                updateTaskAttachments(task, updateTaskDTO.getAttachmentIds());
            }
            // null = không thay đổi attachment, chỉ cập nhật nội dung
            
            Task updated = taskRepository.save(task);
            return convertToDTO(updated);
        }
        return null;
    }

    @Override
    public void deleteTask(Integer id) {
        Optional<Task> optionalTask = taskRepository.findById(id);
        if (optionalTask.isPresent()) {
            Task task = optionalTask.get();
            task.setDeleted(true);
            taskRepository.save(task);
        }
    }

    @Override
    @Transactional
    public void bulkDeleteTasks(List<Integer> taskIds) {
        for (Integer taskId : taskIds) {
            Optional<Task> optionalTask = taskRepository.findById(taskId);
            if (optionalTask.isPresent()) {
                Task task = optionalTask.get();
                task.setDeleted(true);
                taskRepository.save(task);
            }
        }
    }

    @Override
    public TaskDTO getTaskById(Integer id) {
        return taskRepository.findByIdAndDeletedFalse(id).map(this::convertToDTO).orElse(null);
    }

    @Override
    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAllByDeletedFalse().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public TaskDetailDTO getTaskDetailById(Integer id) {
        Task task = taskRepository.findByIdAndDeletedFalse(id).orElse(null);
        if (task == null) return null;
        TaskDetailDTO dto = new TaskDetailDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setContent(task.getContent());
        dto.setInstructions(task.getInstructions());
        dto.setNotes(task.getNotes());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        dto.setStatus(task.getStatus()); // Mapping status enum
        dto.setPriority(task.getPriority());
        
        // NEW: Set parent ID if exists
        if (task.getParent() != null) {
            dto.setParentId(task.getParent().getId());
        }
        
        if (task.getCreatedBy() != null) {
            dto.setCreatedByUser(new UserDTO(task.getCreatedBy()));
        }
        
        // Assignments
        List<AssignmentDTO> assignmentDTOs = assignmentRepository.findAll().stream()
            .filter(a -> a.getTask().getId().equals(task.getId()))
            .map(a -> {
                AssignmentDTO adto = new AssignmentDTO();
                adto.setAssignmentId(a.getAssignmentId());
                adto.setRecipientType(a.getRecipientType());
                adto.setRecipientId(a.getRecipientId());
                adto.setTaskId(a.getTask() != null ? a.getTask().getId() : null);
                if (a.getAssignedBy() != null) {
                    adto.setAssignedByUser(new UserDTO(a.getAssignedBy()));
                }
                adto.setAssignedAt(a.getAssignedAt() != null ? java.sql.Timestamp.valueOf(a.getAssignedAt()) : null);
                adto.setDueAt(a.getDueAt() != null ? java.sql.Timestamp.valueOf(a.getDueAt()) : null);
                adto.setNote(a.getNote());
                adto.setCompletedAt(a.getCompletedAt() != null ? java.sql.Timestamp.valueOf(a.getCompletedAt()) : null);
                if (a.getCompletedBy() != null) {
                    adto.setCompletedByUser(new UserDTO(a.getCompletedBy()));
                }
                adto.setStatus(a.getStatus());
                // recipientUser: user, team, unit
                if ("user".equalsIgnoreCase(a.getRecipientType()) && a.getRecipientId() != null) {
                    userRepository.findById(a.getRecipientId()).ifPresent(u -> adto.setRecipientUser(new UserDTO(u)));
                } else if ("team".equalsIgnoreCase(a.getRecipientType()) && a.getRecipientId() != null) {
                    userRepository.findTeamLeadByTeamId(a.getRecipientId()).ifPresent(u -> adto.setRecipientUser(new UserDTO(u)));
                } else if ("unit".equalsIgnoreCase(a.getRecipientType()) && a.getRecipientId() != null) {
                    userRepository.findUnitLeadByUnitId(a.getRecipientId()).ifPresent(u -> adto.setRecipientUser(new UserDTO(u)));
                }
                return adto;
            }).toList();
        dto.setAssignments(assignmentDTOs);
        
        // NEW: Direct attachments
        List<AttachmentDTO> attachmentDTOs = new ArrayList<>();
        List<Attachment> directAttachments = attachmentRepository.findByTask_IdAndIsDeletedFalse(task.getId());
        for (Attachment att : directAttachments) {
            AttachmentDTO attDto = new AttachmentDTO();
            attDto.setId(att.getId());
            attDto.setFilePath(att.getFilePath());
            attDto.setFileName(att.getFileName());
            attDto.setFileSize(att.getFileSize());
            attDto.setCreatedAt(att.getCreatedAt());
            if (att.getUploadedBy() != null) {
                attDto.setUploadedBy(new UserDTO(att.getUploadedBy()));
            }
            attachmentDTOs.add(attDto);
        }
        dto.setAttachments(attachmentDTOs);
        
        // NEW: Subtasks
        List<TaskDetailDTO> subtaskDTOs = new ArrayList<>();
        List<Task> subtasks = taskRepository.findByParentIdAndDeletedFalse(task.getId());
        for (Task subtask : subtasks) {
            TaskDetailDTO subtaskDto = getTaskDetailById(subtask.getId()); // Recursive call
            if (subtaskDto != null) {
                subtaskDTOs.add(subtaskDto);
            }
        }
        dto.setSubtasks(subtaskDTOs);
        
        return dto;
    }

    @Override
    public List<TaskDetailDTO> getAllTaskDetails() {
        return taskRepository.findAllByDeletedFalse().stream()
            .map(task -> getTaskDetailById(task.getId()))
            .toList();
    }

    @Override
    public List<TaskDetailDTO> getMyTasks(String type) {
        // Lấy user hiện tại từ SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication != null ? authentication.getName() : null;
        User currentUser = (email != null) ? userRepository.findByEmail(email).orElse(null) : null;
        
        if (currentUser == null) {
            return List.of();
        }
        
        Integer currentUserId = currentUser.getId();
        
        switch (type.toLowerCase()) {
            case "created":
                // 🚀 OPTIMIZED: Lấy tasks đã tạo nhưng chưa có assignment (logic cũ)
                List<Task> createdTasks = taskRepository.findCreatedTasksWithoutAssignments(currentUserId);
                
                // ✅ Trả về flat list, KHÔNG cần hierarchy level
                return createdTasks.stream()
                    .map(task -> getTaskDetailById(task.getId()))
                    .collect(Collectors.toList());
                
            case "assigned":
                // 🚀 OPTIMIZED + 🌲 HIERARCHICAL: Lấy tasks đã giao + tất cả subtasks
                List<Task> assignedTasks = taskRepository.findAssignedTasksByUserId(currentUserId);
                
                // 🌳 Special handling for assigned: return hierarchy with levels
                return getTaskHierarchyWithLevels(assignedTasks);
                
            case "received":
                // 🚀 OPTIMIZED + 🌲 HIERARCHICAL: Lấy tasks được giao + hierarchy levels
                List<Task> receivedTasks = new ArrayList<>();
                
                // 1. Tasks được giao trực tiếp cho user
                receivedTasks.addAll(taskRepository.findReceivedTasksByUserId(currentUserId));
                
                // 2. Tasks được giao cho team (chỉ TEAM_LEAD mới nhận)
                if (currentUser.getRole() != null && 
                    "TEAM_LEAD".equals(currentUser.getRole().getRoleName()) &&
                    currentUser.getTeam() != null) {
                    receivedTasks.addAll(taskRepository.findReceivedTasksByTeamId(currentUser.getTeam().getId()));
                }
                
                // 3. Tasks được giao cho unit (chỉ UNIT_LEAD mới nhận)
                if (currentUser.getRole() != null && 
                    "UNIT_LEAD".equals(currentUser.getRole().getRoleName()) &&
                    currentUser.getUnit() != null) {
                    receivedTasks.addAll(taskRepository.findReceivedTasksByUnitId(currentUser.getUnit().getId()));
                }
                
                // Remove duplicates và giữ nguyên sort order
                List<Task> uniqueReceivedTasks = receivedTasks.stream()
                    .distinct()
                    .sorted((t1, t2) -> {
                        // Sort theo updatedAt DESC, sau đó createdAt DESC
                        int updatedCompare = t2.getUpdatedAt().compareTo(t1.getUpdatedAt());
                        if (updatedCompare != 0) return updatedCompare;
                        return t2.getCreatedAt().compareTo(t1.getCreatedAt());
                    })
                    .collect(Collectors.toList());
                
                // 🌳 Special handling for received: return hierarchy with levels
                return getTaskHierarchyWithLevels(uniqueReceivedTasks);
                
            default:
                return List.of();
        }
    }

    // ✅ LOGIC MỚI - ĐƠN GIẢN: Cập nhật trạng thái Task dựa trên trạng thái các Assignment con
    public void updateTaskStatus(Task task) {
        List<Assignment> assignments = assignmentRepository.findAll().stream()
            .filter(a -> a.getTask().getId().equals(task.getId()))
            .collect(Collectors.toList());
            
        // Không có assignment nào → OPEN
        if (assignments == null || assignments.isEmpty()) {
            task.setStatus(TaskStatus.OPEN);
            taskRepository.save(task);
            return;
        }
        
        // Tất cả assignments đều DONE → COMPLETED  
        boolean allDone = assignments.stream()
                .allMatch(a -> a.getStatus() == AssignmentStatus.DONE);
                
        // Có ít nhất 1 assignment WORKING → IN_PROGRESS
        boolean anyWorking = assignments.stream()
                .anyMatch(a -> a.getStatus() == AssignmentStatus.WORKING);
        
        if (allDone) {
            task.setStatus(TaskStatus.COMPLETED);
        } else if (anyWorking) {
            task.setStatus(TaskStatus.IN_PROGRESS);
        } else {
            // Tất cả assignments đều CANCELLED → OPEN (task có thể assign lại)
            task.setStatus(TaskStatus.OPEN);
        }
        
        taskRepository.save(task);
    }

    // MÔ HÌNH ADJACENCY LIST: Triển khai các method subtask
    @Override
    @Transactional
    /**
     * Tạo subtask trong mô hình Adjacency List
     * MÔ HÌNH ADJACENCY LIST: Tạo task con với parent_id tham chiếu
     */
    public TaskDTO createSubtask(Integer parentId, CreateSubtaskRequest request) {
        // Lấy task cha
        Task parentTask = taskRepository.findByIdAndDeletedFalse(parentId).orElse(null);
        if (parentTask == null) {
            throw new RuntimeException("Không tìm thấy task cha: " + parentId);
        }

        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication != null ? authentication.getName() : null;
        User creator = (email != null) ? userRepository.findByEmail(email).orElse(null) : null;

        // Create subtask
        Task subtask = new Task();
        subtask.setTitle(request.getTitle());
        subtask.setContent(request.getContent());
        subtask.setInstructions(request.getInstructions());
        subtask.setNotes(request.getNotes());
        subtask.setPriority(request.getPriority() != null ? request.getPriority() : com.project.quanlycanghangkhong.model.TaskPriority.NORMAL);
        subtask.setParent(parentTask);
        subtask.setCreatedAt(LocalDateTime.now());
        subtask.setUpdatedAt(LocalDateTime.now());
        if (creator != null) subtask.setCreatedBy(creator);
        Task savedSubtask = taskRepository.save(subtask);

        // Assign direct attachments if provided
        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            List<Attachment> attachments = attachmentRepository.findAllByIdIn(request.getAttachmentIds());
            for (Attachment attachment : attachments) {
                if (!attachment.isDeleted()) {
                    attachment.setTask(savedSubtask);
                }
            }
            attachmentRepository.saveAll(attachments);
        }

        // Create assignments for subtask
        if (request.getAssignments() != null) {
            for (AssignmentRequest a : request.getAssignments()) {
                Assignment assignment = new Assignment();
                assignment.setTask(savedSubtask);
                assignment.setRecipientType(a.getRecipientType());
                assignment.setRecipientId(a.getRecipientId());
                assignment.setNote(a.getNote());
                assignment.setAssignedAt(LocalDateTime.now());
                assignment.setAssignedBy(creator);
                assignment.setStatus(AssignmentStatus.WORKING);
                if (a.getDueAt() != null) {
                    assignment.setDueAt(new java.sql.Timestamp(a.getDueAt().getTime()).toLocalDateTime());
                }
                assignmentRepository.save(assignment);
            }
            updateTaskStatus(savedSubtask);
        }

        return convertToDTO(savedSubtask);
    }

    @Override
    public List<TaskDetailDTO> getSubtasks(Integer parentId) {
        List<Task> subtasks = taskRepository.findByParentIdAndDeletedFalse(parentId);
        return subtasks.stream()
            .map(task -> getTaskDetailById(task.getId()))
            .filter(taskDetail -> taskDetail != null)
            .collect(Collectors.toList());
    }

    @Override
    public List<TaskDetailDTO> getRootTasks() {
        List<Task> rootTasks = taskRepository.findByParentIsNullAndDeletedFalse();
        return rootTasks.stream()
            .map(task -> getTaskDetailById(task.getId()))
            .filter(taskDetail -> taskDetail != null)
            .collect(Collectors.toList());
    }

    // === ATTACHMENT MANAGEMENT ===
    // Đã loại bỏ assignAttachmentsToTask và removeAttachmentsFromTask
    // Attachment chỉ được quản lý thông qua createTask và updateTask
    
    /*
    // ❌ KHÔNG CẦN: Đã thay thế bằng logic trong createTask và updateTask
    @Override
    @Transactional
    public void assignAttachmentsToTask(Integer taskId, List<Integer> attachmentIds) {
        Task task = taskRepository.findByIdAndDeletedFalse(taskId).orElse(null);
        if (task == null) {
            throw new RuntimeException("Task not found: " + taskId);
        }

        List<Attachment> attachments = attachmentRepository.findAllByIdIn(attachmentIds);
        for (Attachment attachment : attachments) {
            if (!attachment.isDeleted()) {
                attachment.setTask(task);
            }
        }
        attachmentRepository.saveAll(attachments);
    }

    @Override
    @Transactional
    public void removeAttachmentsFromTask(Integer taskId, List<Integer> attachmentIds) {
        List<Attachment> attachments = attachmentRepository.findAllByIdIn(attachmentIds);
        for (Attachment attachment : attachments) {
            if (attachment.getTask() != null && attachment.getTask().getId().equals(taskId)) {
                attachment.setTask(null);
            }
        }
        attachmentRepository.saveAll(attachments);
    }
    */

    @Override
    public List<AttachmentDTO> getTaskAttachments(Integer taskId) {
        List<Attachment> attachments = attachmentRepository.findByTask_IdAndIsDeletedFalse(taskId);
        return attachments.stream()
            .map(att -> {
                AttachmentDTO dto = new AttachmentDTO();
                dto.setId(att.getId());
                dto.setFilePath(att.getFilePath());
                dto.setFileName(att.getFileName());
                dto.setFileSize(att.getFileSize());
                dto.setCreatedAt(att.getCreatedAt());
                if (att.getUploadedBy() != null) {
                    dto.setUploadedBy(new UserDTO(att.getUploadedBy()));
                }
                return dto;
            })
            .collect(Collectors.toList());
    }

    // ============== SEARCH & FILTER IMPLEMENTATIONS ==============

    @Override
    public List<TaskDetailDTO> searchTasksByTitle(String title) {
        List<Task> tasks = taskRepository.findByTitleContainingIgnoreCaseAndDeletedFalse(title);
        return tasks.stream()
            .map(task -> getTaskDetailById(task.getId()))
            .filter(taskDetail -> taskDetail != null)
            .collect(Collectors.toList());
    }

    @Override
    public List<TaskDetailDTO> getTasksByPriority(com.project.quanlycanghangkhong.model.TaskPriority priority) {
        List<Task> tasks = taskRepository.findByPriorityAndDeletedFalse(priority);
        return tasks.stream()
            .map(task -> getTaskDetailById(task.getId()))
            .filter(taskDetail -> taskDetail != null)
            .collect(Collectors.toList());
    }

    @Override
    public List<TaskDetailDTO> searchTasks(String keyword) {
        List<Task> tasks = taskRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseAndDeletedFalse(keyword, keyword);
        return tasks.stream()
            .map(task -> getTaskDetailById(task.getId()))
            .filter(taskDetail -> taskDetail != null)
            .collect(Collectors.toList());
    }

    /**
     * Cập nhật danh sách attachment của task
     * Logic: 
     * - null: không thay đổi 
     * - empty list: xóa hết attachment
     * - có giá trị: replace toàn bộ attachment list
     * @param task Task cần cập nhật
     * @param attachmentIds Danh sách attachment ID mới
     */
    private void updateTaskAttachments(Task task, List<Integer> attachmentIds) {
        // Lấy danh sách attachment hiện tại của task
        List<Attachment> currentAttachments = attachmentRepository.findByTask_IdAndIsDeletedFalse(task.getId());
        
        // Gỡ tất cả attachment hiện tại khỏi task
        for (Attachment attachment : currentAttachments) {
            attachment.setTask(null);
        }
        attachmentRepository.saveAll(currentAttachments);
        
        // Nếu có attachment mới, gán vào task
        if (!attachmentIds.isEmpty()) {
            List<Attachment> newAttachments = attachmentRepository.findAllByIdIn(attachmentIds);
            for (Attachment attachment : newAttachments) {
                if (!attachment.isDeleted()) {
                    attachment.setTask(task);
                }
            }
            attachmentRepository.saveAll(newAttachments);
        }
        // Nếu attachmentIds empty = xóa hết attachment (đã làm ở trên)
    }

    // ============== HELPER METHODS FOR HIERARCHICAL TASK MANAGEMENT ==============
    
    /**
     * 🌲 Lấy tất cả subtasks theo cấu trúc phân cấp (recursive)
     * Method này sẽ traverse toàn bộ cây subtask và trả về flat list
     * @param parentTask Task gốc
     * @return Danh sách tất cả task con (bao gồm cả task gốc)
     */
    private List<Task> getAllSubtasksRecursive(Task parentTask) {
        List<Task> allTasks = new ArrayList<>();
        allTasks.add(parentTask); // Thêm task gốc
        
        // Lấy tất cả subtasks trực tiếp
        List<Task> directSubtasks = taskRepository.findByParentIdAndDeletedFalse(parentTask.getId());
        
        // Recursive call cho mỗi subtask
        for (Task subtask : directSubtasks) {
            allTasks.addAll(getAllSubtasksRecursive(subtask));
        }
        
        return allTasks;
    }
    
    /**
     * 🌳 Lấy tất cả subtasks cho nhiều task cha (batch processing)
     * @param parentTasks Danh sách task cha
     * @return Danh sách tất cả tasks bao gồm cả subtasks
     */
    private List<Task> getAllSubtasksForTasks(List<Task> parentTasks) {
        List<Task> allTasks = new ArrayList<>();
        
        for (Task parentTask : parentTasks) {
            allTasks.addAll(getAllSubtasksRecursive(parentTask));
        }
        
        // Remove duplicates (trong trường hợp có subtask được reference nhiều lần)
        return allTasks.stream()
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * 🌳 Lấy task hierarchy với level đúng cho type=assigned
     * FIX: Tránh trùng lặp và đảm bảo hierarchy level chính xác
     * @param assignedTasks Danh sách task được giao
     * @return Danh sách TaskDetailDTO với hierarchyLevel được set đúng
     */
    private List<TaskDetailDTO> getTaskHierarchyWithLevels(List<Task> assignedTasks) {
        // Use Map để tránh trùng lặp và lưu trữ kết quả
        Map<Integer, TaskDetailDTO> resultMap = new HashMap<>();
        Set<Integer> processedIds = new HashSet<>();
        
        // Xử lý từng assigned task
        for (Task assignedTask : assignedTasks) {
            if (!processedIds.contains(assignedTask.getId())) {
                // Tính toán level thực tế của task này (dựa trên cấu trúc parent-child)
                int actualLevel = calculateActualLevel(assignedTask);
                
                // Lấy toàn bộ hierarchy từ task này
                List<TaskDetailDTO> hierarchy = getTaskHierarchyRecursive(assignedTask, actualLevel);
                
                // Merge vào result map
                for (TaskDetailDTO task : hierarchy) {
                    if (!resultMap.containsKey(task.getId())) {
                        resultMap.put(task.getId(), task);
                        processedIds.add(task.getId());
                    }
                }
            }
        }
        
        // Convert map to list và sort: hierarchy level trước, sau đó thời gian mới nhất
        return resultMap.values().stream()
            .sorted((t1, t2) -> {
                // 1. Sort theo hierarchyLevel (0=root, 1=child, ...)
                int levelCompare = Integer.compare(t1.getHierarchyLevel(), t2.getHierarchyLevel());
                if (levelCompare != 0) return levelCompare;
                
                // 2. Cùng level thì sort theo thời gian mới nhất (updatedAt DESC, createdAt DESC)
                Task task1 = taskRepository.findById(t1.getId()).orElse(null);
                Task task2 = taskRepository.findById(t2.getId()).orElse(null);
                if (task1 != null && task2 != null) {
                    int updatedCompare = task2.getUpdatedAt().compareTo(task1.getUpdatedAt());
                    if (updatedCompare != 0) return updatedCompare;
                    return task2.getCreatedAt().compareTo(task1.getCreatedAt());
                }
                
                // Fallback: sort theo ID
                return Integer.compare(t1.getId(), t2.getId());
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 🧮 Tính toán level thực tế của task trong cây hierarchy
     * @param task Task cần tính level
     * @return Level thực tế (0 = root, 1 = child, etc.)
     */
    private int calculateActualLevel(Task task) {
        int level = 0;
        Task current = task;
        
        // Đi ngược lên parent để tính level
        while (current.getParent() != null) {
            level++;
            current = current.getParent();
            
            // Tránh vòng lặp vô hạn
            if (level > 10) break; 
        }
        
        return level;
    }
    
    /**
     * 🌲 Đệ quy lấy task hierarchy với level chính xác
     * @param task Task hiện tại
     * @param level Level hiện tại trong hierarchy (0=root)
     * @return Danh sách TaskDetailDTO bao gồm task hiện tại và tất cả subtasks
     */
    private List<TaskDetailDTO> getTaskHierarchyRecursive(Task task, int level) {
        List<TaskDetailDTO> result = new ArrayList<>();
        
        // Convert task hiện tại với level (không include subtasks để tránh đệ quy vô hạn)
        TaskDetailDTO taskDetail = convertToTaskDetailDTOSimple(task);
        taskDetail.setHierarchyLevel(level);
        result.add(taskDetail);
        
        // Lấy tất cả subtasks trực tiếp
        List<Task> subtasks = taskRepository.findByParentIdAndDeletedFalse(task.getId());
        
        // Đệ quy cho mỗi subtask với level tăng lên
        for (Task subtask : subtasks) {
            result.addAll(getTaskHierarchyRecursive(subtask, level + 1));
        }
        
        return result;
    }
    
    /**
     * 🔄 Convert Task to TaskDetailDTO (simple version without subtasks to avoid infinite recursion)
     * @param task Task entity
     * @return TaskDetailDTO không bao gồm subtasks
     */
    private TaskDetailDTO convertToTaskDetailDTOSimple(Task task) {
        TaskDetailDTO dto = new TaskDetailDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setContent(task.getContent());
        dto.setInstructions(task.getInstructions());
        dto.setNotes(task.getNotes());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        
        // Set parent ID if exists
        if (task.getParent() != null) {
            dto.setParentId(task.getParent().getId());
        }
        
        // Set created by user
        if (task.getCreatedBy() != null) {
            dto.setCreatedByUser(new UserDTO(task.getCreatedBy()));
        }
        
        // Assignments
        List<AssignmentDTO> assignmentDTOs = assignmentRepository.findAll().stream()
            .filter(a -> a.getTask().getId().equals(task.getId()))
            .map(a -> {
                AssignmentDTO adto = new AssignmentDTO();
                adto.setAssignmentId(a.getAssignmentId());
                adto.setRecipientType(a.getRecipientType());
                adto.setRecipientId(a.getRecipientId());
                adto.setTaskId(a.getTask() != null ? a.getTask().getId() : null);
                if (a.getAssignedBy() != null) {
                    adto.setAssignedByUser(new UserDTO(a.getAssignedBy()));
                }
                adto.setAssignedAt(a.getAssignedAt() != null ? java.sql.Timestamp.valueOf(a.getAssignedAt()) : null);
                adto.setDueAt(a.getDueAt() != null ? java.sql.Timestamp.valueOf(a.getDueAt()) : null);
                adto.setNote(a.getNote());
                adto.setCompletedAt(a.getCompletedAt() != null ? java.sql.Timestamp.valueOf(a.getCompletedAt()) : null);
                if (a.getCompletedBy() != null) {
                    adto.setCompletedByUser(new UserDTO(a.getCompletedBy()));
                }
                adto.setStatus(a.getStatus());
                // Set recipient user based on type
                if ("user".equalsIgnoreCase(a.getRecipientType()) && a.getRecipientId() != null) {
                    userRepository.findById(a.getRecipientId()).ifPresent(u -> adto.setRecipientUser(new UserDTO(u)));
                } else if ("team".equalsIgnoreCase(a.getRecipientType()) && a.getRecipientId() != null) {
                    userRepository.findTeamLeadByTeamId(a.getRecipientId()).ifPresent(u -> adto.setRecipientUser(new UserDTO(u)));
                } else if ("unit".equalsIgnoreCase(a.getRecipientType()) && a.getRecipientId() != null) {
                    userRepository.findUnitLeadByUnitId(a.getRecipientId()).ifPresent(u -> adto.setRecipientUser(new UserDTO(u)));
                }
                return adto;
            }).toList();
        dto.setAssignments(assignmentDTOs);
        
        // Direct attachments
        List<AttachmentDTO> attachmentDTOs = new ArrayList<>();
        List<Attachment> directAttachments = attachmentRepository.findByTask_IdAndIsDeletedFalse(task.getId());
        for (Attachment att : directAttachments) {
            AttachmentDTO attDto = new AttachmentDTO();
            attDto.setId(att.getId());
            attDto.setFilePath(att.getFilePath());
            attDto.setFileName(att.getFileName());
            attDto.setFileSize(att.getFileSize());
            attDto.setCreatedAt(att.getCreatedAt());
            if (att.getUploadedBy() != null) {
                attDto.setUploadedBy(new UserDTO(att.getUploadedBy()));
            }
            attachmentDTOs.add(attDto);
        }
        dto.setAttachments(attachmentDTOs);
        
        // NOTE: Không include subtasks để tránh vô hạn đệ quy
        dto.setSubtasks(new ArrayList<>());
        
        return dto;
    }

    @Override
    public com.project.quanlycanghangkhong.dto.response.task.MyTasksResponse getMyTasksWithCount(String type) {
        // Lấy danh sách tasks (vẫn bao gồm tất cả như cũ cho data)
        List<TaskDetailDTO> tasks = getMyTasks(type);
        
        // Lấy user hiện tại để tính toàn bộ counts
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication != null ? authentication.getName() : null;
        User currentUser = (email != null) ? userRepository.findByEmail(email).orElse(null) : null;
        
        if (currentUser == null) {
            return new com.project.quanlycanghangkhong.dto.response.task.MyTasksResponse(
                "User không tìm thấy", 401, List.of(), 0, type, false, null);
        }
        
        // Tính toán count cho tất cả các loại (CHỈ ROOT TASKS)
        com.project.quanlycanghangkhong.dto.response.task.MyTasksResponse.TaskCountMetadata metadata = 
            calculateTaskCounts(currentUser.getId());
            
        // Tính totalCount CHỈ từ ROOT TASKS cho type hiện tại
        int totalCount = switch (type.toLowerCase()) {
            case "created" -> metadata.getCreatedCount();
            case "assigned" -> metadata.getAssignedCount(); 
            case "received" -> metadata.getReceivedCount();
            default -> tasks.size();
        };
        
        // Tạo response message với count root tasks
        String message = switch (type.toLowerCase()) {
            case "created" -> String.format("Danh sách công việc đã tạo nhưng chưa giao việc (%d root tasks)", totalCount);
            case "assigned" -> String.format("Danh sách công việc đã giao (%d root tasks, %d total với subtasks)", totalCount, tasks.size());
            case "received" -> String.format("Danh sách công việc được giao (%d root tasks)", totalCount);
            default -> String.format("Thành công (%d tasks)", totalCount);
        };
        
        return new com.project.quanlycanghangkhong.dto.response.task.MyTasksResponse(
            message, 200, tasks, totalCount, type, true, metadata);
    }
    
    /**
     * Tính toán count cho tất cả các loại task (CHỈ ROOT TASKS)
     */
    private com.project.quanlycanghangkhong.dto.response.task.MyTasksResponse.TaskCountMetadata calculateTaskCounts(Integer userId) {
        // Lấy user hiện tại để check team/unit
        User currentUser = userRepository.findById(userId).orElse(null);
        if (currentUser == null) {
            return new com.project.quanlycanghangkhong.dto.response.task.MyTasksResponse.TaskCountMetadata(
                0, 0, 0, null);
        }
        
        // Count created root tasks (chỉ root tasks)
        long createdCount = taskRepository.countCreatedRootTasksWithoutAssignments(userId);
        
        // Count assigned root tasks (chỉ root tasks)  
        long assignedCount = taskRepository.countAssignedRootTasksByUserId(userId);
        
        // Count received root tasks (user + team + unit)
        long receivedCount = taskRepository.countReceivedRootTasksByUserId(userId);
        
        // Add team assignments if user is TEAM_LEAD
        if (currentUser.getRole() != null && 
            "TEAM_LEAD".equals(currentUser.getRole().getRoleName()) &&
            currentUser.getTeam() != null) {
            receivedCount += taskRepository.countReceivedRootTasksByTeamId(currentUser.getTeam().getId());
        }
        
        // Add unit assignments if user is UNIT_LEAD
        if (currentUser.getRole() != null && 
            "UNIT_LEAD".equals(currentUser.getRole().getRoleName()) &&
            currentUser.getUnit() != null) {
            receivedCount += taskRepository.countReceivedRootTasksByUnitId(currentUser.getUnit().getId());
        }
        
        // Calculate hierarchy info for assigned tasks (vẫn cần để hiển thị chi tiết)
        List<Task> assignedTasks = taskRepository.findAssignedTasksByUserId(userId);
        List<TaskDetailDTO> assignedTasksWithHierarchy = getTaskHierarchyWithLevels(assignedTasks);
        com.project.quanlycanghangkhong.dto.response.task.MyTasksResponse.HierarchyInfo hierarchyInfo = 
            calculateHierarchyInfo(assignedTasksWithHierarchy);
        
        return new com.project.quanlycanghangkhong.dto.response.task.MyTasksResponse.TaskCountMetadata(
            (int)createdCount, (int)assignedCount, (int)receivedCount, hierarchyInfo);
    }
    
    /**
     * Tính toán thông tin hierarchy cho assigned tasks
     */
    private com.project.quanlycanghangkhong.dto.response.task.MyTasksResponse.HierarchyInfo calculateHierarchyInfo(
            List<TaskDetailDTO> tasksWithHierarchy) {
        
        if (tasksWithHierarchy.isEmpty()) {
            return new com.project.quanlycanghangkhong.dto.response.task.MyTasksResponse.HierarchyInfo(
                0, 0, 0, new java.util.HashMap<>());
        }
        
        int rootTasksCount = 0;
        int subtasksCount = 0;
        int maxLevel = 0;
        java.util.Map<Integer, Integer> countByLevel = new java.util.HashMap<>();
        
        for (TaskDetailDTO task : tasksWithHierarchy) {
            Integer level = task.getHierarchyLevel() != null ? task.getHierarchyLevel() : 0;
            
            if (level == 0) {
                rootTasksCount++;
            } else {
                subtasksCount++;
            }
            
            maxLevel = Math.max(maxLevel, level);
            countByLevel.put(level, countByLevel.getOrDefault(level, 0) + 1);
        }
        
        return new com.project.quanlycanghangkhong.dto.response.task.MyTasksResponse.HierarchyInfo(
            rootTasksCount, subtasksCount, maxLevel, countByLevel);
    }
}
