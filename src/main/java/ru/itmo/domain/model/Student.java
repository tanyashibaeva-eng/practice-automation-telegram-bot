package ru.itmo.domain.model;

import lombok.*;
import ru.itmo.domain.dto.ExcelStudentDTO;
import ru.itmo.domain.dto.ExcelStudentInfoDTO;
import ru.itmo.domain.dto.ForceUpdateDTO;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.domain.type.PracticePlace;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.BadRequestException;
import ru.itmo.util.TextParser;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(exclude = {"exportedAt", "updatedAt"})
public class Student {
    private static final Map<StudentStatus, Set<StudentStatus>> possibleAdminStatusChangesMap = Map.of(
            StudentStatus.REGISTERED, Set.of(StudentStatus.PRACTICE_IN_ITMO_MARKINA),
            StudentStatus.PRACTICE_IN_ITMO_MARKINA, Set.of(StudentStatus.REGISTERED),
            StudentStatus.COMPANY_INFO_WAITING_APPROVAL, Set.of(StudentStatus.COMPANY_INFO_RETURNED, StudentStatus.PRACTICE_APPROVED, StudentStatus.APPLICATION_WAITING_SUBMISSION),
            StudentStatus.APPLICATION_WAITING_SUBMISSION, Set.of(StudentStatus.COMPANY_INFO_RETURNED),
            StudentStatus.APPLICATION_WAITING_APPROVAL, Set.of(StudentStatus.COMPANY_INFO_RETURNED, StudentStatus.APPLICATION_RETURNED, StudentStatus.APPLICATION_WAITING_SIGNING),
            StudentStatus.APPLICATION_WAITING_SIGNING, Set.of(StudentStatus.APPLICATION_RETURNED, StudentStatus.APPLICATION_SIGNED)
    );
    @Setter
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
    private Long companyINN;
    private String companyName;
    private String companyLeadFullName;
    private String companyLeadPhone;
    private String companyLeadEmail;
    private String companyLeadJobTitle;
    private String cellHexColor;
    private boolean managedManually;
    private Timestamp exportedAt;
    private Timestamp updatedAt;
    private byte[] applicationBytes;
    private boolean isPingNeeded;

    public Student(ExcelStudentInfoDTO s, EduStream eduStream) {
        this.eduStream = eduStream;
        this.isu = s.getIsu();
        this.stGroup = s.getGroup();
        this.fullName = s.getFullName();
        this.status = StudentStatus.REGISTERED;
    }

    public Student duplicateBase() {
        return new Student(
                null,
                this.getEduStream(),
                this.getIsu(),
                this.getStGroup(),
                this.getFullName(),
                StudentStatus.NOT_REGISTERED,
                "",
                "",
                PracticePlace.NOT_SPECIFIED,
                PracticeFormat.NOT_SPECIFIED,
                null,
                null,
                null,
                null,
                null,
                null,
                "FFFFFF",
                false,
                null,
                null,
                null,
                false
        );
    }

