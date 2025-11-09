package com.pond.server.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pond.server.dto.CreateReportRequest;
import com.pond.server.dto.ReportDTO;
import com.pond.server.dto.UpdateReportRequest;
import com.pond.server.model.User;
import com.pond.server.service.ReportService;

@RestController
@RequestMapping("/reports")
public class ReportController {
    
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }
    
    @PostMapping
    public ResponseEntity<ReportDTO> createReport(
            @AuthenticationPrincipal User user,
            @RequestBody CreateReportRequest request) {
        return ResponseEntity.ok(reportService.createReport(user.getUserGU(), request));
    }
    
    @GetMapping("/admin")
    public ResponseEntity<Page<ReportDTO>> getAllReports(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (!user.getAdmin()) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(
            reportService.getAllReports(PageRequest.of(page, size)));
    }
    
    @GetMapping("/admin/pending-count")
    public ResponseEntity<Long> getPendingCount(@AuthenticationPrincipal User user) {
        if (!user.getAdmin()) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(reportService.getPendingReportCount());
    }
    
    @PutMapping("/admin/{reportGU}")
    public ResponseEntity<ReportDTO> updateReport(
            @AuthenticationPrincipal User user,
            @PathVariable String reportGU,
            @RequestBody UpdateReportRequest request) {
        
        if (!user.getAdmin()) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(
            reportService.updateReportStatus(
                UUID.fromString(reportGU), 
                user.getUserGU(), 
                request));
    }
    
    @GetMapping("/my-reports/outgoing")
    public ResponseEntity<Page<ReportDTO>> getMyOutgoingReports(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        return ResponseEntity.ok(
            reportService.getUserOutgoingReports(
                user.getUserGU(), 
                PageRequest.of(page, size)));
    }
    
    @GetMapping("/my-reports/incoming")
    public ResponseEntity<Page<ReportDTO>> getMyIncomingReports(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        return ResponseEntity.ok(
            reportService.getUserIncomingReports(
                user.getUserGU(), 
                PageRequest.of(page, size)));
    }
}