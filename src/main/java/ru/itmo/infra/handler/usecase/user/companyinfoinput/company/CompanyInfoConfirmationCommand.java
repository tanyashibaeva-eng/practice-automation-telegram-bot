package ru.itmo.infra.handler.usecase.user.companyinfoinput.company;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.CompanyInfoUpdateArgs;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.infra.handler.usecase.user.UserCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.InfoSubmittedCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.SubmitCompanyApprovalRequestCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.itmo.AskingITMOPracticeLeadFullNameCommand;

public class CompanyInfoConfirmationCommand implements UserCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var dto = (CompanyInfoUpdateArgs) ContextHolder.getCommandData(chatId);

        switch (message.getText()) {
            case "Да":
                ContextHolder.setNextCommand(chatId, shouldSubmitForAdminApproval(dto)
                        ? new SubmitCompanyApprovalRequestCommand()
                        : new InfoSubmittedCommand());
                return MessageToUser.builder()
                        .text("")
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .needRewriting(false)
                        .build();
            case "Нет":
                ContextHolder.setNextCommand(chatId, getRetryCommand(dto));
                return MessageToUser.builder()
                        .text("")
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .build();
            case "Вернуться в меню":
                ContextHolder.endCommand(chatId);
                return MessageToUser.builder()
                        .text("")
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .build();
            default:
                ContextHolder.setNextCommand(chatId, this);
                return MessageToUser.builder()
                        .text("Извините, я вас не понимаю, ответьте \"Да\", \"Нет\" или \"Вернуться в меню\" ")
                        .keyboardMarkup(getInlineKeyboard())
                        .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    private UserCommand getRetryCommand(CompanyInfoUpdateArgs dto) {
        if (dto.getCompanyName() == null || dto.getCompanyName().isBlank()) {
            return new AskingCompanyNameCommand();
        }
        if (dto.getPracticeFormat() != PracticeFormat.ONLINE
                && (dto.getCompanyAddress() == null || dto.getCompanyAddress().isBlank())) {
            return new AskingCompanyAddressCommand();
        }
        return new AskingITMOPracticeLeadFullNameCommand();
    }

    private boolean shouldSubmitForAdminApproval(CompanyInfoUpdateArgs dto) {
        return dto.isRequiresSpbOfficeApproval() || !dto.isPresentInITMOAgreementFile();
    }
}
