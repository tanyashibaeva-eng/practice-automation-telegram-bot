package ru.itmo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateStudentDTO {
    String eduStreamName;
    String group;
    int isu;
    String fullName;
}
