package ru.itmo.domain.dto.command;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class InnValidationResult {
    private long inn;
    private String companyName;
    private boolean userShouldProvideCompanyName;
    private boolean isPresentInITMOAgreementFile;
    private boolean isSPB;
    private String errorText;
}
