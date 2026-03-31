package ru.itmo.infra.handler.usecase.user.manual;

import ru.itmo.application.ContextHolder;
import ru.itmo.application.manual.ManualEditContext;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.exception.UnknownUserException;
import ru.itmo.infra.handler.usecase.Command;

public class ManualEditPreviewGuardCommand implements Command {

    @Override
    public MessageToUser execute(MessageDTO message) {
        try {
            Object raw = ContextHolder.getCommandData(message.getChatId());
            ManualEditContext ctx = ManualEditContext.fromCommandData(raw);
            if (ctx != null && ctx.phase() == ManualEditContext.Phase.AWAIT_PREVIEW_CONFIRM) {
                return MessageToUser.builder()
                        .text("Используйте кнопки «Подтвердить» или «Отменить» под сообщением с предпросмотром.")
                        .parseMode("HTML")
                        .needRewriting(true)
                        .build();
            }
        } catch (UnknownUserException ignored) {
        }
        ContextHolder.endCommand(message.getChatId());
        return MessageToUser.builder()
                .text("Сессия редактирования устарела. /manual_edit")
                .needRewriting(true)
                .keyboardMarkup(Command.returnToStartInlineMarkup())
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "";
    }
}
