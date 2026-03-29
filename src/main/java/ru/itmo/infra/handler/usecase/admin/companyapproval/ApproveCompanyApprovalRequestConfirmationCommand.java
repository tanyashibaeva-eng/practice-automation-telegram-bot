package ru.itmo.infra.handler.usecase.admin.companyapproval;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.CompanyApprovalRequestService;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

public class ApproveCompanyApprovalRequestConfirmationCommand implements AdminCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var requestId = (Long) ContextHolder.getCommandData(chatId);

        switch (message.getText()) {
            case "Да":
                CompanyApprovalRequestService.approveRequest(requestId, chatId);
                ContextHolder.endCommand(chatId);
                return MessageToUser.builder()
                        .text("Заявка #%d подтверждена".formatted(requestId))
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .needRewriting(false)
                        .build();
            case "Нет":
                ContextHolder.endCommand(chatId);
                return MessageToUser.builder()
                        .text("Подтверждение заявки отменено")
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
