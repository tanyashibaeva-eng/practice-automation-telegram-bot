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
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.exception.InvalidMessageException;
import ru.itmo.exception.UnknownUserException;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.admin.addadmin.AddAdminCommand;
import ru.itmo.infra.handler.usecase.admin.ban.BanCommand;
import ru.itmo.infra.handler.usecase.admin.ban.BanConfirmationCommand;
import ru.itmo.infra.handler.usecase.admin.banadmin.BanAdminCommand;
import ru.itmo.infra.handler.usecase.admin.banadmin.BanAdminConfirmationCommand;
import ru.itmo.infra.handler.usecase.admin.companyapproval.ApproveCompanyApprovalRequestCommand;
import ru.itmo.infra.handler.usecase.admin.companyapproval.ApproveCompanyApprovalRequestConfirmationCommand;
import ru.itmo.infra.handler.usecase.admin.companyapproval.ListCompanyApprovalRequestsCommand;
import ru.itmo.infra.handler.usecase.admin.companyapproval.RejectCompanyApprovalRequestCommand;
import ru.itmo.infra.handler.usecase.admin.companyapproval.RejectCompanyApprovalRequestConfirmationCommand;
import ru.itmo.infra.handler.usecase.admin.configureexport.ConfigureExportCommand;
import ru.itmo.infra.handler.usecase.admin.configureexport.FinishColumnsCommand;
import ru.itmo.infra.handler.usecase.admin.configureexport.ToggleColumnCommand;
import ru.itmo.infra.handler.usecase.admin.deletestream.DeleteStreamCommand;
import ru.itmo.infra.handler.usecase.admin.deletestream.DeleteStreamConfirmationCommand;
import ru.itmo.infra.handler.usecase.admin.downloadapplication.DownloadApplicationCommand;
import ru.itmo.infra.handler.usecase.admin.exportexcel.ExportExcelCommand;
import ru.itmo.infra.handler.usecase.admin.filledustream.FillEduStreamCommand;
import ru.itmo.infra.handler.usecase.admin.filledustream.FillEduStreamMoreFilesCommand;
import ru.itmo.infra.handler.usecase.admin.filledustream.FillEduStreamUploadCommand;
import ru.itmo.infra.handler.usecase.admin.forceupdate.ForceUpdateCommand;
import ru.itmo.infra.handler.usecase.admin.forceupdate.ForceUpdateConfirmationCommand;
import ru.itmo.infra.handler.usecase.admin.getbanned.GetBannedCommand;
import ru.itmo.infra.handler.usecase.admin.gotostream.GotoStreamCommand;
import ru.itmo.infra.handler.usecase.admin.initedustream.InitEduInputStreamNameCommand;
import ru.itmo.infra.handler.usecase.admin.initedustream.InitEduInputStreamStartDateCommand;
import ru.itmo.infra.handler.usecase.admin.initedustream.InitEduStreamCommand;
import ru.itmo.infra.handler.usecase.admin.initedustream.InitEduStreamEndDateCommand;
import ru.itmo.infra.handler.usecase.admin.listadmins.ListAdminsCommand;
import ru.itmo.infra.handler.usecase.admin.mentor.CreateAdminFromUserCommand;
import ru.itmo.infra.handler.usecase.admin.pingstudents.PingStudentsCommand;
import ru.itmo.infra.handler.usecase.admin.practiceoption.AddPracticeOptionCommand;
import ru.itmo.infra.handler.usecase.admin.practiceoption.DeletePracticeOptionCommand;
import ru.itmo.infra.handler.usecase.admin.practiceoption.ListPracticeOptionsCommand;
import ru.itmo.infra.handler.usecase.admin.practiceoption.RenamePracticeOptionCommand;
import ru.itmo.infra.handler.usecase.admin.practiceoption.SetPracticeOptionFlagsCommand;
import ru.itmo.infra.handler.usecase.admin.practiceoption.TogglePracticeOptionCommand;
import ru.itmo.infra.handler.usecase.admin.practiceformat.CreatePracticeFormatCommand;
import ru.itmo.infra.handler.usecase.admin.practiceformat.DeletePracticeFormatCommand;
import ru.itmo.infra.handler.usecase.admin.practiceformat.RenamePracticeFormatCommand;
import ru.itmo.infra.handler.usecase.admin.practiceformat.SetUserPracticeFormatCommand;
import ru.itmo.infra.handler.usecase.admin.studentinfo.GetStudentInfoCommand;
import ru.itmo.infra.handler.usecase.admin.unban.UnbanCommand;
import ru.itmo.infra.handler.usecase.admin.unban.UnbanConfirmationCommand;
import ru.itmo.infra.handler.usecase.admin.uploadexcel.UploadExcelCommand;
import ru.itmo.infra.handler.usecase.admin.uploadexcel.UploadExcelHandleCommand;
import ru.itmo.infra.handler.usecase.admin.updategroup.UpdateGroupStudentsCommand;
import ru.itmo.infra.handler.usecase.admin.viewstudentphoto.ViewStudentPhotoCommand;
import ru.itmo.infra.handler.usecase.help.HelpCommand;
import ru.itmo.infra.handler.usecase.start.StartCommand;
import ru.itmo.infra.handler.usecase.user.UserCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.ChangePracticePlaceCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.ChoosePracticePlaceCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.ErrorCompanyInputCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.InfoSubmittedCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.PracticeConfirmationCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.StudentInputConfirmationCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.SubmitCompanyApprovalRequestCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.company.AskingApproveNoContractCompanyCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.company.AskingCompanyAddressCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.company.AskingCompanyNameCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.company.AskingCorporateEmailCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.company.AskingInnCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.company.AskingLeadJobTitleCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.company.AskingLeadPhoneNumberCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.company.AskingPracticeFormatCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.company.CompanyInfoConfirmationCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.company.CompanyInfoSummaryCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.company.InputCompanyAddressCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.company.InputCompanyNameCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.company.InputCorporateEmailCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.company.InputInnValidationCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.company.InputLeadJobTitleCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.company.InputLeadPhoneNumberCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.company.InputPracticeFormatCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.itmo.AskingITMOPracticeDepartmentCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.itmo.AskingITMOPracticeLeadFullNameCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.itmo.InputITMOStudentDepartmentCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.itmo.InputITMOStudentLeadFullNameCommand;
import ru.itmo.infra.handler.usecase.user.guide.GuideMenuCommand;
import ru.itmo.infra.handler.usecase.user.guide.GuideNavigateCommand;
import ru.itmo.infra.handler.usecase.user.guide.GuideSectionOpenCommand;
import ru.itmo.infra.handler.usecase.user.manual.ManualEditAbortCommand;
import ru.itmo.infra.handler.usecase.user.manual.ManualEditConfirmCommand;
import ru.itmo.infra.handler.usecase.user.manual.ManualEditPickCommand;
import ru.itmo.infra.handler.usecase.user.manual.ManualEditPreviewCancelCommand;
import ru.itmo.infra.handler.usecase.user.manual.ManualEditSectionCommand;
import ru.itmo.infra.handler.usecase.user.manual.ManualEditStartCommand;
import ru.itmo.infra.handler.usecase.user.manual.ManualReorderDownCommand;
import ru.itmo.infra.handler.usecase.user.manual.ManualReorderEditBodyCommand;
import ru.itmo.infra.handler.usecase.user.manual.ManualReorderNoopCommand;
import ru.itmo.infra.handler.usecase.user.manual.ManualReorderUpCommand;
import ru.itmo.infra.handler.usecase.user.manual.ManualSubsectionAddCommand;
import ru.itmo.infra.handler.usecase.user.manual.ManualSubsectionDeleteCommand;
import ru.itmo.infra.handler.usecase.user.manual.ManualSubsectionDeleteConfirmCommand;
import ru.itmo.infra.handler.usecase.user.practiceformat.ChangePracticeFormatCommand;
import ru.itmo.infra.handler.usecase.user.studentapplicationinput.StudentDownloadApplicationCommand;
import ru.itmo.infra.handler.usecase.user.studentapplicationinput.StudentFilledApplicationCommand;
import ru.itmo.infra.handler.usecase.user.studentapplicationinput.UploadApplicationCommand;
import ru.itmo.infra.handler.usecase.user.studentapplicationinput.UploadApplicationHandleCommand;
import ru.itmo.infra.handler.usecase.user.studentregistration.StudentRegistrationConfirmationCommand;
import ru.itmo.infra.handler.usecase.user.studentregistration.StudentRegistrationISUCommand;
import ru.itmo.infra.handler.usecase.user.studentregistration.StudentRegistrationProcessISUCommand;
import ru.itmo.infra.handler.usecase.user.studentregistration.StudentRegistrationProcessStreamCommand;
import ru.itmo.infra.handler.usecase.user.studentregistration.StudentRegistrationStartCommand;
import ru.itmo.infra.handler.usecase.user.studentstatus.StatusCommand;
import ru.itmo.infra.storage.GuideRepository;
import ru.itmo.infra.handler.usecase.user.uploadsignedphoto.UploadSignedPhotoStartCommand;
import ru.itmo.infra.handler.usecase.user.uploadsignedphoto.UploadSignedPhotoHandleCommand;
import ru.itmo.infra.handler.usecase.user.uploadsignedphoto.ViewSignedPhotoCommand;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.itmo.exception.InvalidMessageException.ThrowDocumentException;

