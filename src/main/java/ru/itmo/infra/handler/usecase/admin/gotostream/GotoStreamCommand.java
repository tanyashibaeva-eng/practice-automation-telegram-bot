package ru.itmo.infra.handler.usecase.admin.gotostream;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.Handler;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.infra.handler.usecase.admin.deletestream.DeleteStreamCommand;
import ru.itmo.infra.handler.usecase.admin.exportexcel.ExportExcelCommand;
import ru.itmo.infra.handler.usecase.admin.filledustream.FillEduStreamCommand;
import ru.itmo.infra.handler.usecase.admin.updategroup.UpdateGroupStudentsCommand;
import ru.itmo.infra.handler.usecase.admin.uploadexcel.UploadExcelCommand;

@NoArgsConstructor
public class GotoStreamCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        Handler.updateCommandsDropOut(message.getChatId());
        ContextHolder.setNextCommand(message.getChatId(), this);
        String streamName = ContextHolder.getEduStreamName(message.getChatId());
        return MessageToUser.builder()
                .text("Какое действие с потоком '" + streamName + "' хотите совершить?")
                .keyboardMarkup(getActionsKeyboard())
                .needRewriting(true)
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/goto_stream_menu";
    }

    private static ReplyKeyboard getActionsKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(getIcon + " Получить выгрузку по студентам")
                                .callbackData(
                                        CallbackData.builder()
                                                .command(new ExportExcelCommand().getName())
                                                .build()
                                                .toString()
                                ).build()
                ))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(uploadIcon + " Загрузить обновленный файл со студентами")
                                .callbackData(
                                        CallbackData.builder()
                                                .command(new UploadExcelCommand().getName())
                                                .build()
                                                .toString()
                                ).build()
                ))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(addIcon + " Добавить группы в поток")
                                .callbackData(
                                        CallbackData.builder()
                                                .command(new FillEduStreamCommand().getName())
                                                .build()
                                                .toString()
                                ).build()
                ))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(RemoveIcon + " Удалить поток")
                                .callbackData(
                                        CallbackData.builder()
                                                .command(new DeleteStreamCommand().getName())
                                                .build()
                                                .toString()
                                ).build()
                ))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(updateIcon + " Обновить список студентов в группе")
                                .callbackData(
                                        CallbackData.builder()
                                                .command(new UpdateGroupStudentsCommand().getName())
                                                .build()
                                                .toString()
                                ).build()
                ))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(returnIcon + " Назад к списку потоков")
                                .callbackData(
                                        CallbackData.builder()
                                                .command("/start")
                                                .build()
                                                .toString()
                                ).build()
                ))
                .build();
    }
}
