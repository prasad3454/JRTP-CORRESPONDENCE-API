package com.co.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;

import com.co.entity.DCCaseEntity;

public interface CaseEntityRepository extends JpaRepository<DCCaseEntity, Serializable>{
	
	public DCCaseEntity findByAppId(Integer id);
}
