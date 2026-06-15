package com.hr.hrapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hr.hrapp.entity.CompanyUpdate;

public interface CompanyUpdateRepository
extends JpaRepository<CompanyUpdate, Long> {
	
}
