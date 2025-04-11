package ru.itmo.domain.dto.command;

import lombok.*;
import ru.itmo.domain.type.PracticeFormat;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class CompanyInfoUpdateArgs {
    private long chatId;
    private PracticeFormat practiceFormat;
    private long inn;
    private String companyName;
    private String companyLeadFullName;
    private String companyLeadPhone;
    private String companyLeadEmail;
    private String companyLeadJobTitle;
}
