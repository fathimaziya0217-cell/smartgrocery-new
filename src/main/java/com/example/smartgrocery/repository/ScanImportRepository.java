package com.example.smartgrocery.repository;

import com.example.smartgrocery.entity.ScanImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScanImportRepository extends JpaRepository<ScanImport, Long> {
    List<ScanImport> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<ScanImport> findByIdAndUserId(Long id, Long userId);
}
