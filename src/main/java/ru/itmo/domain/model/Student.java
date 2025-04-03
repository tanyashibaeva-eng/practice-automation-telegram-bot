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

        if (errors.isEmpty()) {
            this.isu = dto.getIsu();
            this.stGroup = dto.getStGroup();
            this.fullName = dto.getFullName();
            this.status = dto.getStatus();
            this.comments = dto.getComments();
            this.callStatusComments = dto.getCallStatusComments();
            this.practicePlace = dto.getPracticePlace();
            this.practiceFormat = dto.getPracticeFormat();
            this.companyINN = dto.getCompanyINN();
            this.companyName = dto.getCompanyName();
            this.companyLeadFullName = dto.getCompanyLeadFullName();
            this.companyLeadPhone = dto.getCompanyLeadPhone();
            this.companyLeadEmail = dto.getCompanyLeadEmail();
            this.companyLeadJobTitle = dto.getCompanyLeadJobTitle();
            this.cellHexColor = dto.getCellHexColor();
        }

        if (!this.isRequiredFieldsForCurrentStatusFilled()) {
            errors.add("не все поля для нового статуса заполнены");
        }

        return errors;
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone.startsWith("+7") || phone.startsWith("8");
    }

    private boolean isPracticeFormatValid(Integer companyINN, PracticeFormat practiceFormat) {
        if (companyINN.toString().startsWith("78")) {
            return true;
        }
        return practiceFormat.equals(PracticeFormat.ONLINE);
    }

    private boolean isStatusTransitionValid(StudentStatus newStatus) {
        // TODO: add validation
        return true;
    }

    private boolean isBaseRequiredFieldsFilled() {
        return this.isu != 0 && this.stGroup != null && !this.stGroup.isEmpty() &&
                this.fullName != null && !this.fullName.isEmpty() &&
                this.status != null;
    }

    private boolean isRegisteredFieldsFilled() {
        return this.isBaseRequiredFieldsFilled();
    }

    private boolean isITMOMarkinaFieldsFilled() {
        return this.isBaseRequiredFieldsFilled() && this.practiceFormat != null;
    }

    private boolean isCompanyInfoFieldsFilled() {
        if (this.practicePlace == null) {
            return false;
        }
        if (this.practicePlace == PracticePlace.ITMO_MARKINA) {
            return false;
        }
        if (this.practicePlace == PracticePlace.ITMO_UNIVERSITY) {
            return this.isBaseRequiredFieldsFilled() && this.practicePlace != null && this.companyLeadFullName != null;
        }
        return this.isBaseRequiredFieldsFilled() && this.practiceFormat != null && this.companyINN != null;
    }

    private boolean isApplicationsInfoFieldsFilled() {
        return this.isCompanyInfoFieldsFilled() && this.companyLeadFullName != null &&
                this.companyLeadPhone != null && this.companyLeadEmail != null && this.companyLeadJobTitle != null;
    }

    private boolean isRequiredFieldsForCurrentStatusFilled() {
        return switch (this.status) {
            case NOT_REGISTERED -> this.isBaseRequiredFieldsFilled();
            case REGISTERED -> this.isRegisteredFieldsFilled();
            case PRACTICE_IN_ITMO_MARKINA -> this.isITMOMarkinaFieldsFilled();
            case COMPANY_INFO_WAITING_APPROVAL, COMPANY_INFO_RETURNED, APPLICATION_WAITING_SUBMISSION ->
                    this.isCompanyInfoFieldsFilled();
            case APPLICATION_WAITING_APPROVAL, APPLICATION_RETURNED, APPLICATION_WAITING_SIGNING ->
                    this.isApplicationsInfoFieldsFilled();
            case PRACTICE_APPROVED -> true;
            default -> false;
        };
    }
}

