package ru.itmo.infra.handler.usecase.admin.filledustream;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.EduStreamService;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.model.EduStream;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.UnknownUserException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.infra.handler.usecase.admin.gotostream.GotoStreamCommand;

public class FillEduStreamCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            var streamName = getEduStreamNameOrThrow(message);
            EduStream stream = new EduStream(streamName);

            if (EduStreamService.findEduStreamByName(stream).isEmpty()) {
                throw new BadRequestException("Поток с таким именем не найден");
            }

            ContextHolder.setNextCommand(message.getChatId(), new FillEduStreamUploadCommand());
            return MessageToUser.builder()
                    .text("Пожалуйста, прикрепите файл с данными студентов для потока")
                    .keyboardMarkup(getReturnToStartMarkup())
                    .needRewriting(true)
                    .build();
        } catch (BadRequestException e) {
            return MessageToUser.builder().text(e.getMessage()).build();

        } catch (
                UnknownUserException e) {
            return returnToMainMenuWithError(message.getChatId(),
                    "Из-за внешних обстоятельств контекст был утерян. Пожалуйста, повторите действие еще раз");
        }
    }

    @Override
    public ReplyKeyboard getReturnToStartMarkup() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(
                        new InlineKeyboardRow(
                                InlineKeyboardButton.builder()
                                        .text(returnIcon + " Вернуться в меню")
                                        .callbackData(
                                                CallbackData.builder()
                                                        .command(new GotoStreamCommand().getName())
                                                        .build()
                                                        .toString()
                                        ).build()
                        )).build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/fill_edu_stream";
    }

    @Override
    public String getDescription() {
        return "Загрузить новых студентов в поток. Пример: `/fill_edu_stream Бакалавры 2025`";
    }
}