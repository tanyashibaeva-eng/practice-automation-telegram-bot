package ru.itmo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.InputStream;

@AllArgsConstructor
@Getter
@Builder
public class FileStreamDTO {
    private InputStream fileStream;
    private String fileName;
}