@Log
public class Handler {

    private static final TelegramClient telegramClient = PracticeAutomationBot.getTelegramClient();
    private static final List<Command> commands = new ArrayList<>();
    private static final Map<String, Command> commandsMap = new HashMap<>();

    static {
        // общие
        commands.add(new StartCommand());
        commands.add(new HelpCommand());

        // для студентов
        commands.add(new AskingApproveNoContractCompanyCommand());
        commands.add(new AskingCompanyAddressCommand());
        commands.add(new AskingCompanyNameCommand());
        commands.add(new AskingCorporateEmailCommand());
        commands.add(new AskingInnCommand());
        commands.add(new AskingLeadJobTitleCommand());
        commands.add(new AskingLeadPhoneNumberCommand());
        commands.add(new AskingPracticeFormatCommand());
        commands.add(new CompanyInfoConfirmationCommand());
        commands.add(new CompanyInfoSummaryCommand());
        commands.add(new InputCompanyAddressCommand());
        commands.add(new InputCompanyNameCommand());
        commands.add(new InputCorporateEmailCommand());
        commands.add(new InputInnValidationCommand());
        commands.add(new InputLeadJobTitleCommand());
        commands.add(new InputLeadPhoneNumberCommand());
        commands.add(new InputPracticeFormatCommand());

        commands.add(new AskingITMOPracticeDepartmentCommand());
        commands.add(new AskingITMOPracticeLeadFullNameCommand());
        commands.add(new InputITMOStudentDepartmentCommand());
        commands.add(new InputITMOStudentLeadFullNameCommand());

        commands.add(new ChoosePracticePlaceCommand());
        commands.add(new ChangePracticePlaceCommand());
        commands.add(new ErrorCompanyInputCommand());
        commands.add(new InfoSubmittedCommand());
        commands.add(new PracticeConfirmationCommand());
        commands.add(new SubmitCompanyApprovalRequestCommand());
        commands.add(new StudentInputConfirmationCommand());

        commands.add(new ChangePracticeFormatCommand());

        commands.add(new UploadApplicationHandleCommand());
        commands.add(new StudentDownloadApplicationCommand());
        commands.add(new StudentFilledApplicationCommand());
        commands.add(new UploadApplicationCommand());

        commands.add(new UploadSignedPhotoStartCommand());
        commands.add(new UploadSignedPhotoHandleCommand());
        commands.add(new ViewSignedPhotoCommand());

        commands.add(new StudentRegistrationConfirmationCommand());
        commands.add(new StudentRegistrationISUCommand());
        commands.add(new StudentRegistrationProcessStreamCommand());
        commands.add(new StudentRegistrationProcessISUCommand());
        commands.add(new StudentRegistrationStartCommand());

        commands.add(new StatusCommand());

        try {
            for (var section : GuideRepository.findAllActiveSectionsOrdered()) {
                commands.add(new GuideSectionOpenCommand(section.getCommand(), section.getTitle()));
            }
        } catch (InternalException e) {
            log.warning("Guide sections not registered: " + e.getMessage());
        }
        commands.add(new GuideNavigateCommand());
        commands.add(new GuideMenuCommand());
        commands.add(new ManualEditStartCommand());
        commands.add(new ManualEditSectionCommand());
        commands.add(new ManualEditPickCommand());
        commands.add(new ManualReorderUpCommand());
        commands.add(new ManualReorderDownCommand());
        commands.add(new ManualReorderNoopCommand());
        commands.add(new ManualReorderEditBodyCommand());
        commands.add(new ManualEditAbortCommand());
        commands.add(new ManualEditConfirmCommand());
        commands.add(new ManualEditPreviewCancelCommand());
        commands.add(new ManualSubsectionAddCommand());
        commands.add(new ManualSubsectionDeleteCommand());
        commands.add(new ManualSubsectionDeleteConfirmCommand());

        // для админов
        commands.add(new AddAdminCommand());
        commands.add(new BanCommand());
        commands.add(new BanConfirmationCommand());
        commands.add(new BanAdminCommand());
        commands.add(new BanAdminConfirmationCommand());
        commands.add(new ApproveCompanyApprovalRequestCommand());
        commands.add(new ApproveCompanyApprovalRequestConfirmationCommand());
        commands.add(new GetBannedCommand());
        commands.add(new ListAdminsCommand());
        commands.add(new ListCompanyApprovalRequestsCommand());
        commands.add(new RejectCompanyApprovalRequestCommand());
        commands.add(new RejectCompanyApprovalRequestConfirmationCommand());
        commands.add(new DeleteStreamCommand());
        commands.add(new DeleteStreamConfirmationCommand());
        commands.add(new DownloadApplicationCommand());
        commands.add(new ExportExcelCommand());
        commands.add(new ConfigureExportCommand());
        commands.add(new ToggleColumnCommand());
        commands.add(new FinishColumnsCommand());
        commands.add(new FillEduStreamCommand());
        commands.add(new FillEduStreamMoreFilesCommand());
        commands.add(new FillEduStreamUploadCommand());
        commands.add(new ForceUpdateCommand());
        commands.add(new ForceUpdateConfirmationCommand());
        commands.add(new GotoStreamCommand());
        commands.add(new InitEduInputStreamNameCommand());
        commands.add(new InitEduInputStreamStartDateCommand());
        commands.add(new InitEduStreamCommand());
        commands.add(new InitEduStreamEndDateCommand());
        commands.add(new CreateAdminFromUserCommand());
        commands.add(new PingStudentsCommand());
        commands.add(new ListPracticeOptionsCommand());
        commands.add(new AddPracticeOptionCommand());
        commands.add(new DeletePracticeOptionCommand());
        commands.add(new TogglePracticeOptionCommand());
        commands.add(new RenamePracticeOptionCommand());
        commands.add(new SetPracticeOptionFlagsCommand());
        commands.add(new UnbanCommand());
        commands.add(new UnbanConfirmationCommand());
        commands.add(new UploadExcelCommand());
        commands.add(new UploadExcelHandleCommand());
        commands.add(new ViewStudentPhotoCommand());
        commands.add(new GetStudentInfoCommand());
        commands.add(new UpdateGroupStudentsCommand());
        commands.add(new CreatePracticeFormatCommand());
        commands.add(new RenamePracticeFormatCommand());
        commands.add(new DeletePracticeFormatCommand());
        commands.add(new SetUserPracticeFormatCommand());

        for (Command command : commands) {
            if (command.getName().isEmpty()) {
                continue;
            }
            commandsMap.put(command.getName(), command);
        }
    }

