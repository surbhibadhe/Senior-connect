package com.project.seniorconnect.Repository;

import com.project.seniorconnect.Entity.ReportStatus;
import com.project.seniorconnect.Entity.ResourceReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResourceReportRepository extends JpaRepository<ResourceReport, Long> {

    boolean existsByReporterIdAndResourceIdAndStatus(Long reporterId, Long resourceId, ReportStatus status);

    Page<ResourceReport> findByStatus(ReportStatus status, Pageable pageable);

    List<ResourceReport> findByResourceId(Long resourceId);

    @Modifying
    @Query("UPDATE ResourceReport r SET r.resource = null WHERE r.resource.id = :resourceId")
    void disassociateResource(@Param("resourceId") Long resourceId);
}
