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

public class DeletePracticeFormatCommand implements AdminCommand {
    private static final Pattern QUOTED_ONE_ARG = Pattern.compile("^/practice_format_delete\\s+\"([^\"]+)\"\\s*$");

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            ContextHolder.endCommand(message.getChatId());
            String raw = message.getText() == null ? "" : message.getText().trim();

            Matcher m = QUOTED_ONE_ARG.matcher(raw);
            if (!m.matches()) {
                throw new BadRequestException("Неверный формат. Используйте: /practice_format_delete \"<название>\"");
            }

            String name = m.group(1).trim();
            var oldFormatOpt = PracticeFormatService.findByDisplayNameIgnoreCase(name);

            // информируем студентов
            if (oldFormatOpt.isPresent()) {
                var chatIds = StudentRepository.findChatIdsByPracticeFormatIdInActiveStreams(oldFormatOpt.get().getId());
                NotificationService.notifyUsers(chatIds, """
                        Формат прохождения практики, который вы выбрали, был удален администратором: %s

                        Пожалуйста, выберите новый формат и, если вы уже загружали заявку, загрузите её заново.
                        """.formatted(name));
            }

            PracticeFormatService.delete(name);

            return MessageToUser.builder()
                    .text("Формат удален: \"%s\"".formatted(name))
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
        return "/practice_format_delete";
    }

    @Override
    public String getDescription() {
        return "Удалить формат. Пример: `/practice_format_delete \"Очно\"`";
    }
}