    public static MessageToUser handleMessage(MessageDTO message) throws Exception {
        var nextFunc = getNextCommandFunction(message.getChatId());
        if (!message.hasText()) {
            if (nextFunc != null) {
                return executeCommand(nextFunc, message);
            }
            return MessageToUser.builder().text("").build();
        }

        var commandText = message.getText();
        var commandName = resolveCommandName(commandText);
        if (commandText.startsWith("/") && commandsMap.containsKey(commandName)) {
            return executeCommand(commandsMap.get(commandName), message);
        }

        if (nextFunc != null) {
            return executeCommand(nextFunc, message);
        }

        if (!commandsMap.containsKey(commandName)) {
            return MessageToUser.builder()
                    .text("Извините, но я не понимаю такую команду. Попробуйте другую или напишите \"/help\" для помощи или \"/start\" для возврата в меню")
                    .build();
        }

        return executeCommand(commandsMap.get(commandName), message);
    }

    /** Telegram шлёт /cmd@BotName в меню команд — приводим к ключу {@link #commandsMap}. */
    private static String normalizeTelegramCommandToken(String token) {
        if (token == null || token.isEmpty()) {
            return "";
        }
        int at = token.indexOf('@');
        if (at > 0) {
            return token.substring(0, at);
        }
        return token;
    }

