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

public class RejectCompanyApprovalRequestCommand implements AdminCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            long requestId = ApproveCompanyApprovalRequestCommand.parseRequestId(message.getText(), getName());
            var request = CompanyApprovalRequestService.getPendingRequestOrThrow(requestId);
            var studentName = StudentService.findStudentByChatIdAndEduStreamName(request.getStudentChatId(), request.getEduStreamName())
                    .map(student -> student.getFullName())
                    .orElse("Неизвестный студент");

            ContextHolder.setCommandData(message.getChatId(), requestId);
            ContextHolder.setNextCommand(message.getChatId(), new RejectCompanyApprovalRequestConfirmationCommand());

            return MessageToUser.builder()
                    .text("%s заявку #%d?\nСтудент: %s\nПоток: %s\nКомпания: %s\nИНН: %d\nАдрес: %s".formatted(
                            request.isRequiresSpbOfficeApproval()
                                    ? "Отклонить подтверждение офиса в Санкт-Петербурге для"
                                    : "Отклонить",
                            request.getId(),
                            studentName,
                            request.getEduStreamName(),
                            request.getCompanyName(),
                            request.getInn(),
                            request.getCompanyAddress()
                    ))
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

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/reject_company_request";
    }

    @Override
    public String getDescription() {
        return "Отклонить заявку на компанию. Пример: `/reject_company_request_12`";
    }
}
