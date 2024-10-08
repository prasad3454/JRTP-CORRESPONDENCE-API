package com.co.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;

import com.co.entity.Plan;

public interface PlanRepo extends JpaRepository<Plan, Serializable>{
	
}
