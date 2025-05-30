package com.project.quanlycanghangkhong.repository;

import com.project.quanlycanghangkhong.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {
    List<Assignment> findByTask_Id(Integer taskId);
}