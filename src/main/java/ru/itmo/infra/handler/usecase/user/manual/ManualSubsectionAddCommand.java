package ru.itmo.infra.handler.usecase.user.manual;

import lombok.NoArgsConstructor;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.handler.usecase.user.UserCommand;
import ru.itmo.infra.storage.GuideRepository;

@NoArgsConstructor
public class ManualSubsectionAddCommand implements UserCommand {

    public static final String COMMAND_NAME = "/manual_sub_add";

    @Override
    public MessageToUser execute(MessageDTO message) {
        var cd = new CallbackData(message.getText());
        if (!"sectionId".equals(cd.getKey()) || cd.getValue() == null || cd.getValue().isBlank()) {
            return MessageToUser.builder()
                    .text("Некорректные данные.")
                    .needRewriting(true)
                    .build();
        }
        try {
            int sectionId = Integer.parseInt(cd.getValue().trim());
            var secOpt = GuideRepository.findSectionById(sectionId);
            if (secOpt.isEmpty()) {
                return MessageToUser.builder()
                        .text("Раздел не найден.")
                        .needRewriting(true)
                        .build();
            }
            ContextHolder.endCommand(message.getChatId());
            ContextHolder.setNextCommand(message.getChatId(), new ManualSubsectionAddInputCommand());
            ContextHolder.setCommandData(message.getChatId(), sectionId);
            return MessageToUser.builder()
                    .text("Раздел «" + secOpt.get().getTitle() + "»\n\nВведите название нового подраздела.")
                    .keyboardMarkup(ManualEditAbortCommand.awaitInputMarkup())
                    .needRewriting(true)
                    .build();
        } catch (NumberFormatException e) {
            return MessageToUser.builder()
                    .text("Некорректный идентификатор.")
                    .needRewriting(true)
                    .build();
        } catch (InternalException e) {
            return MessageToUser.builder()
                    .text("Ошибка загрузки раздела.")
                    .needRewriting(true)
                    .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean isAvailableForStatus(StudentStatus status) {
        return true;
    }
}
