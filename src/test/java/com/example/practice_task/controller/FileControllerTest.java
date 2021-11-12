package com.example.practice_task.controller;

import com.example.practice_task.service.FileService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(FileController.class)
public class FileControllerTest {
    @MockBean
    private FileService fileService;

    @Autowired MockMvc mockMvc;

    @Value("${app.document-root}")String documentRoot;

    @Test
    public void test_handleFileUpload() throws Exception{

        String fileName = "sample-file-mock.txt";
        MockMultipartFile sampleFile = new MockMultipartFile(
                "file",
                fileName,
                "text/plain",
                "This is the file content".getBytes());

        MockMultipartHttpServletRequestBuilder multipartRequest =
                MockMvcRequestBuilders.multipart("/file/upload");

        mockMvc
                .perform(multipartRequest.file(sampleFile))
                .andExpect(status().isOk());
    }

    @Test
    public void test_handleFileUpload_NoFileProvided() throws Exception{
        MockMultipartHttpServletRequestBuilder multipartRequest =
                MockMvcRequestBuilders.multipart("/file/upload");

        mockMvc.perform(multipartRequest)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void test_handleFileUpdate() throws Exception{

        String fileName = "sample-file-mock.txt";
        MockMultipartFile sampleFile = new MockMultipartFile(
                "file",
                fileName,
                "text/plain",
                "This is the file content".getBytes());

        fileService.uploadFile(sampleFile);

        String fileName2 = "sample-file-mock1.txt";
        MockMultipartFile sampleFile1 = new MockMultipartFile(
                "file",
                fileName,
                "text/plain",
                "This is other content".getBytes());

        MockMultipartHttpServletRequestBuilder multipartRequest =
                MockMvcRequestBuilders.multipart("/file/1");

        multipartRequest.with(new RequestPostProcessor() {
            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.setMethod("PUT");
                return request;
            }
        });
        mockMvc
                .perform(multipartRequest.file(sampleFile1))
                .andExpect(status().isOk());
    }

    @Test
    public void test_deleteFile() throws Exception {


        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/file/{id}", "1"))
            .andExpect(status().isOk());
    }
}
