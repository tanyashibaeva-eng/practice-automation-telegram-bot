package ru.itmo.infra.handler.usecase.admin.uploadexcel;

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
import ru.itmo.infra.handler.Handler;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.infra.handler.usecase.admin.gotostream.GotoStreamCommand;

public class UploadExcelHandleCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var file = Handler.getFileFromMessage(message);

        var streamName = getEduStreamNameOrThrow(message);

        EduStream stream;
        try {
            stream = new EduStream(streamName);
        } catch (BadRequestException ex) {
            return returnToMainMenuWithError(message.getChatId(),
                    "Из-за внешних обстоятельств контекст был утерян. Пожалуйста, повторите действие еще раз");
        }

        var res = StudentService.updateStudentsFromExcel(file, stream.getName());
        if (res.isEmpty()) {
            ContextHolder.setNextCommand(message.getChatId(), new GotoStreamCommand());
            return MessageToUser.builder().text("Файл был успешно загружен").build();
        }
        return MessageToUser.builder()
                .text("В загруженном файле содержатся ошибки, поправьте их и попробуйте снова или вернитесь назад.")
                .keyboardMarkup(getReturnToStartMarkup())
                .document(res.get())
                .build();
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
        return true;
    }
}
