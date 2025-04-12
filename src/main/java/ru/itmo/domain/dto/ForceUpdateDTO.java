package ru.itmo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForceUpdateDTO {
    private long chatId;
    private String eduStreamName;
    private String status;
    private String practicePlace;
    private String practiceFormat;
    private String companyINN;
    private String companyName;
    private String companyLeadFullName;
    private String companyLeadPhone;
    private String companyLeadEmail;
    private String companyLeadJobTitle;
}