package ru.itmo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class StudentsWithErrors {
    private List<ExcelStudentDTO> students;
    private Map<Integer, List<String>> errorsByRows;
}