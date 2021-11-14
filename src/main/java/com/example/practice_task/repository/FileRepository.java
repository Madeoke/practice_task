package com.example.practice_task.repository;

import com.example.practice_task.model.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<FileInfo, UUID>, JpaSpecificationExecutor<FileInfo> {

}