    private static String resolveCommandName(String commandText) {
        var commandName = normalizeTelegramCommandToken(commandText.split(" ")[0]);
        if (commandsMap.containsKey(commandName)) {
            return commandName;
        }

        var approveCommand = "/approve_company_request";
        if (commandName.startsWith(approveCommand + "_")) {
            return approveCommand;
        }

        var rejectCommand = "/reject_company_request";
        if (commandName.startsWith(rejectCommand + "_")) {
            return rejectCommand;
        }

        var practiceOptionsCommand = "/practice_option_list";
        if (commandName.startsWith(practiceOptionsCommand + "_")) {
            return practiceOptionsCommand;
        }

        return commandName;
    }

    private static Command getNextCommandFunction(long chatId) {
        try {
            return ContextHolder.getNextCommand(chatId);
        } catch (UnknownUserException e) {
            return null;
        }
    }

    private static MessageToUser executeCommand(Command command, MessageDTO message) throws UnknownUserException, InternalException {
        if (AuthorizationService.isBanned(message.getChatId()) && !command.getName().equals("/start")) {
            return permissionDenied(message);
        }

        if (!checkPermission(message.getChatId(), command)) {
            return permissionDenied(message);
        }

        tryToSetEduStream(message.getChatId());
        updateCommandsDropOut(message.getChatId());
        prepareMessage(message);

        var response = command.execute(message);

        var nextCommand = getNextCommandFunction(message.getChatId());
        if (nextCommand == null && !command.getName().equals("/start")) {
            nextCommand = new StartCommand();
            ContextHolder.setNextCommand(message.getChatId(), nextCommand);
        }

        if (command.isNextCallNeeded()
                && nextCommand != null
                && command.getClass() != nextCommand.getClass()) {
            PracticeAutomationBot.sendToUser(response, message.getChatId(), false, null, null);
            response = executeCommand(nextCommand, message);
        }

        return response;
    }

