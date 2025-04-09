package ru.itmo.domain.dto.command;

import lombok.Builder;
import lombok.Getter;
import ru.itmo.domain.model.Student;

@Builder
@Getter
public class IsuValidationResult {
    private int isu;
    private Student student;
    private String errorText;
    private boolean alreadyRegistered;
}
