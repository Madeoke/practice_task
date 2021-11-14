package com.example.practice_task.service;

import com.example.practice_task.Convertor.FileConvertor;
import com.example.practice_task.dto.FileInfoDto;
import com.example.practice_task.enums.AllowedTypes;
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
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService{
    private final FileRepository fileRepository;
    private final FileConvertor fileConvertor;

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


        boolean flag = false;
        for (AllowedTypes type: AllowedTypes.values()
        ) {
            if(file.getOriginalFilename().endsWith(type.name().toLowerCase()))
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
        return files.stream().map(FileInfo::getFilename)
                .collect(Collectors.toList());
    }

    public List<?> findAll( Filter filter){
        List<FileInfo> files = fileRepository.findAll(
                Specification
                        .where(FileInfoSpecification.filenameStartsWith(filter.getFilename()))
                        .and(FileInfoSpecification.dateBetween(
                                filter.getFrom(),
                                filter.getTo())
                        ));
        List<FileInfoDto> fileInfoDTOs = new  ArrayList<FileInfoDto>();
        if (files != null && !files.isEmpty()) {
            fileInfoDTOs = files
                    .stream()
                    .map(fileConvertor::convertToDto)
                    .collect(Collectors.toList());
        }
        if(filter.isContentType()){
            List<String> types = new ArrayList<>();
            for (FileInfo f: files
            ) {
                if(!types.contains(f.getContentType())){
                    types.add(f.getContentType());
                }
            }
            return types;
        }

        return fileInfoDTOs;
    }

    public FileInfo findFileInfo(UUID fileId) throws NoSuchFileException{
        FileInfo file = fileRepository.findById(fileId).orElse(null);
        if(file == null)
            throw new NoSuchFileException();
        return file;
    }

    public void delete(UUID id) {
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
