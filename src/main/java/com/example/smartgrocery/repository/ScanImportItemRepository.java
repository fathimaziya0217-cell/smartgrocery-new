package com.example.smartgrocery.repository;

import com.example.smartgrocery.entity.ScanImportItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScanImportItemRepository extends JpaRepository<ScanImportItem, Long> {
    List<ScanImportItem> findByScanImportId(Long scanImportId);
}
