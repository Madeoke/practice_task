package com.example.practice_task.controller;

import com.example.practice_task.dto.FileInfoDto;
import com.example.practice_task.exceptions.BadFileTypeException;
import com.example.practice_task.exceptions.MaxUploadSizeException;
import com.example.practice_task.exceptions.NoSuchFileException;
import com.example.practice_task.exceptions.NullFileException;
import com.example.practice_task.model.FileInfo;
import com.example.practice_task.model.Filter;
import com.example.practice_task.service.FileService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/file")
@AllArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;

    private final ModelMapper modelMapper;

    @GetMapping(path = "/downloadFile")
    public ResponseEntity<?> downloadFile (Long id){

        FileInfo file;
        try {
            file = fileService.findFileInfo(id);
        }catch (NoSuchFileException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(String.format("Could not download the file! No Such File"));
        }

        byte [] rawFile = file.getFile();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-type", MediaType.IMAGE_JPEG_VALUE);
        headers.set("Content-Disposition","attachment; filename="+ file.getFilename()); // to view in browser change attachment to inline

        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(rawFile);
    }

    @GetMapping(path = "/downloadFiles")
    public ResponseEntity<?> downloadFiles (String idString) throws IOException{

        List<String> idList = new ArrayList<>();
        idList = Arrays.asList(idString.split(","));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

            for (String id: idList
            ) {
                FileInfo file;
                try {
                    file = fileService.findFileInfo(Long.valueOf(id));
                }catch(NumberFormatException e){
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(String.format("Could not download the file! Incorrect input!"));
                }catch (NoSuchFileException e ){
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(String.format("Could not download the file! No Such File"));
                }
                ZipEntry entry = new ZipEntry(file.getFilename());
                byte [] rawFile = file.getFile();
                entry.setSize(rawFile.length);
                zos.putNextEntry(entry);
                zos.write(rawFile);
                zos.closeEntry();
            }


        zos.close();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-type", MediaType.IMAGE_JPEG_VALUE);
        headers.set("Content-Disposition","attachment; filename=zip.zip"); // to view in browser change attachment to inline

        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(baos.toByteArray());
    }

    @PostMapping(path = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            fileService.uploadFile(file);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(String.format("File uploaded successfully: %s", file.getOriginalFilename()));
        } catch (NullFileException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(String.format("Null file error"));
        } catch (BadFileTypeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(String.format("Bad File Type"));
        } catch (MaxUploadSizeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(String.format("Too Large FIle!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(String.format("Could not upload the file: %s!", file.getOriginalFilename()));
        }
    }

    @GetMapping("filenames")
    public List<String> getFileNames(){

        return fileService.getFileNames();
    }

    @PostMapping("findAll")
    public <T> List<T> findAllFiles(@RequestBody Filter filter){
        List<FileInfo> files = fileService.findAll(filter);
        if(filter.isContentType())
            return (List<T>) files;
        List<FileInfoDto> files1 = new  ArrayList<FileInfoDto>();
        if (files != null && !files.isEmpty()) {
            files1=  files
                    .stream()
                    .map(file -> modelMapper.map(file, FileInfoDto.class))
                    .collect(Collectors.toList());
            for (FileInfoDto file : files1
                 ) {

                file.setDownloadUrl(ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/file/")
                        .path(String.valueOf(file.getId()))
                        .toUriString());
            }
            return (List<T>) files1;
        }
        return Collections.emptyList();
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {

        FileInfo fileInfo;
        try {
            fileInfo = fileService.findFileInfo(id);
        }catch (NoSuchFileException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(String.format("Could not delete the file! No Such File"));
        }

        fileService.delete(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(String.format("File deleted successfully"));
    }

    @RequestMapping(value = "{id}", method = RequestMethod.PUT, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException{
        FileInfo current;
        try {
            current = fileService.findFileInfo(id);
        }catch (NoSuchFileException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(String.format("Could not update the file! No Such File"));
        }

        try {
            fileService.update(current,file);
        }catch (NullFileException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(String.format("Null file error"));
        } catch (BadFileTypeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(String.format("Bad File Type"));
        } catch (MaxUploadSizeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(String.format("Too Large FIle!"));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(String.format("File updated successfully"));
    }
}
