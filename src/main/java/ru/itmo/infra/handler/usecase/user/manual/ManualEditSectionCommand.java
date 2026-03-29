package ru.itmo.infra.handler.usecase.user.manual;

import lombok.NoArgsConstructor;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.user.UserCommand;

@NoArgsConstructor
public class ManualEditSectionCommand implements UserCommand {

    public static final String COMMAND_NAME = "/manual_ed_sec";

    @Override
    public MessageToUser execute(MessageDTO message) {
        var cd = new CallbackData(message.getText());
        if (!"sectionId".equals(cd.getKey()) || cd.getValue() == null || cd.getValue().isBlank()) {
            return MessageToUser.builder()
                    .text("Некорректный выбор раздела.")
                    .needRewriting(true)
                    .build();
        }
        try {
            int sectionId = Integer.parseInt(cd.getValue().trim());
            return ManualReorderView.build(sectionId);
        } catch (NumberFormatException e) {
            return MessageToUser.builder()
                    .text("Некорректный идентификатор раздела.")
                    .needRewriting(true)
                    .build();
        } catch (InternalException e) {
            return MessageToUser.builder()
                    .text("Не удалось загрузить подразделы.")
                    .needRewriting(true)
                    .keyboardMarkup(Command.returnToStartInlineMarkup())
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
