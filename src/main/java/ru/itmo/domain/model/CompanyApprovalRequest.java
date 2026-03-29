package ru.itmo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.domain.type.CompanyApprovalRequestStatus;
import ru.itmo.domain.type.PracticeFormat;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyApprovalRequest {
    private long id;
    private long studentChatId;
    private String eduStreamName;
    private long inn;
    private String companyName;
    private String companyAddress;
    private PracticeFormat practiceFormat;
    private String companyLeadFullName;
    private String companyLeadPhone;
    private String companyLeadEmail;
    private String companyLeadJobTitle;
    private boolean requiresSpbOfficeApproval;
    private CompanyApprovalRequestStatus status;
    private Long processedByChatId;
    private Timestamp createdAt;
    private Timestamp processedAt;
}
