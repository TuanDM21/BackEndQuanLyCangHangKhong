package com.project.quanlycanghangkhong.service;

import java.util.List;

import com.project.quanlycanghangkhong.dto.UnitDTO;
import com.project.quanlycanghangkhong.model.Unit;

public interface UnitService {
    List<UnitDTO> getUnitsByTeam(Integer teamId);
    Unit getUnitById(Long id);
    Unit createUnit(Unit unit);
    void deleteUnit(Long id);
    List<UnitDTO> getAllUnits();
    
}
