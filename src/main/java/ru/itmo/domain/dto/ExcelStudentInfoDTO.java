package ru.itmo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ExcelStudentInfoDTO {
    String group;
    Integer isu;
    String fullName;
    Integer row;
    List<String> errors;
}
