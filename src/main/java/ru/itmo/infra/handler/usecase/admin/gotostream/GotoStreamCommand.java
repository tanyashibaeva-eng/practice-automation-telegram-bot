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
import ru.itmo.infra.handler.usecase.admin.addadmin.AddAdminCommand;
import ru.itmo.infra.handler.usecase.admin.ban.BanCommand;
import ru.itmo.infra.handler.usecase.admin.deletestream.DeleteStreamCommand;
import ru.itmo.infra.handler.usecase.admin.downloadapplication.DownloadApplicationCommand;
import ru.itmo.infra.handler.usecase.admin.exportexcel.ExportExcelCommand;
import ru.itmo.infra.handler.usecase.admin.filledustream.FillEduStreamCommand;
import ru.itmo.infra.handler.usecase.admin.forceupdate.ForceUpdateCommand;
import ru.itmo.infra.handler.usecase.admin.initedustream.InitEduStreamCommand;
import ru.itmo.infra.handler.usecase.admin.mentor.CreateAdminFromUserCommand;
import ru.itmo.infra.handler.usecase.admin.pingstudents.PingStudentsCommand;
import ru.itmo.infra.handler.usecase.admin.studentinfo.GetStudentInfoCommand;
import ru.itmo.infra.handler.usecase.admin.unban.ban.UnbanCommand;
import ru.itmo.infra.handler.usecase.admin.uploadexcel.UploadExcelCommand;

@NoArgsConstructor
public class GotoStreamCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        ContextHolder.setNextCommand(message.getChatId(), this);
        String streamName = ContextHolder.getEduStreamName(message.getChatId());
        if (message.hasText() && isNotStreamInnerCommand(message.getText())) {
            ContextHolder.endCommand(message.getChatId());
            return Handler.handleMessage(message);
        }
        return MessageToUser.builder()
                .text("Какое действие с потоком '" + streamName + "' хотите совершить?")
                .keyboardMarkup(getActionsKeyboard())
                .needRewriting(true)
                .build();
    }

    private boolean isNotStreamInnerCommand(String commandName) {
        return commandName.startsWith(new AddAdminCommand().getName()) ||
                commandName.startsWith(new DownloadApplicationCommand().getName()) ||
                commandName.startsWith(new ForceUpdateCommand().getName()) ||
                commandName.startsWith(new InitEduStreamCommand().getName()) ||
                commandName.startsWith(new PingStudentsCommand().getName()) ||
                commandName.startsWith(new CreateAdminFromUserCommand().getName()) ||
                commandName.startsWith(new GetStudentInfoCommand().getName()) ||
                commandName.startsWith(new BanCommand().getName()) ||
                commandName.startsWith(new UnbanCommand().getName());
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
