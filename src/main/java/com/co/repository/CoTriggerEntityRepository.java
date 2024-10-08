package com.co.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.co.entity.CoTriggerEntity;

public interface CoTriggerEntityRepository extends JpaRepository<CoTriggerEntity, Integer>{
	
	public List<CoTriggerEntity> findByTrgStatus(String status);
	
	public CoTriggerEntity findByCaseNum(Long caseNum);
}
