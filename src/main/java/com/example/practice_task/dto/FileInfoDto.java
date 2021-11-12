package com.example.practice_task.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FileInfoDto {
    private Long id;
    private String filename;
    private String fileType;
    private Date uploadDate;
    private Date changeDate;
    private Long fileVolumeInBytes;
    private String downloadUrl;

}
