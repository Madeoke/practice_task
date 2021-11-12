package com.example.practice_task.service;

import com.example.practice_task.exceptions.BadFileTypeException;
import com.example.practice_task.exceptions.MaxUploadSizeException;
import com.example.practice_task.exceptions.NoSuchFileException;
import com.example.practice_task.exceptions.NullFileException;
import com.example.practice_task.model.FileInfo;
import com.example.practice_task.model.Filter;
import com.example.practice_task.repository.FileRepository;
import com.example.practice_task.repository.specification.FileInfoSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;

    public void uploadFile(MultipartFile file) throws IOException,NullFileException,BadFileTypeException, MaxUploadSizeException{

        validateFile(file);
        FileInfo fileInfo = new FileInfo();
        fileInfo = initFile(fileInfo, file);

        fileRepository.save(fileInfo);
    }

    private void validateFile(MultipartFile file) throws NullFileException,IOException,BadFileTypeException, MaxUploadSizeException{
        if(file == null){
            throw new NullFileException();
        }
        if(file.getSize()>15728640)
            throw new MaxUploadSizeException();
        List<String> types = Arrays.asList(".txt",".doc",".odt",".rtf",".html",".xml",".docx",".pdf");
        boolean flag = false;
        for (String type: types
             ) {
            if(file.getOriginalFilename().endsWith(type))
                flag = true;
        }
        if (!flag)
            throw new BadFileTypeException();
    }


    private Date getChangeDate(MultipartFile multipart) throws IOException {
        File convFile = new File( multipart.getOriginalFilename());
        multipart.transferTo(convFile);
        return new Date(convFile.lastModified());
    }

    public List<String> getFileNames(){
        List<FileInfo> files = fileRepository.findAll();
        List<String> fileNames = new ArrayList<>();
        for (FileInfo file: files
             ) {
            fileNames.add(file.getFilename());
        }
        return fileNames;

    }

    public <T> List<T>  findAll( Filter filter){
        List<FileInfo> fileInfos = fileRepository.findAll(
                Specification
                        .where(FileInfoSpecification.filenameStartsWith(filter.getFilename()))
                        .and(FileInfoSpecification.dateBetween(
                                filter.getFrom(),
                                filter.getTo())
                        ));
        if(filter.isContentType()){
            List<String> types = new ArrayList<>();
            for (FileInfo f: fileInfos
                 ) {
                if(!types.contains(f.getContentType())){
                    types.add(f.getContentType());
                }
            }
            return (List<T>) types;
        }

        return (List<T>) fileInfos;
    }
    
    public FileInfo findFileInfo(Long fileId) throws NoSuchFileException{
        FileInfo file = fileRepository.findById(fileId).orElse(null);
        if(file == null)
            throw new NoSuchFileException();
        return file;
    }

    public void delete(Long id) {
        fileRepository.delete(fileRepository.getById(id));
    }

    public void update(FileInfo fileInfo, MultipartFile file) throws IOException, BadFileTypeException, NullFileException, MaxUploadSizeException{

        validateFile(file);
        fileInfo = initFile(fileInfo, file);

        fileRepository.save(fileInfo);
    }

    private FileInfo initFile(FileInfo fileInfo, MultipartFile file) throws IOException{
        fileInfo.setFilename(StringUtils.cleanPath(file.getOriginalFilename()));
        fileInfo.setContentType(file.getContentType());
        fileInfo.setFile(file.getBytes());
        fileInfo.setUploadDate(new Date());
        fileInfo.setChangeDate(getChangeDate(file));
        fileInfo.setFileVolumeInBytes(file.getSize());
        return fileInfo;
    }
}
