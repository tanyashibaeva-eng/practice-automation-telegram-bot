package ru.itmo.infra.handler.usecase.start;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.itmo.application.AuthorizationService;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.EduStreamService;
import ru.itmo.application.StudentService;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.model.Student;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.handler.Handler;
import ru.itmo.infra.handler.usecase.admin.companyapproval.ListCompanyApprovalRequestsCommand;
import ru.itmo.infra.handler.usecase.admin.gotostream.GotoStreamCommand;
import ru.itmo.infra.handler.usecase.admin.initedustream.InitEduStreamCommand;
import ru.itmo.infra.handler.usecase.admin.practiceoption.ListPracticeOptionsCommand;
import ru.itmo.infra.handler.usecase.user.UserCommand;
import ru.itmo.infra.handler.usecase.user.guide.GuideMenuCommand;
import ru.itmo.infra.handler.usecase.user.manual.ManualEditStartCommand;
import ru.itmo.infra.handler.usecase.user.studentregistration.StudentRegistrationStartCommand;
import ru.itmo.infra.handler.usecase.user.studentstatus.StatusCommand;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public class StartCommand implements UserCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        ContextHolder.endCommand(message.getChatId());
        Handler.updateCommandsDropOut(message.getChatId());

        if (AuthorizationService.isBanned(message.getChatId())) {
            return bannedStartCommand(message);
        }

        if (AuthorizationService.canDoAdminActions(message.getChatId())) {
            return adminStartCommand(message);
        }

        return userStartCommand(message);
    }

    private MessageToUser bannedStartCommand(MessageDTO message) {
        return MessageToUser.builder()
                .text("К сожалению, вы были забанены администратором. Доступ ко всем командам запрещен. Для разбана требуется связаться с преподавателем и выяснить причину бана")
                .needRewriting(true)
                .build();
    }

    private MessageToUser adminStartCommand(MessageDTO message) throws InternalException {
        var streams = EduStreamService.findAllEduStreams();

        var streamNames = new ArrayList<String>(streams.size());
        for (var stream : streams) {
            streamNames.add(stream.getName());
        }

        return MessageToUser.builder()
                .text("Админ панель\n\nОтсюда вы можете управлять потоками студентов и отслеживать их статус!\n\nВыберите поток с которым хотите работать или введите команду (`/help` для помощи)")
                .keyboardMarkup(getAdminKeyboard(streamNames))
                .needRewriting(true)
                .build();
    }

    private MessageToUser userStartCommand(MessageDTO message) {
        StudentStatus status = null;
        try {
            Optional<String> activeEduStreamOpt = StudentService.findActiveEduStreamNameByChatId(message.getChatId());

            if (activeEduStreamOpt.isPresent()) {
                Optional<Student> studentOpt = StudentService.findStudentByChatIdAndEduStreamName(
                        message.getChatId(),
                        activeEduStreamOpt.get()
                );
                if (studentOpt.isPresent()) {
                    status = studentOpt.get().getStatus();
                }
            }
        } catch (InternalException | BadRequestException e) {
        }

        return MessageToUser.builder()
                .text("Главное меню студента\n\nЗдесь вы можете управлять своей практикой")
                .keyboardMarkup(getUserKeyboard(status))
                .needRewriting(true)
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/start";
    }

    @Override
    public String getDescription() {
        return "Главное меню";
    }

    private static ReplyKeyboard getAdminKeyboard(List<String> streamNames) {
        var markupBuilder = InlineKeyboardMarkup.builder();
        for (var streamName : streamNames) {
            var callbackData = CallbackData.builder()
                    .command(new GotoStreamCommand().getName())
                    .key("eduStreamName")
                    .value(streamName)
                    .build();
            var keyboardRow = new InlineKeyboardRow(
                    InlineKeyboardButton.builder()
                            .text(streamName)
                            .callbackData(callbackData.toString())
                            .build()
            );
            markupBuilder.keyboardRow(keyboardRow);
        }

        var initStreamCallbackData = CallbackData.builder()
                .command(new InitEduStreamCommand().getName())
                .build();
        markupBuilder.keyboardRow(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text(registerIcon + " Добавить новый поток")
                        .callbackData(initStreamCallbackData.toString())
                        .build()
        ));

        markupBuilder.keyboardRow(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text("📖 Открыть мануал")
                        .callbackData(CallbackData.builder()
                                .command(GuideMenuCommand.COMMAND_NAME)
                                .build()
                                .toString())
                        .build()
        ));

        markupBuilder.keyboardRow(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text("✏️ Редактировать мануал")
                        .callbackData(CallbackData.builder()
                                .command(ManualEditStartCommand.COMMAND_NAME)
                                .build()
                                .toString())
                        .build()
        ));

        var helpCallbackData = CallbackData.builder()
                .command("/help")
                .build();
        markupBuilder.keyboardRow(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text(helpIcon + " Справка с командами")
                        .callbackData(helpCallbackData.toString())
                        .build()
        ));

        var requestsCallbackData = CallbackData.builder()
                .command(new ListCompanyApprovalRequestsCommand().getName())
                .build();
        markupBuilder.keyboardRow(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text("Заявки на компании")
                        .callbackData(requestsCallbackData.toString())
                        .build()
        ));

        var practiceOptionsCallbackData = CallbackData.builder()
                .command(new ListPracticeOptionsCommand().getName())
                .build();
        markupBuilder.keyboardRow(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text("🏢 Посмотреть места практики")
                        .callbackData(practiceOptionsCallbackData.toString())
                        .build()
        ));

        return markupBuilder.build();
    }

    private static ReplyKeyboard getMarkupKeyboardForStart() {
        var markupBuilder = InlineKeyboardMarkup.builder()
                .keyboardRow(
                        new InlineKeyboardRow(
                                InlineKeyboardButton.builder()
                                        .text(registerIcon + " Регистрация")
                                        .callbackData(
                                                CallbackData.builder()
                                                        .command(new StudentRegistrationStartCommand().getName())
                                                        .build()
                                                        .toString()
                                        ).build()
                        ));
        appendGuideManualButtonRow(markupBuilder);
        return markupBuilder.build();
    }

    private static ReplyKeyboard getUserKeyboard(StudentStatus status) {
        if (status == null) {
            return getMarkupKeyboardForStart();
        }

        var markupBuilder = InlineKeyboardMarkup.builder();

        // Добавим кнопку "Мой статус" в любом случае.
        markupBuilder.keyboardRow(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text("Мой статус")
                        .callbackData(CallbackData.builder()
                                .command(new StatusCommand().getName())
                                .build()
                                .toString())
                        .build()
        ));

        appendGuideManualButtonRow(markupBuilder);

        // Добавляем остальные команды, доступные для статуса.
        Handler.getAvailableStudentCommands(status).forEach(cmd -> {
            if (cmd instanceof UserCommand userCmd && !cmd.getName().equals(new StatusCommand().getName())) {
                markupBuilder.keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(userCmd.getDisplayName())
                                .callbackData(
                                        CallbackData.builder()
                                                .command(userCmd.getName())
                                                .build()
                                                .toString())
                                .build()
                ));
            }
        });

        return markupBuilder.build();
    }

    private static void appendGuideManualButtonRow(InlineKeyboardMarkup.InlineKeyboardMarkupBuilder markupBuilder) {
        markupBuilder.keyboardRow(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text("📖 Открыть мануал")
                        .callbackData(CallbackData.builder()
                                .command(GuideMenuCommand.COMMAND_NAME)
                                .build()
                                .toString())
                        .build()
        ));
    }
}
