package com.example.practice_task.repository;

import com.example.practice_task.model.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<FileInfo,Long>, JpaSpecificationExecutor<FileInfo> {

}
