package com.example.practice_task.service;

import com.example.practice_task.exceptions.BadFileTypeException;
import com.example.practice_task.exceptions.MaxUploadSizeException;
import com.example.practice_task.exceptions.NoSuchFileException;
import com.example.practice_task.exceptions.NullFileException;
import com.example.practice_task.model.FileInfo;
import com.example.practice_task.model.Filter;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface FileService {

    void uploadFile(MultipartFile file) throws IOException,NullFileException,BadFileTypeException, MaxUploadSizeException;

    List<?> findAll( Filter filter);

    List<String> getFileNames();
    
    FileInfo findFileInfo(UUID fileId) throws NoSuchFileException;

    void delete(UUID id);

    void update(FileInfo fileInfo, MultipartFile file) throws IOException, BadFileTypeException, NullFileException, MaxUploadSizeException;

}
