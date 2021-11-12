package com.example.practice_task.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class Filter {
    private String filename;

    private Date from;

    private Date to;

    private boolean contentType;
}