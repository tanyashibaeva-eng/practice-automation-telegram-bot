package ru.itmo.infra.handler.usecase.user.companyinfoinput;

import lombok.extern.java.Log;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.CompanyApprovalRequestService;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.CompanyInfoUpdateArgs;
import ru.itmo.infra.handler.usecase.user.UserCommand;

@Log
public class SubmitCompanyApprovalRequestCommand implements UserCommand {

    @Override
    public MessageToUser execute(MessageDTO message) {
        try {
            var chatId = message.getChatId();
            var args = (CompanyInfoUpdateArgs) ContextHolder.getCommandData(chatId);
            long requestId = CompanyApprovalRequestService.submitRequest(args);
            ContextHolder.endCommand(chatId);
            return MessageToUser.builder()
                    .text(args.isRequiresSpbOfficeApproval()
                            ? "Заявка на подтверждение офиса компании в Санкт-Петербурге отправлена администратору. Номер заявки: %d.".formatted(requestId)
                            : "Заявка на новую компанию отправлена администратору. Номер заявки: %d.".formatted(requestId))
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();
        } catch (Exception e) {
            log.severe("Не удалось отправить заявку на подтверждение компании: " + e.getMessage());
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text("Не удалось отправить заявку на подтверждение компании: %s".formatted(
                            e.getMessage() == null || e.getMessage().isBlank()
                                    ? "внутренняя ошибка"
                                    : e.getMessage()
                    ))
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }
}
