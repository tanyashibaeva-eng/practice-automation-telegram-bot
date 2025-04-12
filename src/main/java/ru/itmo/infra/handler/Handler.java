package ru.itmo.infra.handler;

import lombok.extern.java.Log;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.itmo.application.AuthorizationService;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.bot.PracticeAutomationBot;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.InvalidMessageException;
import ru.itmo.exception.UnknownUserException;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.admin.addAdmin.AddAdminCommand;
import ru.itmo.infra.handler.usecase.admin.ban.BanCommand;
import ru.itmo.infra.handler.usecase.admin.createedustream.CreateEduStreamStartCommand;
import ru.itmo.infra.handler.usecase.admin.downloadapplication.DownloadApplicationCommand;
import ru.itmo.infra.handler.usecase.admin.exportexcel.ExportExcelExportCommand;
import ru.itmo.infra.handler.usecase.admin.uploadexcel.UploadExcelStartCommand;
import ru.itmo.infra.handler.usecase.start.StartCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.ChoosePracticePlaceCommand;
import ru.itmo.infra.handler.usecase.user.studentapplicationinput.StudentDownloadApplicationCommand;
import ru.itmo.infra.handler.usecase.user.studentapplicationinput.UnloadApplicationCommand;
import ru.itmo.infra.handler.usecase.user.studentregistration.StudentRegistrationStartCommand;
import ru.itmo.infra.handler.usecase.user.studentstatus.StatusCommand;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.itmo.exception.InvalidMessageException.ThrowDocumentException;

@Log
public class Handler {

    private static final TelegramClient telegramClient = PracticeAutomationBot.getTelegramClient();
    private static final List<Command> commands = new ArrayList<>();
    private static final Map<String, Command> commandsMap = new HashMap<>();

    static {
        commands.add(new StartCommand());
        commands.add(new UploadExcelStartCommand());
        commands.add(new ExportExcelExportCommand());
        commands.add(new CreateEduStreamStartCommand());
        commands.add(new StudentRegistrationStartCommand());
        commands.add(new ChoosePracticePlaceCommand());
        commands.add(new StudentDownloadApplicationCommand());
        commands.add(new UnloadApplicationCommand());
        commands.add(new AddAdminCommand());
        commands.add(new StatusCommand());
        commands.add(new BanCommand());
        commands.add(new DownloadApplicationCommand());

        for (Command command : commands) {
            if (command.getName().isEmpty()) {
                continue;
            }
            commandsMap.put(command.getName(), command);
        }
    }

    public static void updateCommandsDropOut(long chatId) {
        try {
            List<BotCommand> userCommands = new ArrayList<>();
            if (!AuthorizationService.canDoAdminActions(chatId)) {
                var studentOpt = StudentService.findStudentByChatIdAndEduStreamName(chatId, "2");
                if (studentOpt.isEmpty()) {
                    return;
                }
                var student = studentOpt.get();
                userCommands = getStudentsCommandsDropOut(student.getStatus());
            } else {
                userCommands = getAdminCommandsDropOut();
            }
            setCommandsForUser(chatId, userCommands);
        } catch (Exception e) {
            log.warning("Ошибка обновления команд для " + chatId + ": " + e.getMessage());
        }
    }

    private static List<BotCommand> getStudentsCommandsDropOut(StudentStatus status) {
        List<BotCommand> resultCommands = new ArrayList<>();

        addCommandIfExists(resultCommands, new StartCommand());
        addCommandIfExists(resultCommands, new StatusCommand());

        switch (status) {
            case REGISTERED, COMPANY_INFO_RETURNED:
                addCommandIfExists(resultCommands, new ChoosePracticePlaceCommand());
                break;
            case COMPANY_INFO_WAITING_APPROVAL, PRACTICE_APPROVED, APPLICATION_WAITING_SUBMISSION,
                 APPLICATION_WAITING_APPROVAL, APPLICATION_RETURNED:
                addCommandIfExists(resultCommands, new DownloadApplicationCommand());
                break;
            case APPLICATION_SIGNED:
                addCommandIfExists(resultCommands, new UnloadApplicationCommand());
                break;
        }
        return resultCommands;
    }

