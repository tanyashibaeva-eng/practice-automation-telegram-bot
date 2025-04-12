package ru.itmo.domain.dto.command;

import lombok.*;

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
