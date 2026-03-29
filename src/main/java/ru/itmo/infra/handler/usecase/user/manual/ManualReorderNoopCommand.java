package ru.itmo.infra.handler.usecase.user.manual;

import lombok.NoArgsConstructor;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.infra.handler.usecase.user.UserCommand;

@NoArgsConstructor
public class ManualReorderNoopCommand implements UserCommand {

    public static final String COMMAND_NAME = "/manual_r_no";

    @Override
    public MessageToUser execute(MessageDTO message) {
        CallbackData cd = new CallbackData(message.getText());
        if ("edge".equals(cd.getKey())) {
            String hint = switch (cd.getValue() == null ? "" : cd.getValue()) {
                case "up" -> "Это первый пункт — выше не поднять.";
                case "down" -> "Это последний пункт — ниже не опустить.";
                default -> null;
            };
            if (hint != null) {
                return MessageToUser.builder()
                        .skipMessageEdit(true)
                        .callbackAnswerText(hint)
                        .build();
            }
        }
        return MessageToUser.builder()
                .skipMessageEdit(true)
                .callbackAnswerText("—")
                .build();
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
