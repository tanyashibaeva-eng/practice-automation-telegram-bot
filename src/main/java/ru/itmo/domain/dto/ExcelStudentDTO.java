package ru.itmo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.itmo.domain.type.StudentStatus;

@Getter
@AllArgsConstructor
public class ExcelStudentDTO {
    private Integer isu;
    private String stGroup;
    private String fullName;
    private StudentStatus status;
    private String comments;
    private Integer companyINN;
    private String companyName;
    private String companyLeadFullName;
    private String companyLeadPhone;
    private String companyLeadEmail;
    private String companyLeadJobTitle;
}
