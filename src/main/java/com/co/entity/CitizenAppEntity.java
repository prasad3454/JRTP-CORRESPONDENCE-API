package com.co.entity;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.Data;

@Entity
@Table(name = "CITIZEN_APPS")
@Data
public class CitizenAppEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer appId;
	
	private String fullName;
	
	private String email;
	
	private Long phno;
	
	private Long ssn;
	
	private String gender;
	
	private String stateName;
	
	private LocalDate dob;
	
	@CreationTimestamp
	private LocalDate createDate;
	
	@CreationTimestamp
	private LocalDate updateDate;
	
	private String createdBy;
	
	private String updatedBy;
}
