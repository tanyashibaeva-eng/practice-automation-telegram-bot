package ru.itmo.infra.handler.usecase.admin.companyapproval;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.CompanyApprovalRequestService;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.exception.BadRequestException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.util.TextUtils;

public class ApproveCompanyApprovalRequestCommand implements AdminCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            long requestId = parseRequestId(message.getText(), getName());
            var request = CompanyApprovalRequestService.getPendingRequestOrThrow(requestId);
            var studentName = StudentService.findStudentByChatIdAndEduStreamName(request.getStudentChatId(), request.getEduStreamName())
                    .map(student -> student.getFullName())
                    .orElse("Неизвестный студент");

            ContextHolder.setCommandData(message.getChatId(), requestId);
            ContextHolder.setNextCommand(message.getChatId(), new ApproveCompanyApprovalRequestConfirmationCommand());

            var text = """
                    Подтвердить заявку #%d?
                    Тип: %s
                    Студент: %s
                    Поток: %s
                    Компания: %s
                    ИНН: %d
                    Адрес: %s
                    Руководитель: %s
                    Телефон: %s
                    Email: %s
                    Должность: %s
                    """.formatted(
                    request.getId(),
                    request.isRequiresSpbOfficeApproval()
                            ? "подтверждение офиса в Санкт-Петербурге"
                            : "добавление новой компании",
                    studentName,
                    request.getEduStreamName(),
                    request.getCompanyName(),
                    request.getInn(),
                    request.getCompanyAddress(),
                    request.getCompanyLeadFullName(),
                    request.getCompanyLeadPhone(),
                    request.getCompanyLeadEmail(),
                    request.getCompanyLeadJobTitle()
            );

            return MessageToUser.builder()
                    .text(text.trim())
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .keyboardMarkup(getInlineKeyboard())
                    .build();
        } catch (BadRequestException e) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text(e.getMessage())
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();
        }
    }

    static long parseRequestId(String text, String commandName) throws BadRequestException {
        var normalizedText = TextUtils.removeRedundantSpaces(text);
        int callbackSeparatorIndex = normalizedText.indexOf('#');
        if (callbackSeparatorIndex >= 0) {
            normalizedText = normalizedText.substring(0, callbackSeparatorIndex);
        }
        var fields = normalizedText.split(" +");
        if (fields.length >= 2) {
            return TextUtils.parseDoubleStrToLong(fields[1]);
        }

        var commandPrefix = commandName + "_";
        if (normalizedText.startsWith(commandPrefix) && normalizedText.length() > commandPrefix.length()) {
            return TextUtils.parseDoubleStrToLong(normalizedText.substring(commandPrefix.length()));
        }

        throw new BadRequestException("Неверный формат команды. Используйте: %s_<requestId> или %s <requestId>"
                .formatted(commandName, commandName));
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/approve_company_request";
    }

    @Override
    public String getDescription() {
        return "Подтвердить заявку на компанию. Пример: `/approve_company_request_12`";
    }
}
