package ru.itmo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateStudentDTO {
    String eduStreamName;
    String stGroup;
    int isu;
    String fullName;
}
