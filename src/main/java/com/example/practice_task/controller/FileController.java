package com.example.practice_task.controller;

import com.example.practice_task.exceptions.BadFileTypeException;
import com.example.practice_task.exceptions.MaxUploadSizeException;
import com.example.practice_task.exceptions.NoSuchFileException;
import com.example.practice_task.exceptions.NullFileException;
import com.example.practice_task.model.FileInfo;
import com.example.practice_task.model.Filter;
import com.example.practice_task.service.FileService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/file")
@AllArgsConstructor
public class FileController {

    private final FileService fileService;


    @GetMapping(path = "/downloadFile")
    public ResponseEntity<?> downloadFile (UUID id){

        FileInfo file;
        try {
            file = fileService.findFileInfo(id);
        }catch (NoSuchFileException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format("Could not download the file! No Such File"));
        }

        byte [] rawFile = file.getFile();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Disposition","attachment; filename="+ file.getFilename());

        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(rawFile);
    }

    @GetMapping(path = "/downloadFiles")
    public ResponseEntity<?> downloadFiles (String idString) throws IOException{

        List<UUID> idList = new ArrayList<>();
        idList = Arrays.stream(idString.split(","))
                .map(UUID::fromString)
                .collect(Collectors.toList());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

            for (UUID id: idList
            ) {
                FileInfo file;
                try {
                    file = fileService.findFileInfo(id);
                }catch(NumberFormatException e){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(String.format("Could not download the file! Incorrect input!"));
                }catch (NoSuchFileException e ){
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
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
    public List<?> findAllFiles(@RequestBody Filter filter){
        List<?> files = fileService.findAll(filter);
        return  files;
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable("id") UUID id) {

        FileInfo fileInfo;
        try {
            fileInfo = fileService.findFileInfo(id);
        }catch (NoSuchFileException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format("Could not delete the file! No Such File"));
        }

        fileService.delete(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(String.format("File deleted successfully"));
    }

    @RequestMapping(value = "{id}", method = RequestMethod.PUT, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestParam("file") MultipartFile file) throws IOException{
        FileInfo current;
        try {
            current = fileService.findFileInfo(id);
        }catch (NoSuchFileException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
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
