package ru.itmo.domain.model;

import lombok.*;
import ru.itmo.domain.dto.ExcelStudentDTO;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.domain.type.PracticePlace;
import ru.itmo.domain.type.StudentStatus;

import java.util.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
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

    public static final Map<StudentStatus, Set<StudentStatus>> PossibleAdminStatusChangesMap = Map.of(
            StudentStatus.REGISTERED, Set.of(StudentStatus.PRACTICE_IN_ITMO_MARKINA),
            StudentStatus.PRACTICE_IN_ITMO_MARKINA, Set.of(StudentStatus.REGISTERED),
            StudentStatus.COMPANY_INFO_WAITING_APPROVAL, Set.of(StudentStatus.COMPANY_INFO_RETURNED, StudentStatus.PRACTICE_APPROVED, StudentStatus.APPLICATION_WAITING_SUBMISSION),
            StudentStatus.APPLICATION_WAITING_SUBMISSION, Set.of(StudentStatus.COMPANY_INFO_RETURNED),
            StudentStatus.APPLICATION_WAITING_APPROVAL, Set.of(StudentStatus.COMPANY_INFO_RETURNED, StudentStatus.APPLICATION_RETURNED, StudentStatus.APPLICATION_WAITING_SIGNING),
            StudentStatus.APPLICATION_WAITING_SIGNING, Set.of(StudentStatus.APPLICATION_RETURNED, StudentStatus.APPLICATION_SIGNED)
    );

    public List<String> updateOrGetErrors(ExcelStudentDTO dto) {
        var errors = new ArrayList<String>();

        if (!this.managedManually && dto.getCompanyINN() != null && dto.getPracticeFormat() != null) {
            if (!isPracticeFormatValid(dto.getCompanyINN(), dto.getPracticeFormat())) {
                errors.add("для компаний не из Питера формат практики только \"Удаленный\"");
            }
        }

        if (!this.managedManually && dto.getCompanyLeadPhone() != null) {
            if (!isRusPhoneNumber(dto.getCompanyLeadPhone())) {
                errors.add("номер телефона должен начинаться с +7 или 8.");
            }
        }

        this.isu = dto.getIsu();
        this.stGroup = dto.getStGroup();
        this.fullName = dto.getFullName();
        this.cellHexColor = dto.getCellHexColor();
        this.comments = dto.getComments();
        this.callStatusComments = dto.getCallStatusComments();
        if (!this.managedManually && (status == dto.getStatus() || (PossibleAdminStatusChangesMap.containsKey(status) && PossibleAdminStatusChangesMap.get(status).contains(dto.getStatus())))) {
            this.status = dto.getStatus();
            this.practicePlace = dto.getPracticePlace();
            this.practiceFormat = dto.getPracticeFormat();
            this.companyINN = dto.getCompanyINN();
            this.companyName = dto.getCompanyName();
            this.companyLeadFullName = dto.getCompanyLeadFullName();
            this.companyLeadPhone = dto.getCompanyLeadPhone();
            this.companyLeadEmail = dto.getCompanyLeadEmail();
            this.companyLeadJobTitle = dto.getCompanyLeadJobTitle();
        } else {
            errors.add("переход из статуса \"%s\" в статус \"%s\" невозможен".formatted(this.getStatus().getUserName(), dto.getStatus().getUserName()));
        }

        if (!this.managedManually && !this.isRequiredFieldsForCurrentStatusFilled()) {
            errors.add("не все поля для статуса \"%s\" заполнены".formatted(status.getUserName()));
        }

        return errors;
    }

    private boolean isRusPhoneNumber(String phone) {
        return phone.startsWith("+7") || phone.startsWith("8");
    }

    private boolean isPracticeFormatValid(Integer companyINN, PracticeFormat practiceFormat) {
        if (companyINN.toString().startsWith("78")) {
            return true;
        }
        return practiceFormat.equals(PracticeFormat.ONLINE);
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
        return this.isBaseRequiredFieldsFilled() && this.practicePlace == PracticePlace.ITMO_MARKINA;
    }

    private boolean isCompanyInfoFieldsFilled() {
        if (this.practicePlace == null || this.practicePlace == PracticePlace.NOT_SPECIFIED) {
            return false;
        }
        if (this.practicePlace == PracticePlace.ITMO_MARKINA) {
            return false;
        }
        if (this.practicePlace == PracticePlace.ITMO_UNIVERSITY) {
            return this.isBaseRequiredFieldsFilled() && this.practiceFormat != PracticeFormat.NOT_SPECIFIED && this.companyLeadFullName != null;
        }
        return this.isBaseRequiredFieldsFilled() && this.practiceFormat != null &&
                this.practiceFormat != PracticeFormat.NOT_SPECIFIED &&
                this.companyINN != null && this.companyName != null;
    }

    private boolean isApplicationInfoFieldsFilled() {
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
                    this.isApplicationInfoFieldsFilled();
            case PRACTICE_APPROVED -> true;
            default -> false;
        };
    }
}
