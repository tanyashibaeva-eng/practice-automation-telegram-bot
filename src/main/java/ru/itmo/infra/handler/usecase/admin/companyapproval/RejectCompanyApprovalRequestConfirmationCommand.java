package ru.itmo.infra.handler.usecase.admin.companyapproval;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.CompanyApprovalRequestService;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

public class RejectCompanyApprovalRequestConfirmationCommand implements AdminCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var requestId = (Long) ContextHolder.getCommandData(chatId);

        switch (message.getText()) {
            case "Да":
                var request = CompanyApprovalRequestService.getPendingRequestOrThrow(requestId);
                StudentService.updateStatusByChatIdAndEduStreamName(
                        request.getStudentChatId(),
                        request.getEduStreamName(),
                        StudentStatus.COMPANY_INFO_RETURNED
                );
                CompanyApprovalRequestService.rejectRequest(requestId, chatId);
                ContextHolder.endCommand(chatId);
                return MessageToUser.builder()
                        .text("Заявка #%d отклонена".formatted(requestId))
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .needRewriting(false)
                        .build();
            case "Нет":
                ContextHolder.endCommand(chatId);
                return MessageToUser.builder()
                        .text("Отклонение заявки отменено")
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .build();
            default:
                ContextHolder.setNextCommand(chatId, this);
                return MessageToUser.builder()
                        .text("Ответьте \"Да\" или \"Нет\"")
                        .keyboardMarkup(getConfirmationKeyboard())
                        .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }
}