    private static void prepareMessage(MessageDTO message) {
        if (message.hasText()) {
            message.setText(message.getText().replaceAll(Command.returnIcon, "").trim());
            message.setText(message.getText().replaceAll(Command.getIcon, "").trim());
            message.setText(message.getText().replaceAll(Command.uploadIcon, "").trim());
            message.setText(message.getText().replaceAll(Command.addIcon, "").trim());
            message.setText(message.getText().replaceAll(Command.RemoveIcon, "").trim());
            message.setText(message.getText().replaceAll(Command.helpIcon, "").trim());
            message.setText(message.getText().replaceAll(Command.registerIcon, "").trim());
        }
    }

    private static boolean checkPermission(long chatId, Command command) {
        try {
            if (command instanceof StartCommand) {
                return true;
            }

            if (AuthorizationService.isBanned(chatId)) {
                return false;
            }

            boolean isAdmin = AuthorizationService.canDoAdminActions(chatId);

            if (command.isAdminCommand()) {
                return isAdmin;
            }

            if (command instanceof UserCommand) {
                StudentStatus status = getStudentStatus(chatId);
                return ((UserCommand) command).isAvailableForStatus(status);
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static MessageToUser permissionDenied(MessageDTO message) throws InternalException, UnknownUserException {
        ContextHolder.endCommand(message.getChatId());
        var response = MessageToUser.builder().text("Доступ запрещен").build();
        PracticeAutomationBot.sendToUser(response, message.getChatId(), false, null, null);
        return executeCommand(new StartCommand(), message);
    }

    private static void tryToSetEduStream(long chatId) throws InternalException {
        var isAdmin = AuthorizationService.canDoAdminActions(chatId);
        if (isAdmin) {
            return;
        }

        try {
            ContextHolder.getEduStreamName(chatId);
        } catch (UnknownUserException e) {
            var eduOpt = StudentService.getNewestStudentEduStreamNameByChatId(chatId);
            eduOpt.ifPresent(s -> ContextHolder.setEduStreamName(chatId, s));
        }
    }

    public static MessageToUser handleCallback(MessageDTO message, String callbackDataString) throws Exception {
        var callbackData = new CallbackData(callbackDataString);
        if (callbackData.getKey() != null && !callbackData.getKey().isBlank()) {
            mapKeyToFunc(message.getChatId(), callbackData.getKey(), callbackData.getValue());
        }
        var commandName = resolveCommandName(callbackData.getCommand());
        message.setText(callbackDataString);
        Command cmd = commandsMap.get(commandName);
        if (cmd == null) {
            return MessageToUser.builder()
                    .text("Кнопка устарела или неизвестна. Откройте /start.")
                    .needRewriting(true)
                    .build();
        }
        return executeCommand(cmd, message);
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

    private static void mapKeyToFunc(Long chatId, String key, String value) {
        if (key.equals("eduStreamName")) {
            ContextHolder.setEduStreamName(chatId, value);
        }
        if (key.equals("column")) {
            ContextHolder.setCurrentColumn(chatId, value);
        }
    }

    public static void updateCommandsDropOut(long chatId) {
        try {
            List<BotCommand> userCommands;
            var isAdmin = AuthorizationService.canDoAdminActions(chatId);

            if (isAdmin) {
                userCommands = getAdminCommandsDropOut(chatId);
                setCommandsForUser(chatId, userCommands);
                return;
            }

            var status = getStudentStatus(chatId);
            userCommands = getStudentsCommandsDropOut(status);
            setCommandsForUser(chatId, userCommands);
        } catch (InternalException e) {
            log.warning("Ошибка обновления команд для " + chatId + ": " + e.getMessage());
        }
    }

    public static List<Command> getAvailableStudentCommands(StudentStatus status) {
        List<Command> commands = new ArrayList<>();

        commands.add(new ChoosePracticePlaceCommand());
        commands.add(new ChangePracticePlaceCommand());
        commands.add(new StudentDownloadApplicationCommand());
        commands.add(new UploadApplicationCommand());
        commands.add(new StudentRegistrationStartCommand());
        commands.add(new StudentFilledApplicationCommand());
        commands.add(new UploadSignedPhotoStartCommand());
        commands.add(new ViewSignedPhotoCommand());
        commands.add(new ChangePracticeFormatCommand());

        // Фильтруем только те, которые доступны для текущего статуса.
        return commands.stream()
                .filter(cmd -> {
                    if (cmd instanceof UserCommand) {
                        return ((UserCommand) cmd).isAvailableForStatus(status);
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    private static List<BotCommand> getStudentsCommandsDropOut(StudentStatus status) {
        List<BotCommand> result = new ArrayList<>();

        // Всегда доступные базовые команды
        addCommandIfExists(result, new StartCommand());
        addCommandIfExists(result, new HelpCommand());
        addCommandIfExists(result, new StatusCommand());

        try {
            for (var section : GuideRepository.findAllActiveSectionsOrdered()) {
                addCommandIfExists(result, new GuideSectionOpenCommand(section.getCommand(), section.getTitle()));
            }
        } catch (InternalException e) {
            log.warning("Guide commands not added to menu: " + e.getMessage());
        }

        // Команды, зависящие от статуса
        if (status != null) {
            getAvailableStudentCommands(status).forEach(cmd -> {
                // Проверяем, что команда не добавлена ранее и доступна
                if (result.stream().noneMatch(bc -> bc.getCommand().equals(cmd.getName())) &&
                        (!(cmd instanceof UserCommand) || ((UserCommand) cmd).isAvailableForStatus(status))) {
                    addCommandIfExists(result, cmd);
                }
            });
        }

        return result;
    }

    private static List<BotCommand> getAdminCommandsDropOut(long chatId) {
        List<BotCommand> resultCommands = new ArrayList<>();

        for (var cmd : getAdminCommands(chatId)) {
            addCommandIfExists(resultCommands, cmd);
        }

        return resultCommands;
    }

    public static List<Command> getStudentCommands() {
        return List.of(
                new HelpCommand(),
                new StartCommand(),
                new StatusCommand()
        );
    }

    public static List<Command> getAdminCommands(long chatId) {
        try {
            ContextHolder.getEduStreamName(chatId);
            return List.of(new StartCommand(), new ListCompanyApprovalRequestsCommand());
        } catch (UnknownUserException ignored) { }
        return List.of(
                new HelpCommand(),
                new StartCommand(),
                new GuideMenuCommand(),
                new ManualEditStartCommand(),
                new BanCommand(),
                new BanAdminCommand(),
                new ListCompanyApprovalRequestsCommand(),
                new ApproveCompanyApprovalRequestCommand(),
                new RejectCompanyApprovalRequestCommand(),
                new DeleteStreamCommand(),
                new DownloadApplicationCommand(),
                new ExportExcelCommand(),
                new ConfigureExportCommand(),
                new ToggleColumnCommand(),
                new FinishColumnsCommand(),
                new ForceUpdateCommand(),
                new InitEduStreamCommand(),
                new AddAdminCommand(),
                new CreateAdminFromUserCommand(),
                new PingStudentsCommand(),
                new ListPracticeOptionsCommand(),
                new AddPracticeOptionCommand(),
                new DeletePracticeOptionCommand(),
                new TogglePracticeOptionCommand(),
                new RenamePracticeOptionCommand(),
                new SetPracticeOptionFlagsCommand(),
                new GetBannedCommand(),
                new ListAdminsCommand(),
                new UnbanCommand(),
                new UploadExcelCommand(),
                new GetStudentInfoCommand(),
                new UpdateGroupStudentsCommand(),
                new CreatePracticeFormatCommand(),
                new DeletePracticeFormatCommand(),
                new RenamePracticeFormatCommand(),
                new SetUserPracticeFormatCommand()
        );
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

    public static StudentStatus getStudentStatus(long chatId) {
        try {
            // Сначала пробуем получить имя потока
            String streamName;
            try {
                streamName = ContextHolder.getEduStreamName(chatId);
            } catch (UnknownUserException e) {
                // Если нет в контексте, пробуем получить из базы
                var eduOpt = StudentService.getNewestStudentEduStreamNameByChatId(chatId);
                if (eduOpt.isEmpty()) {
                    return StudentStatus.NOT_REGISTERED;
                }
                streamName = eduOpt.get();
                ContextHolder.setEduStreamName(chatId, streamName);
            }

            // Получаем студента
            var studentOpt = StudentService.findStudentByChatIdAndEduStreamName(chatId, streamName);
            if (studentOpt.isPresent()) {
                return studentOpt.get().getStatus();
            }

            return StudentStatus.NOT_REGISTERED;
        } catch (InternalException | BadRequestException e) {
            return StudentStatus.NOT_REGISTERED;
        }
    }
}
