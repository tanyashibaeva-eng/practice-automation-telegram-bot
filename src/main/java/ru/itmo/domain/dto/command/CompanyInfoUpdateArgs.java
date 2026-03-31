package ru.itmo.domain.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.domain.type.PracticeFormat;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class CompanyInfoUpdateArgs {
    private long chatId;
    private PracticeFormat practiceFormat;
    private Long practiceFormatId;
    private String practiceFormatDisplayName;
    private long inn;
    private String companyName;
    private String companyAddress;
    private String companyLeadFullName;
    private String companyLeadPhone;
    private String companyLeadEmail;
    private String companyLeadJobTitle;
    private boolean presentInITMOAgreementFile;
    private boolean requiresSpbOfficeApproval;
}
