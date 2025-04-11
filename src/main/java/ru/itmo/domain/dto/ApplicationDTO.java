package ru.itmo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApplicationDTO {
    private String fullName;
    private String group;
    private String startDate;
    private String endDate;
    private String formatType;
    private String companyName;
}
