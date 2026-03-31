package ru.itmo.infra.handler.usecase.admin.initedustream;


import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.EduStreamService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.EduStreamCreationArgs;
import ru.itmo.domain.model.EduStream;
import ru.itmo.exception.BadRequestException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

public class InitEduInputStreamNameCommand implements AdminCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            String streamName = message.getText().trim();
            EduStream eduStream = new EduStream(streamName);

            // Проверяем существование потока
            if (EduStreamService.findEduStreamByName(eduStream).isPresent()) {
                throw new BadRequestException("Поток с таким именем уже существует");
            }

            // Сохраняем имя потока в контекст
            var dto = EduStreamCreationArgs.builder()
                    .name(streamName)
                    .build();
            ContextHolder.setCommandData(message.getChatId(), dto);
            ContextHolder.setNextCommand(message.getChatId(), new InitEduInputStreamStartDateCommand());

            return MessageToUser.builder()
                    .text("Введите дату начала производственной практики в формате чч.мм.гггг (25.10.2025):")
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