    public List<String> forceUpdateOrGetErrors(ForceUpdateDTO dto) {
        var errors = new ArrayList<String>();

        try {
            if (dto.getStatus() != null) this.status = TextParser.parseStatus(dto.getStatus());
        } catch (BadRequestException e) {
            errors.add(e.getMessage());
        }

        try {
            if (dto.getPracticePlace() != null) this.practicePlace = TextParser.parsePracticePlace(dto.getStatus());
        } catch (BadRequestException e) {
            errors.add(e.getMessage());
        }

        try {
            if (dto.getPracticeFormat() != null) this.practiceFormat = TextParser.parsePracticeFormat(dto.getStatus());
        } catch (BadRequestException e) {
            errors.add(e.getMessage());
        }

        try {
            if (dto.getCompanyINN() != null) this.companyINN = TextParser.parseDoubleStrToLong(dto.getCompanyINN());
        } catch (BadRequestException e) {
            errors.add(e.getMessage());
        }

        if (dto.getCompanyName() != null) this.companyName = dto.getCompanyName();

        if (dto.getCompanyLeadFullName() != null) this.companyLeadFullName = dto.getCompanyLeadFullName();

        try {
            if (dto.getCompanyLeadPhone() != null)
                this.companyLeadPhone = TextParser.parsePhone(dto.getCompanyLeadPhone());
        } catch (BadRequestException e) {
            errors.add(e.getMessage());
        }

        try {
            if (dto.getCompanyLeadEmail() != null)
                this.companyLeadEmail = TextParser.parseEmail(dto.getCompanyLeadEmail());
        } catch (BadRequestException e) {
            errors.add(e.getMessage());
        }

        if (dto.getCompanyLeadJobTitle() != null) this.companyLeadJobTitle = dto.getCompanyLeadJobTitle();

        this.managedManually = true;
        return errors;
    }

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
        this.cellHexColor = dto.getCellHexColor() == null ? "FFFFFF" : dto.getCellHexColor().replace("#", "");
        this.comments = dto.getComments();
        this.callStatusComments = dto.getCallStatusComments();
        if (this.updatedAt.before(this.exportedAt)) {
            if (status == dto.getStatus()
                    || possibleAdminStatusChangesMap.containsKey(status)
                    && possibleAdminStatusChangesMap.get(status).contains(dto.getStatus())) {
                if (this.status != dto.getStatus()) {
                    this.status = dto.getStatus();
                    this.isPingNeeded = true;
                }
                this.practicePlace = dto.getPracticePlace();
                this.practiceFormat = dto.getPracticeFormat();
                this.companyINN = dto.getCompanyINN();
                this.companyName = dto.getCompanyName();
                this.companyLeadFullName = dto.getCompanyLeadFullName();
                this.companyLeadPhone = dto.getCompanyLeadPhone();
                this.companyLeadEmail = dto.getCompanyLeadEmail();
                this.companyLeadJobTitle = dto.getCompanyLeadJobTitle();
            } else {
                errors.add("переход из статуса \"%s\" в статус \"%s\" невозможен".formatted(this.getStatus().getDisplayName(), dto.getStatus().getDisplayName()));
            }
        }

        if (!this.managedManually && !isRequiredFieldsForCurrentStatusFilled(dto, this)) {
            errors.add("не все поля для статуса \"%s\" заполнены".formatted(status.getDisplayName()));
        }

