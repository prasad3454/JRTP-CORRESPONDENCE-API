package com.co.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.co.entity.EligibilityDetailEntity;

public interface EligibilityDetailEntityRepository extends JpaRepository<EligibilityDetailEntity, Integer>{
	
	public EligibilityDetailEntity findByCaseNum(Long caseNum);
}
