package ru.itmo.domain.model;

import lombok.Data;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.domain.type.PracticePlace;
import ru.itmo.domain.type.StudentStatus;

@Data
public class Student {
    private TelegramUser telegramUser;
    private EduStream eduStream;
    private int isu;
    private String stGroup;
    private String fullName;
    private StudentStatus status;
    private String comments;
    private String callStatusComments;
    private PracticePlace practicePlace;
    private PracticeFormat practiceFormat;
    private int companyINN;
    private String companyName;
    private String companyLeadFullName;
    private String companyLeadPhone;
    private String companyLeadEmail;
    private String companyLeadJobTitle;
    private String cellHexColor;
}
