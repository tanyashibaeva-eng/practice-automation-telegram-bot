package ru.itmo.domain.dto.command;

import lombok.Builder;
import lombok.Data;
import ru.itmo.domain.dto.FileStreamDTO;

@Data
@Builder
public class ApplicationFillingResult {
    private String errorText;
    private FileStreamDTO fileStreamDTO;
}
