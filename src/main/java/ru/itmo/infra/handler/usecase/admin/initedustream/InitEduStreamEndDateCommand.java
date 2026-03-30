package ru.itmo.infra.handler.usecase.admin.initedustream;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.EduStreamService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.EduStreamCreationArgs;
import ru.itmo.exception.BadRequestException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.util.TextUtils;

import java.time.LocalDate;
import java.time.Year;

public class InitEduStreamEndDateCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            var dateStr = message.getText().trim();
            var date = TextUtils.parseDate(dateStr);
            var dto = (EduStreamCreationArgs)ContextHolder.getCommandData(message.getChatId());

            // Проверяем, что дата в будущем
            if (date.isBefore(LocalDate.now())){
                throw new BadRequestException("Дата должна быть в будущем");
            }
            if (date.isBefore(dto.getDateFrom())){
                throw new BadRequestException("Дата окончания должна быть позже даты начала");
            }

            // Сохраняем дату потока в контекст
            dto.setDateTo(date);
            dto.setYear(Year.now().getValue());
            ContextHolder.setCommandData(message.getChatId(), dto);
            ContextHolder.endCommand(message.getChatId());
            EduStreamService.createEduStream(dto);
            return MessageToUser.builder()
                    .text("Поток %s успешно создан".formatted(dto.getName()))
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
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
        return true;
    }

}
