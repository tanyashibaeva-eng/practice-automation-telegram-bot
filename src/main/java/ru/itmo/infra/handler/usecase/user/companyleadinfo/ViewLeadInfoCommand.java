package ru.itmo.infra.handler.usecase.user.companyleadinfo;

import lombok.SneakyThrows;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.user.UserCommand;

public class ViewLeadInfoCommand implements UserCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var studentOpt = StudentService.getStudentWithLeadInfo(chatId);

        if (studentOpt.isEmpty()) {
            return MessageToUser.builder()
                    .text("Студент не найден в активном потоке")
                    .keyboardMarkup(Command.returnToStartInlineMarkup())
                    .needRewriting(true)
                    .build();
        }

        var student = studentOpt.get();
        var sb = new StringBuilder("Данные руководителя практики от компании:\n\n");
        sb.append("ФИО: ").append(valueOrDash(student.getCompanyLeadFullName())).append("\n");
        sb.append("Телефон: ").append(valueOrDash(student.getCompanyLeadPhone())).append("\n");
        sb.append("Email: ").append(valueOrDash(student.getCompanyLeadEmail())).append("\n");
        sb.append("Должность: ").append(valueOrDash(student.getCompanyLeadJobTitle())).append("\n");

        return MessageToUser.builder()
                .text(sb.toString())
                .keyboardMarkup(Command.returnToStartInlineMarkup())
                .needRewriting(true)
                .build();
    }

    private static String valueOrDash(String value) {
        return (value == null || value.isBlank()) ? "—" : value;
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/view_lead_info";
    }

    @Override
    public String getDescription() {
        return "Посмотреть данные руководителя практики от компании";
    }

    @Override
    public String getDisplayName() {
        return "Просмотр данных руководителя";
    }

    @Override
    public boolean isAvailableForStatus(StudentStatus status) {
        if (status == null) return false;
        return switch (status) {
            case NOT_REGISTERED, REGISTERED, PRACTICE_IN_ITMO_MARKINA, PRACTICE_APPROVED -> false;
            default -> true;
        };
    }
}
