package ru.itmo.infra.handler.usecase.user.companyinfoinput;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.CompanyInfoUpdateArgs;
import ru.itmo.domain.dto.command.ITMOPracticeInfoUpdateArgs;
import ru.itmo.infra.handler.usecase.user.UserCommand;

public class InfoSubmittedCommand implements UserCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var dto = ContextHolder.getCommandData(message.getChatId());
        var res = true;
        if (dto instanceof ITMOPracticeInfoUpdateArgs) {
            res = StudentService.updateITMOPracticeInfo((ITMOPracticeInfoUpdateArgs) dto);
        } else {
            res = StudentService.updateCompanyInfo((CompanyInfoUpdateArgs) dto);
        }

        if (!res) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text("Не удалось обновить данные о компании, попробуйте в снова")
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();
        }

        ContextHolder.endCommand(message.getChatId());
        return MessageToUser.builder()
                .text("Спасибо за информацию! Данные о практике отправлены на проверку преподавателю")
                .keyboardMarkup(new ReplyKeyboardRemove(true))
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }
}