package ru.itmo.infra.handler;

import lombok.extern.java.Log;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.bot.PracticeAutomationBot;
import ru.itmo.exception.InvalidMessageException;
import ru.itmo.exception.UnknownUserException;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.createedustream.CreateEduStreamStartCommand;
import ru.itmo.infra.handler.usecase.exportexcel.ExportExcelExportCommand;
import ru.itmo.infra.handler.usecase.greeting.GreetingCommand;
import ru.itmo.infra.handler.usecase.studentregistration.StudentRegistrationStartCommand;
import ru.itmo.infra.handler.usecase.uploadexcel.UploadExcelStartCommand;

import java.io.File;
import java.io.IOException;
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
        commands.add(new GreetingCommand());
        commands.add(new GreetingCommand());
        commands.add(new UploadExcelStartCommand());
        commands.add(new ExportExcelExportCommand());
        commands.add(new StudentRegistrationStartCommand());
        commands.add(new CreateEduStreamStartCommand());
//        commands.put("/showEduStreamInfo", ShowEduStreamInfo::start);
//        commands.put("/registration", StudentRegistration::startRegistration);

        for (Command command : commands) {
            if (command.getName().isEmpty()) {
                continue;
            }
            commandsMap.put(command.getName(), command);
        }
    }

    public static MessageToUser handleMessage(MessageDTO message) throws Exception {
        var nextFunc = getNextCommandFunction(message.getChatId());

        if (nextFunc != null) {
            return executeCommand(nextFunc, message);
        }

        var commandText = message.getText();
        if (!commandsMap.containsKey(commandText)) {
            return MessageToUser.builder().text("Извините, но я не понимаю такую команду. Попробуйте другую или напишите \"/help\" для помощи").build();
        }

        var command = commandsMap.get(commandText);
        return executeCommand(command, message);
    }

    private static Command getNextCommandFunction(long chatId) {
        try {
            return ContextHolder.getNextCommand(chatId);
        } catch (UnknownUserException e) {
            return null;
        }
    }

    private static MessageToUser executeCommand(Command command, MessageDTO message) {
        var response = command.execute(message);

        var nextCommand = getNextCommandFunction(message.getChatId());
        if (nextCommand == null && !command.getName().equals("/start")) {
            nextCommand = new GreetingCommand();
            ContextHolder.setNextCommand(message.getChatId(), nextCommand);
        }

        if (command.isNextCallNeeded() && nextCommand != null && !command.getName().equals(nextCommand.getName())) {
            PracticeAutomationBot.sendToUser(response, message.getChatId());
            return nextCommand.execute(message);
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

    public static File getFileFromMessage(MessageDTO message) throws TelegramApiException, IOException, InvalidMessageException {
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
