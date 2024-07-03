package com.example.service;

import com.example.entity.ScannerResult;
import com.example.entity.Scans;
import com.example.repository.ScanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author ishrat.jahan
 * @since 06/09,2024
 */
@Service
public class ScanImpl implements ScanService {

    @Autowired
    private ScanRepository scanRepository;

    @Override
    public void saveScan(Scans scans) {
        scanRepository.save(scans);
    }
}
