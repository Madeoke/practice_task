package com.example.practice_task.Convertor;

import com.example.practice_task.dto.FileInfoDto;
import com.example.practice_task.model.FileInfo;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
public class FileConvertor {
    private final ModelMapper modelMapper;
    private Converter<Long, String> getUrl = (src) ->
            ServletUriComponentsBuilder.fromCurrentContextPath().path("/file/downloadFile").toUriString() + "?id=" + src.getSource();

    public FileConvertor() {
        this.modelMapper = new ModelMapper();
        modelMapper.createTypeMap(FileInfo.class, FileInfoDto.class)
                .addMappings(mapper -> mapper.using(getUrl).map(FileInfo::getId, FileInfoDto::setDownloadUrl));
    }

    public FileInfoDto convertToDto(FileInfo entity){
        return modelMapper.map(entity,FileInfoDto.class);
    }
}
