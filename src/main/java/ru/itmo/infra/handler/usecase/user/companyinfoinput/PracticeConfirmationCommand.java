package ru.itmo.infra.handler.usecase.user.companyinfoinput;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.PracticeOptionService;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.CompanyInfoUpdateArgs;
import ru.itmo.domain.dto.command.ITMOPracticeInfoUpdateArgs;
import ru.itmo.domain.type.PracticePlace;
import ru.itmo.infra.handler.usecase.user.UserCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.company.AskingPracticeFormatCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.itmo.AskingITMOPracticeLeadFullNameCommand;

import static ru.itmo.infra.handler.usecase.user.companyinfoinput.ChoosePracticePlaceCommand.getPracticePlaceKeyboard;

public class PracticeConfirmationCommand implements UserCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        if ("Вернуться в меню".equals(message.getText())) {
            ContextHolder.endCommand(chatId);
            return MessageToUser.builder()
                    .text("")
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();
        }

        try {
            var selectedOption = PracticeOptionService.getEnabledOptionByTitleChecked(message.getText());
            StudentService.choosePracticeOption(chatId, selectedOption);
            if (selectedOption.isRequiresItmoInfo()) {
                ContextHolder.setNextCommand(chatId, new AskingITMOPracticeLeadFullNameCommand());
                ContextHolder.setCommandData(chatId, ITMOPracticeInfoUpdateArgs.builder()
                        .chatId(chatId)
                        .practicePlace(PracticePlace.ITMO_UNIVERSITY)
                        .build()
                );
                return MessageToUser.builder()
                        .text("")
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .build();
            }
            if (selectedOption.isRequiresCompanyInfo()) {
                ContextHolder.setNextCommand(chatId, new AskingPracticeFormatCommand());
                ContextHolder.setCommandData(chatId, CompanyInfoUpdateArgs.builder()
                        .chatId(chatId)
                        .build()
                );
                return MessageToUser.builder()
                        .text("")
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .build();
            }

                ContextHolder.setNextCommand(chatId, new InfoSubmittedCommand());
                ContextHolder.setCommandData(chatId, ITMOPracticeInfoUpdateArgs.builder()
                        .chatId(chatId)
                        .practicePlace(PracticePlace.ITMO_MARKINA)
                        .companyName("ИТМО")
                        .companyLeadFullName("Маркина Татьяна Анатольевна")
                        .build()
                );
                return MessageToUser.builder()
                        .text("")
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .build();
        } catch (Exception e) {
            ContextHolder.setNextCommand(chatId, this);
            return MessageToUser.builder()
                    .text("Извините, я вас не понимаю. Выберите вариант места практики из списка или \"Вернуться в меню\"")
                    .keyboardMarkup(getPracticePlaceKeyboard())
                    .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }
}
