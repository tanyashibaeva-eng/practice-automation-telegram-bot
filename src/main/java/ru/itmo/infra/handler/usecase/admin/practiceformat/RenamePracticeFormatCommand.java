package ru.itmo.infra.handler.usecase.admin.practiceformat;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.NotificationService;
import ru.itmo.application.PracticeFormatService;
import ru.itmo.infra.storage.StudentRepository;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RenamePracticeFormatCommand implements AdminCommand {
    private static final Pattern QUOTED_TWO_ARGS = Pattern.compile("^/practice_format_rename\\s+\"([^\"]+)\"\\s+\"([^\"]+)\"\\s*$");

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            ContextHolder.endCommand(message.getChatId());
            String raw = message.getText() == null ? "" : message.getText().trim();

            Matcher m = QUOTED_TWO_ARGS.matcher(raw);
            if (!m.matches()) {
                throw new BadRequestException("Неверный формат. Используйте: /practice_format_rename \"<старое>\" \"<новое>\"");
            }

            String oldName = m.group(1).trim();
            String newName = m.group(2).trim();
            var oldFormatOpt = PracticeFormatService.findByDisplayNameIgnoreCase(oldName);
            PracticeFormatService.rename(oldName, newName);

            // информируем студентов
            if (oldFormatOpt.isPresent()) {
                var chatIds = StudentRepository.findChatIdsByPracticeFormatIdInActiveStreams(oldFormatOpt.get().getId());
                NotificationService.notifyUsers(chatIds, """
                        Формат прохождения практики был переименован администратором.
                        Было: %s
                        Стало: %s

                        Если вы уже загружали заявку, пожалуйста, загрузите её заново.
                        """.formatted(oldName, newName));
            }

            return MessageToUser.builder()
                    .text("Формат переименован: \"%s\" -> \"%s\"".formatted(oldName, newName))
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
        return "/practice_format_rename";
    }

    @Override
    public String getDescription() {
        return "Переименовать формат. Пример: `/practice_format_rename \"Очно\" \"Очно (обновлено)\"`";
    }
}

