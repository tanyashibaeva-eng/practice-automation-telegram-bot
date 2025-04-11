package ru.itmo.domain.dto.command;

import lombok.Builder;
import lombok.Data;

import java.io.File;

@Data
@Builder
public class ApplicationFillingResult {
    private String errorText;
    private File file;
}
