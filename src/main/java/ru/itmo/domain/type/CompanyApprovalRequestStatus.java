package ru.itmo.domain.type;

public enum CompanyApprovalRequestStatus {
    PENDING,
    APPROVED,
    REJECTED;

    public static CompanyApprovalRequestStatus valueOfIgnoreCase(String value) {
        return valueOf(value.trim().toUpperCase());
    }
}
