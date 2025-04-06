package ru.itmo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
public class EduStream {
    private String name;
    private int year;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
