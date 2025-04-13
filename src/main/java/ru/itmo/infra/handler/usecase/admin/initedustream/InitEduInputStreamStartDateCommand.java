package ru.itmo.infra.handler.usecase.admin.initedustream;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.EduStreamCreationArgs;
import ru.itmo.exception.BadRequestException;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.util.TextParser;

import java.time.LocalDate;

public class InitEduInputStreamStartDateCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            var dateStr = message.getText().trim();
            var date = TextParser.parseDate(dateStr);

            // Проверяем, что дата в будущем
            if (date.isBefore(LocalDate.now())){
                throw new BadRequestException("Дата должна быть в будущем");
            }

            // Сохраняем дату потока в контекст
            var dto =(EduStreamCreationArgs) ContextHolder.getCommandData(message.getChatId());
            dto.setDateFrom(date);
            ContextHolder.setCommandData(message.getChatId(), dto);
            ContextHolder.setNextCommand(message.getChatId(), new InitEduStreamEndDateCommand());

            return MessageToUser.builder()
                    .text("Введите дату окончания производственной практики в формате чч.мм.гггг (25.12.2025):")
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .keyboardMarkup(getReturnToStartMarkup())
                    .needRewriting(true)
                    .build();

        } catch (BadRequestException e) {
            // Остаемся на этом же шаге для повторного ввода
            ContextHolder.setNextCommand(message.getChatId(), this);
            return MessageToUser.builder()
                    .text(e.getMessage() + "\nПожалуйста, попробуйте еще раз")
                    .keyboardMarkup(getReturnToStartMarkup())
                    .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }
}
