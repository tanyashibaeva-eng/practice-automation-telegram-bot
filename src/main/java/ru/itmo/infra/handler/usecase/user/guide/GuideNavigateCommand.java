package ru.itmo.infra.handler.usecase.user.guide;

import lombok.NoArgsConstructor;
import ru.itmo.application.GuideService;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.handler.usecase.user.UserCommand;

@NoArgsConstructor
public class GuideNavigateCommand implements UserCommand {

    public static final String COMMAND_NAME = "/guide_nav";

    @Override
    public MessageToUser execute(MessageDTO message) {
        CallbackData cd = new CallbackData(message.getText());
        if (!"subsectionId".equals(cd.getKey()) || cd.getValue() == null || cd.getValue().isBlank()) {
            return MessageToUser.builder()
                    .text("Некорректные данные навигации.")
                    .needRewriting(true)
                    .build();
        }
        try {
            int id = Integer.parseInt(cd.getValue().trim());
            return GuideService.openSubsectionById(id);
        } catch (NumberFormatException e) {
            return MessageToUser.builder()
                    .text("Некорректный идентификатор подраздела.")
                    .needRewriting(true)
                    .build();
        } catch (InternalException e) {
            return MessageToUser.builder()
                    .text("Не удалось загрузить подраздел.")
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
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean isAvailableForStatus(StudentStatus status) {
        return true;
    }
}
