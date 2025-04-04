package ru.itmo.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.itmo.domain.dto.ExcelStudentDTO;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.domain.type.PracticePlace;
import ru.itmo.domain.type.StudentStatus;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
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
    private Integer companyINN;
    private String companyName;
    private String companyLeadFullName;
    private String companyLeadPhone;
    private String companyLeadEmail;
    private String companyLeadJobTitle;
    private String cellHexColor;
    private boolean managedManually;
}

    public List<String> updateOrGetErrors(ExcelStudentDTO dto, StudentStatus currentStatus) {
        var errors = new ArrayList<String>();

        if (dto.getCompanyINN() != null && dto.getPracticeFormat() != null) {
            if (!isPracticeFormatValid(dto.getCompanyINN(), dto.getPracticeFormat())) {
                errors.add("для компаний не из Питера формат практики только \"Удаленный\"");
            }
        }

        if (dto.getCompanyLeadPhone() != null) {
            if (!isValidPhoneNumber(dto.getCompanyLeadPhone())) {
                errors.add("номер телефона должен начинаться с +7 или 8.");
            }
        }

        if (!isStatusTransitionValid(dto.getStatus())) {
            errors.add("переход из статуса %s в статус %s невозможен".formatted(currentStatus, dto.getStatus()));
        }
