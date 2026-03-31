package ru.itmo.domain.dto.command;

import lombok.Data;

@Data
public class CompanyInfoUpdateResult {
    private boolean isSuccessful;
    private String errorText;
}
