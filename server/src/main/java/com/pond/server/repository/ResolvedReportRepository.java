package com.pond.server.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pond.server.model.ResolvedReport;

@Repository
public interface ResolvedReportRepository extends JpaRepository<ResolvedReport, UUID> {
    // Basic CRUD operations are inherited from JpaRepository
    // Add custom query methods here if needed in the future
}

