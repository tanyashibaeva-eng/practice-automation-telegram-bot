package ru.itmo.infra.handler.usecase.user.guide;

import lombok.RequiredArgsConstructor;
import ru.itmo.application.GuideService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.handler.usecase.user.UserCommand;

@RequiredArgsConstructor
public class GuideSectionOpenCommand implements UserCommand {

    private final String commandName;
    private final String descriptionTitle;

    @Override
    public MessageToUser execute(MessageDTO message) {
        try {
            return GuideService.openSectionByCommand(commandName);
        } catch (InternalException e) {
            return MessageToUser.builder()
                    .text("Не удалось загрузить раздел.")
                    .needRewriting(true)
                    .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return commandName;
    }

    @Override
    public String getDescription() {
        return descriptionTitle;
    }

    @Override
    public boolean isAvailableForStatus(StudentStatus status) {
        return true;
    }
}