        return errors;
    }

    private boolean isRusPhoneNumber(String phone) {
        return phone.startsWith("+7") || phone.startsWith("8") || phone.startsWith("7");
    }

    private boolean isPracticeFormatValid(Long companyINN, PracticeFormat practiceFormat) {
        if (companyINN.toString().startsWith("78")) {
            return true;
        }
        return practiceFormat.equals(PracticeFormat.ONLINE);
    }

    private static boolean isBaseRequiredFieldsFilled(ExcelStudentDTO dto) {
        return dto.getIsu() != 0 && dto.getStGroup() != null && !dto.getStGroup().isEmpty() &&
                dto.getFullName() != null && !dto.getFullName().isEmpty() &&
                dto.getStatus() != null;
    }

    private static boolean isRegisteredFieldsFilled(ExcelStudentDTO dto) {
        return isBaseRequiredFieldsFilled(dto);
    }

    private static boolean isITMOMarkinaFieldsFilled(ExcelStudentDTO dto) {
        return isBaseRequiredFieldsFilled(dto) && dto.getPracticePlace() == PracticePlace.ITMO_MARKINA;
    }

    private static boolean isCompanyInfoFieldsFilled(ExcelStudentDTO dto) {
        if (dto.getPracticePlace() == null || dto.getPracticePlace() == PracticePlace.NOT_SPECIFIED) {
            return false;
        }
        if (dto.getPracticePlace() == PracticePlace.ITMO_MARKINA) {
            return false;
        }
        if (dto.getPracticePlace() == PracticePlace.ITMO_UNIVERSITY) {
            return isBaseRequiredFieldsFilled(dto) && dto.getCompanyLeadFullName() != null;
        }
        return isBaseRequiredFieldsFilled(dto) && dto.getPracticeFormat() != null &&
                dto.getPracticeFormat() != PracticeFormat.NOT_SPECIFIED &&
                dto.getCompanyINN() != null && dto.getCompanyName() != null &&
                dto.getCompanyLeadFullName() != null &&
                dto.getCompanyLeadPhone() != null && dto.getCompanyLeadEmail() != null && dto.getCompanyLeadJobTitle() != null;
    }

    private static boolean isApplicationInfoFieldsFilled(ExcelStudentDTO dto, Student student) {
        return student.applicationBytes != null
                && student.applicationBytes.length != 0
                && isCompanyInfoFieldsFilled(dto);
    }

    private static boolean isRequiredFieldsForCurrentStatusFilled(ExcelStudentDTO dto, Student student) {
        return switch (dto.getStatus()) {
            case NOT_REGISTERED -> isBaseRequiredFieldsFilled(dto);
            case REGISTERED -> isRegisteredFieldsFilled(dto);
            case PRACTICE_IN_ITMO_MARKINA -> isITMOMarkinaFieldsFilled(dto);
            case COMPANY_INFO_WAITING_APPROVAL, COMPANY_INFO_RETURNED, APPLICATION_WAITING_SUBMISSION ->
                    isCompanyInfoFieldsFilled(dto);
            case APPLICATION_WAITING_APPROVAL, APPLICATION_RETURNED, APPLICATION_WAITING_SIGNING ->
                    isApplicationInfoFieldsFilled(dto, student);
            case PRACTICE_APPROVED -> true;
            case APPLICATION_SIGNED -> true;
            default -> false;
        };
    }

    public String[] getTransitionStatuses() {
        return switch (this.status) {
            case NOT_REGISTERED -> new String[]{StudentStatus.NOT_REGISTERED.getDisplayName()};
            case REGISTERED -> new String[]{StudentStatus.REGISTERED.getDisplayName()};
            case PRACTICE_IN_ITMO_MARKINA ->
                    new String[]{StudentStatus.PRACTICE_IN_ITMO_MARKINA.getDisplayName(), StudentStatus.REGISTERED.getDisplayName()};
            case COMPANY_INFO_WAITING_APPROVAL -> {
                if (this.practicePlace.equals(PracticePlace.ITMO_UNIVERSITY)) {
                    yield new String[]{StudentStatus.COMPANY_INFO_WAITING_APPROVAL.getDisplayName(), StudentStatus.COMPANY_INFO_RETURNED.getDisplayName(), StudentStatus.PRACTICE_APPROVED.getDisplayName()};
                } else {
                    yield new String[]{StudentStatus.COMPANY_INFO_WAITING_APPROVAL.getDisplayName(), StudentStatus.COMPANY_INFO_RETURNED.getDisplayName(), StudentStatus.APPLICATION_WAITING_SUBMISSION.getDisplayName()};
                }
            }
            case COMPANY_INFO_RETURNED -> new String[]{StudentStatus.COMPANY_INFO_RETURNED.getDisplayName()};
            case APPLICATION_WAITING_SUBMISSION ->
                    new String[]{StudentStatus.APPLICATION_WAITING_SUBMISSION.getDisplayName(), StudentStatus.APPLICATION_RETURNED.getDisplayName()};
            case APPLICATION_WAITING_APPROVAL ->
                    new String[]{StudentStatus.APPLICATION_WAITING_APPROVAL.getDisplayName(), StudentStatus.APPLICATION_RETURNED.getDisplayName(), StudentStatus.APPLICATION_WAITING_SIGNING.getDisplayName(), StudentStatus.COMPANY_INFO_RETURNED.getDisplayName()};
            case APPLICATION_RETURNED -> new String[]{StudentStatus.APPLICATION_RETURNED.getDisplayName()};
            case APPLICATION_WAITING_SIGNING ->
                    new String[]{StudentStatus.APPLICATION_WAITING_SIGNING.getDisplayName(), StudentStatus.APPLICATION_RETURNED.getDisplayName(), StudentStatus.APPLICATION_SIGNED.getDisplayName()};
            case APPLICATION_SIGNED -> new String[]{StudentStatus.APPLICATION_SIGNED.getDisplayName()};
            case PRACTICE_APPROVED ->
                    new String[]{StudentStatus.PRACTICE_APPROVED.getDisplayName()};
        };
    }
}
