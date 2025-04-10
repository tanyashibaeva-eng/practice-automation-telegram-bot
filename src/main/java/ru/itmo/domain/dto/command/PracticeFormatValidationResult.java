package ru.itmo.domain.dto.command;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PracticeFormatValidationResult {
    private String errorText;
}
