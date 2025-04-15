package ru.itmo.infra.handler.usecase.admin.filledustream;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.model.EduStream;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.UnknownUserException;
import ru.itmo.infra.handler.Handler;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.infra.handler.usecase.admin.gotostream.GotoStreamCommand;

import java.io.File;

public class FillEduStreamUploadCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            String streamName;
            streamName = ContextHolder.getEduStreamName(message.getChatId());

            if (!message.hasDocument()) {
                throw new BadRequestException("Пожалуйста, прикрепите файл с группой из ИСУ");
            }

            File file = Handler.getFileFromMessage(message);
            EduStream eduStream = new EduStream(streamName);

            String errors = StudentService.createStudentsFromExcel(file, eduStream.getName());
            if (!errors.isEmpty()) {
                throw new BadRequestException("В загруженном файле содержатся ошибки:\n" + errors);
            }

            ContextHolder.setNextCommand(message.getChatId(), new FillEduStreamMoreFilesCommand());
            return MessageToUser.builder()
                    .text("Группа была добавлена в поток \"%s\". Хотите загрузить еще один файл?".formatted(streamName))
                    .keyboardMarkup(getConfirmationKeyboard())
                    .build();

        } catch (BadRequestException e) {
            ContextHolder.setNextCommand(message.getChatId(), this);
            return MessageToUser.builder()
                    .text(e.getMessage())
                    .needRewriting(true)
                    .keyboardMarkup(getReturnToStartMarkup())
                    .build();
        } catch (UnknownUserException e) {
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
}