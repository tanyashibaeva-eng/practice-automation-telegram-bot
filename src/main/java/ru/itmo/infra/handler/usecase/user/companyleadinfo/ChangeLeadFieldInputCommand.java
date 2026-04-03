package ru.itmo.infra.handler.usecase.user.companyleadinfo;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.exception.UnknownUserException;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.util.TextUtils;

public class ChangeLeadFieldInputCommand implements Command {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var text = message.getText() == null ? "" : message.getText().trim();

        LeadInfoField field;
        try {
            field = (LeadInfoField) ContextHolder.getCommandData(chatId);
        } catch (UnknownUserException | ClassCastException e) {
            ContextHolder.endCommand(chatId);
            return MessageToUser.builder()
                    .text("Ошибка: не удалось определить поле для изменения")
                    .keyboardMarkup(Command.returnToStartInlineMarkup())
                    .needRewriting(true)
                    .build();
        }

        if (text.isEmpty()) {
            ContextHolder.setNextCommand(chatId, this);
            return MessageToUser.builder()
                    .text("Значение не может быть пустым. Попробуйте ещё раз:")
                    .build();
        }

        try {
            String newValue = validateAndPrepareValue(chatId, field, text);
            StudentService.updateCompanyLeadField(chatId, field.getColumn(), newValue);
            ContextHolder.endCommand(chatId);
            return MessageToUser.builder()
                    .text(field.getDisplayName() + " успешно обновлён(а)")
                    .keyboardMarkup(Command.returnToStartInlineMarkup())
                    .build();
        } catch (BadRequestException e) {
            ContextHolder.setNextCommand(chatId, this);
            return MessageToUser.builder()
                    .text(e.getMessage() + "\nПопробуйте ещё раз:")
                    .build();
        } catch (InternalException e) {
            ContextHolder.endCommand(chatId);
            return MessageToUser.builder()
                    .text("Не удалось обновить данные из-за технической ошибки")
                    .keyboardMarkup(Command.returnToStartInlineMarkup())
                    .build();
        }
    }

    private String validateAndPrepareValue(long chatId, LeadInfoField field, String text)
            throws BadRequestException, InternalException {
        return switch (field) {
            case PHONE -> TextUtils.parsePhone(text);
            case EMAIL -> TextUtils.parseEmail(text);
            case FULLNAME, JOB_TITLE -> text;
            case LASTNAME, FIRSTNAME, PATRONYMIC -> buildUpdatedFullName(chatId, field, text);
        };
    }

    static String buildUpdatedFullName(long chatId, LeadInfoField field, String newPart)
            throws InternalException, BadRequestException {
        var studentOpt = StudentService.getStudentWithLeadInfo(chatId);
        if (studentOpt.isEmpty()) {
            throw new BadRequestException("Студент не найден в активном потоке");
        }
        var currentFullName = studentOpt.get().getCompanyLeadFullName();
        if (currentFullName == null || currentFullName.isBlank()) {
            throw new BadRequestException("Текущее ФИО руководителя не задано. Используйте изменение полного ФИО");
        }
        var parts = currentFullName.trim().split("\\s+");
        if (parts.length < 3) {
            throw new BadRequestException(
                    "Текущее ФИО руководителя (\"%s\") не содержит 3 частей. Используйте изменение полного ФИО"
                            .formatted(currentFullName));
        }
        return switch (field) {
            case LASTNAME -> newPart + " " + parts[1] + " " + parts[2];
            case FIRSTNAME -> parts[0] + " " + newPart + " " + parts[2];
            case PATRONYMIC -> parts[0] + " " + parts[1] + " " + newPart;
            default -> currentFullName;
        };
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }
}
