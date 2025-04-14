package ru.itmo.infra.handler.usecase.admin.pingstudents;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.NotificationService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

public class PingStudentsCommand implements AdminCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        ContextHolder.endCommand(message.getChatId());
        NotificationService.pingStudents();
        return MessageToUser.builder()
                .text("Всем студентам, от которых ожидаются действия, были отправлены уведомления")
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "/ping_students";
    }

    @Override
    public String getDescription() {
        return "Отправить уведомления всем студентам, от которых ожидается действие (например, заполнение данных о компании)";
    }

}
