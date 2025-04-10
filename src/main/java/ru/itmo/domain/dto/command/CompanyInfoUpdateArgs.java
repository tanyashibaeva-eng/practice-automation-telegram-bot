package ru.itmo.domain.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.itmo.domain.type.PracticeFormat;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class CompanyInfoUpdateArgs {
    private long chatId;
    private PracticeFormat practiceFormat;
    private long inn;
    private String companyName;
    private String companyLeadFullname;
    private String companyLeadPhone;
    private String companyLeadEmail;
    private String companyLeadJobTitle;
}