    private static List<BotCommand> getAdminCommandsDropOut() {
        List<BotCommand> resultCommands = new ArrayList<>();

        addCommandIfExists(resultCommands, new StartCommand());
        addCommandIfExists(resultCommands, new ExportExcelExportCommand());

        return resultCommands;
    }

    private static void addCommandIfExists(List<BotCommand> commands, Command command) {
        commands.add(new BotCommand(command.getName(), command.getDescription()));
    }

    private static void setCommandsForUser(long chatId, List<BotCommand> commands) {
        try {
            SetMyCommands setCommands = new SetMyCommands(
                    commands,
                    new BotCommandScopeChat(String.valueOf(chatId)),
                    null
            );
            telegramClient.execute(setCommands);
        } catch (TelegramApiException e) {
            log.severe("Ошибка установки команд: " + e.getMessage());
        }
    }

    public static MessageToUser handleMessage(MessageDTO message) throws Exception {
        var nextFunc = getNextCommandFunction(message.getChatId());

        if (nextFunc != null) {
            return executeCommand(nextFunc, message);
        }

        var commandText = message.getText();
        var commandName = commandText.split(" ")[0];
        if (!commandsMap.containsKey(commandName)) {
            return MessageToUser.builder().text("Извините, но я не понимаю такую команду. Попробуйте другую или напишите \"/help\" для помощи").build();
        }

        var command = commandsMap.get(commandName);
        return executeCommand(command, message);
    }

    private static Command getNextCommandFunction(long chatId) {
        try {
            return ContextHolder.getNextCommand(chatId);
        } catch (UnknownUserException e) {
            return null;
        }
    }

    private static MessageToUser executeCommand(Command command, MessageDTO message) throws UnknownUserException {
        var response = command.execute(message);

        var nextCommand = getNextCommandFunction(message.getChatId());
        if (nextCommand == null && !command.getName().equals("/start")) {
            nextCommand = new StartCommand();
            ContextHolder.setNextCommand(message.getChatId(), nextCommand);
        }

        if (command.isNextCallNeeded() && nextCommand != null && !command.getName().equals(nextCommand.getName())) {
            PracticeAutomationBot.sendToUser(response, message.getChatId(), false);
            response = executeCommand(nextCommand, message);
        }

        return response;
    }

    public static MessageToUser handleCallback(MessageDTO message, String callbackDataString) throws Exception {
        var callbackData = new CallbackData(callbackDataString);
        if (callbackData.getKey() != null) {
            mapKeyToFunc(message.getChatId(), callbackData.getKey(), callbackData.getValue());
        }
        return commandsMap.get(callbackData.getCommand()).execute(message);
    }

    public static File getFileFromMessage(MessageDTO message) throws TelegramApiException, InvalidMessageException {
        if (!message.hasDocument()) {
            ThrowDocumentException();
        }

        var fileId = message.getDocument().getFileId();
        var fileName = message.getDocument().getFileName();
        var fileMimeType = message.getDocument().getMimeType();
        var fileSize = message.getDocument().getFileSize();

        var document = new Document();
        document.setMimeType(fileMimeType);
        document.setFileName(fileName);
        document.setFileSize(fileSize);
        document.setFileId(fileId);

        var getFile = new GetFile(fileId);
        getFile.setFileId(document.getFileId());

        var tgFile = telegramClient.execute(getFile);
        return telegramClient.downloadFile(tgFile).toPath().toFile();
    }

    public static void mapKeyToFunc(Long chatId, String key, String value) throws UnknownUserException {
        switch (key) {
            case "eduStreamName":
                ContextHolder.setEduStreamName(chatId, value);
            default:
        }
    }
}
