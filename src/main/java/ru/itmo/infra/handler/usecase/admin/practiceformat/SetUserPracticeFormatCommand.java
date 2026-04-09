package ru.itmo.infra.handler.usecase.admin.practiceformat;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.NotificationService;
import ru.itmo.application.PracticeFormatService;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.util.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetUserPracticeFormatCommand implements AdminCommand {
    private static final Pattern PATTERN = Pattern.compile("^/practice_format_set_user\\s+(\\d+)\\s+\"([^\"]+)\"\\s*$");

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            ContextHolder.endCommand(message.getChatId());
            String raw = message.getText() == null ? "" : message.getText().trim();

            Matcher m = PATTERN.matcher(raw);
            if (!m.matches()) {
                throw new BadRequestException("Неверный формат. Используйте: /practice_format_set_user <ISU> \"<формат>\"");
            }

            int targetIsuId = TextUtils.parseIsu(m.group(1));
            String formatName = m.group(2).trim();

            var formatOpt = PracticeFormatService.findByDisplayNameIgnoreCase(formatName);
            if (formatOpt.isEmpty()) {
                throw new BadRequestException("Формат \"%s\" не найден".formatted(formatName));
            }

            var format = formatOpt.get();
            PracticeFormat legacy;
            try {
                legacy = PracticeFormat.valueOfIgnoreCase(format.getCode());
            } catch (Exception ignored) {
                legacy = PracticeFormat.NOT_SPECIFIED;
            }

            var studentOpt = StudentService.findStudentByIsu(targetIsuId);
            if (studentOpt.isEmpty()) {
                throw new BadRequestException("Студент с заданным ИСУ не найден");
            }
            Long chatId = studentOpt.get().getTelegramUser().getChatId();

            StudentService.changePracticeFormatForCurrentStream(targetIsuId, legacy, format.getId());

            // информируем несчастного студента
            NotificationService.notifyUser(chatId, """
                    Администратор изменил формат прохождения вашей практики на: %s

                    Если вы уже загружали заявку, пожалуйста, загрузите её заново.
                    """.formatted(format.getDisplayName()));

            return MessageToUser.builder()
                    .text("Формат практики студента %d обновлен".formatted(targetIsuId))
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();
        } catch (BadRequestException e) {
            return MessageToUser.builder().text(e.getMessage()).keyboardMarkup(new ReplyKeyboardRemove(true)).build();
        } catch (InternalException e) {
            return MessageToUser.builder().text("Что-то пошло не так").keyboardMarkup(new ReplyKeyboardRemove(true)).build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "/practice_format_set_user";
    }

    @Override
    public String getDescription() {
        return "Изменить формат практики у пользователя. Пример: `/practice_format_set_user 111111 \"Очно\"`";
    }
}

