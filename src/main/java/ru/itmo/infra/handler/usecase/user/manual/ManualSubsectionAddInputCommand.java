package ru.itmo.infra.handler.usecase.user.manual;

import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.exception.InternalException;
import ru.itmo.exception.UnknownUserException;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.storage.GuideRepository;

public class ManualSubsectionAddInputCommand implements Command {

    @Override
    public MessageToUser execute(MessageDTO message) {
        Object raw;
        try {
            raw = ContextHolder.getCommandData(message.getChatId());
        } catch (UnknownUserException e) {
            return MessageToUser.builder()
                    .text("Сессия не найдена. Начните заново: /manual_edit")
                    .needRewriting(true)
                    .keyboardMarkup(Command.returnToStartInlineMarkup())
                    .build();
        }
        if (!(raw instanceof Integer sectionId)) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text("Некорректный контекст. Начните заново: /manual_edit")
                    .needRewriting(true)
                    .keyboardMarkup(Command.returnToStartInlineMarkup())
                    .build();
        }
        String text = message.getText() == null ? "" : message.getText();
        if (text.startsWith("/")) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text("Добавление отменено.")
                    .needRewriting(true)
                    .keyboardMarkup(Command.returnToStartInlineMarkup())
                    .build();
        }
        String title = text.trim();
        if (title.isEmpty()) {
            return MessageToUser.builder()
                    .text("Название не может быть пустым. Введите название подраздела.")
                    .keyboardMarkup(ManualEditAbortCommand.awaitInputMarkup())
                    .build();
        }
        ContextHolder.endCommand(message.getChatId());
        try {
            GuideRepository.insertSubsection(sectionId, title);
            return ManualReorderView.build(sectionId);
        } catch (InternalException e) {
            return MessageToUser.builder()
                    .text("Ошибка при создании подраздела.")
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
        return "";
    }
}
