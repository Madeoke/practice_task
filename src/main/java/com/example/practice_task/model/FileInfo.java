package com.example.practice_task.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class FileInfo {
    @Id
    @GeneratedValue
    private UUID id;

    private String filename;
    private String contentType;

    private Date uploadDate;
    private Date changeDate;

    @Lob
    private byte[] file;
    private Long fileVolumeInBytes;
}
