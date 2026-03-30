package ru.itmo.infra.handler.usecase.admin.companyapproval;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.itmo.application.CompanyApprovalRequestService;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.model.CompanyApprovalRequest;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

import java.util.ArrayList;
import java.util.List;

public class ListCompanyApprovalRequestsCommand implements AdminCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        ContextHolder.endCommand(message.getChatId());

        var requests = CompanyApprovalRequestService.getPendingRequests();
        if (requests.isEmpty()) {
            return MessageToUser.builder()
                    .text("Сейчас нет заявок на подтверждение компаний или офисов в Санкт-Петербурге.")
                    .keyboardMarkup(getReturnToStartMarkup())
                    .needRewriting(true)
                    .build();
        }

        var textBuilder = new StringBuilder("Заявки на подтверждение компаний и офисов в Санкт-Петербурге:\n\n");
        for (var request : requests) {
            var studentName = StudentService.findStudentByChatIdAndEduStreamName(request.getStudentChatId(), request.getEduStreamName())
                    .map(student -> student.getFullName())
                    .orElse("Неизвестный студент");

            textBuilder.append("#").append(request.getId()).append("\n");
            textBuilder.append("Студент: ").append(studentName).append("\n");
            textBuilder.append("Поток: ").append(request.getEduStreamName()).append("\n");
            textBuilder.append("Компания: ").append(request.getCompanyName()).append("\n");
            textBuilder.append("ИНН: ").append(request.getInn()).append("\n");
            textBuilder.append("Причина: ")
                    .append(request.isRequiresSpbOfficeApproval()
                            ? "подтверждение офиса в Санкт-Петербурге"
                            : "добавление новой компании")
                    .append("\n");
            textBuilder.append("Адрес: ").append(request.getCompanyAddress()).append("\n");
            textBuilder.append("Действие: кнопки ниже\n\n");
        }

        return MessageToUser.builder()
                .text(textBuilder.toString().trim())
                .keyboardMarkup(buildRequestsKeyboard(requests))
                .needRewriting(true)
                .build();
    }

    private InlineKeyboardMarkup buildRequestsKeyboard(List<CompanyApprovalRequest> requests) {
        var rows = new ArrayList<InlineKeyboardRow>();

        for (var request : requests) {
            rows.add(new InlineKeyboardRow(
                    InlineKeyboardButton.builder()
                            .text("Подтвердить #" + request.getId())
                            .callbackData(CallbackData.builder()
                                    .command("/approve_company_request_" + request.getId())
                                    .build()
                                    .toString())
                            .build(),
                    InlineKeyboardButton.builder()
                            .text("Отклонить #" + request.getId())
                            .callbackData(CallbackData.builder()
                                    .command("/reject_company_request_" + request.getId())
                                    .build()
                                    .toString())
                            .build()
            ));
        }

        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text(returnIcon + " Вернуться в меню")
                        .callbackData(CallbackData.builder().command("/start").build().toString())
                        .build()
        ));

        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/company_requests";
    }

    @Override
    public String getDescription() {
        return "Показать заявки на подтверждение компаний";
    }
}
