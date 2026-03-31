package ru.itmo.infra.handler.usecase.user;

import ru.itmo.domain.type.StudentStatus;
import ru.itmo.infra.handler.usecase.Command;

public interface UserCommand extends Command {
    default String getDisplayName() {
        return getDescription();
    }

    default boolean isAvailableForStatus(StudentStatus status) {
        return true;
    }
}