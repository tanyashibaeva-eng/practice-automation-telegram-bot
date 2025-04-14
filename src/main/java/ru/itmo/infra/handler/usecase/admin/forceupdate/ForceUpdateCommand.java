package ru.itmo.infra.handler.usecase.admin.forceupdate;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.ForceUpdateDTO;
import ru.itmo.exception.BadRequestException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.util.TextParser;

import java.util.Set;

public class ForceUpdateCommand implements AdminCommand {
    private final Set<String> fieldNames = Set.of("статус", "место_практики", "формат_практики", "инн_компании", "имя_компании", "фио_руководителя", "телефон_руководителя", "почта_руководителя", "должность_руководителя");

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            var messageText = message.getText().trim().replaceAll(" +", "");
            var fields = messageText.split(" \"");
            if (fields.length < 5 && fields.length % 2 == 0) {
                throw new BadRequestException("Неверный формат команды, пример (кавычки обязательны): `/forceupdate <chatId> \"<eduStreamName>\" \"<fieldName1>\" \"<fieldValue1>\", ...,  \"<fieldNameN>\" \"<fieldValueN>\"`");
            }

            var studentChatIdStr = fields[0].replace("/forceupdate ", "").trim();
            long studentChatId;
            try {
                studentChatId = TextParser.parseDoubleToLong(studentChatIdStr);
            } catch (BadRequestException e) {
                throw new BadRequestException("Неверный тип аргумента <chatId>, ожидалось число");
            }

            var eduStreamName = fields[2].replace("\"", "").replaceAll(" +", "").trim();

            var studentOpt = StudentService.findStudentByChatIdAndEduStreamName(studentChatId, eduStreamName);
            if (studentOpt.isEmpty()) {
                throw new BadRequestException("Студент с chatId: %d не найден".formatted(studentChatId));
            }

            var dtoBuilder = ForceUpdateDTO.builder();
            dtoBuilder.chatId(studentChatId);
            dtoBuilder.eduStreamName(eduStreamName);

            for (int i = 0; i < fields.length; i += 2) {
                var fieldName = fields[i].replace("\"", "").replaceAll(" +", "").trim();
                var fieldValue = fields[i + 1].replace("\"", "").replaceAll(" +", "").trim();

                if (!fieldNames.contains(fieldName)) {
                    throw new BadRequestException("Неизвестное поле \"%s\", список полей доступных для обновления: ".formatted(fieldName) + fieldNames.stream().map(v -> "\"" + v + "\""));
                }

                switch (fieldName) {
                    case "статус":
                        dtoBuilder.status(fieldValue);
                    case "место_практики":
                        dtoBuilder.practicePlace(fieldValue);
                    case "формат_практики":
                        dtoBuilder.practiceFormat(fieldValue);
                    case "инн_компании":
                        dtoBuilder.companyINN(fieldValue);
                    case "имя_компании":
                        dtoBuilder.companyName(fieldValue);
                    case "фио_руководителя":
                        dtoBuilder.companyLeadFullName(fieldValue);
                    case "телефон_руководителя":
                        dtoBuilder.companyLeadPhone(fieldValue);
                    case "почта_руководителя":
                        dtoBuilder.companyLeadEmail(fieldValue);
                    case "должность_руководителя":
                        dtoBuilder.companyLeadJobTitle(fieldValue);
                }
            }


            var textBuilder = new StringBuilder();
            var student = studentOpt.get();
            var dto = dtoBuilder.build();

            var currStrValStatus = student.getStatus().getDisplayName().isEmpty() ? "Не заполнено" : student.getStatus().getDisplayName();
            var currStrValPlace = student.getPracticePlace() == null ? "Не заполнено" : student.getPracticePlace().getDisplayName();
            var currStrValFormat = student.getPracticeFormat() == null ? "Не заполнено" : student.getPracticeFormat().getDisplayName();
            var currStrValINN = student.getCompanyINN() == null ? "Не заполнено" : student.getCompanyINN();
            var currStrValCompany = student.getCompanyName() == null ? "Не заполнено" : student.getCompanyName();
            var currStrValLeadFullName = student.getCompanyLeadFullName() == null ? "Не заполнено" : student.getCompanyLeadFullName();
            var currStrValLeadPhone = student.getCompanyLeadPhone() == null ? "Не заполнено" : student.getCompanyLeadPhone();
            var currStrValLeadEmail = student.getCompanyLeadEmail() == null ? "Не заполнено" : student.getCompanyLeadEmail();
            var currStrValLeadTitle = student.getCompanyLeadJobTitle() == null ? "Не заполнено" : student.getCompanyLeadJobTitle();

