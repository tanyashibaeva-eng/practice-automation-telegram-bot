package ru.itmo.domain.dto.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserRegistrationResult {
    private String errorText;
}
