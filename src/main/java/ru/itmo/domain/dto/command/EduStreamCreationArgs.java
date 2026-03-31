package ru.itmo.domain.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;


@AllArgsConstructor
@Data
@Builder
public class EduStreamCreationArgs {
    private String name;
    private Integer year;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