            textBuilder.append("Текущие значения студента с chatId %d  в потоке %s:\n".formatted(studentChatId, eduStreamName));

            textBuilder.append("\tФИО: %s\n".formatted(student.getFullName()));
            textBuilder.append("\tСтатус: %s\n".formatted(currStrValStatus));
            textBuilder.append("\tМесто практики: %s\n".formatted(currStrValPlace));
            textBuilder.append("\tФормат практики: %s\n".formatted(currStrValFormat));
            textBuilder.append("\tИНН компании: %s\n".formatted(currStrValINN));
            textBuilder.append("\tИмя компании: %s\n".formatted(currStrValCompany));
            textBuilder.append("\tФИО руководителя: %s\n".formatted(currStrValLeadFullName));
            textBuilder.append("\tТелефон руководителя: %s\n".formatted(currStrValLeadPhone));
            textBuilder.append("\tПочта руководителя: %s\n".formatted(currStrValLeadEmail));
            textBuilder.append("\tДолжность руководителя: %s\n".formatted(currStrValLeadTitle));

            textBuilder.append("\n");
            textBuilder.append("Новые значения, которые будут установлены:\n");
            textBuilder.append("\tФИО: %s\n".formatted(student.getFullName()));
            textBuilder.append("\tСтатус: %s\n".formatted(dto.getStatus() == null ? currStrValStatus : dto.getStatus()));
            textBuilder.append("\tМесто практики: %s\n".formatted(dto.getPracticePlace() == null ? currStrValPlace : dto.getPracticePlace()));
            textBuilder.append("\tФормат практики: %s\n".formatted(dto.getPracticeFormat() == null ? currStrValFormat : dto.getPracticeFormat()));
            textBuilder.append("\tИНН компании: %s\n".formatted(dto.getCompanyINN() == null ? currStrValINN : dto.getCompanyINN()));
            textBuilder.append("\tИмя компании: %s\n".formatted(dto.getCompanyName() == null ? currStrValCompany : dto.getCompanyName()));
            textBuilder.append("\tФИО руководителя: %s\n".formatted(dto.getCompanyLeadFullName() == null ? currStrValLeadFullName : dto.getCompanyLeadFullName()));
            textBuilder.append("\tТелефон руководителя: %s\n".formatted(dto.getCompanyLeadPhone() == null ? currStrValLeadPhone : dto.getCompanyLeadPhone()));
            textBuilder.append("\tПочта руководителя: %s\n".formatted(dto.getCompanyLeadEmail() == null ? currStrValLeadEmail : dto.getCompanyLeadEmail()));
            textBuilder.append("\tДолжность руководителя: %s\n".formatted(dto.getCompanyLeadJobTitle() == null ? currStrValLeadTitle : dto.getCompanyLeadJobTitle()));


            textBuilder.append("\nОбновить информацию о студенте (ВНИМАНИЕ: команда напрямую обновляет поля в обход всех валидаций)?");
            ContextHolder.setCommandData(message.getChatId(), dto);
            ContextHolder.setNextCommand(message.getChatId(), new ForceUpdateConfirmationCommand());
            return MessageToUser.builder()
                    .text(textBuilder.toString())
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .keyboardMarkup(getInlineKeyboard())
                    .build();
        } catch (BadRequestException e) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text(e.getMessage())
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "/forceupdate";
    }

    @Override
    public String getDescription() {
        return "Вручную обновить состояние студента (в обход валидаций). Пример: `/forceupdate 12345 \"Бакалавры 2025\" \"статус\" \"Практика согласована\" \"фио_руководителя\" \"Иванов Иван Иванович\"`";
    }
}
