package ru.itmo.domain.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EduStream {
    private long id;
    private int year;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
