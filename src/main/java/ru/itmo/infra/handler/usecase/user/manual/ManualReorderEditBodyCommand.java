package ru.itmo.infra.handler.usecase.user.manual;

import lombok.NoArgsConstructor;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.GuideService;
import ru.itmo.application.manual.ManualEditContext;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.handler.usecase.user.UserCommand;
import ru.itmo.infra.storage.GuideRepository;

@NoArgsConstructor
public class ManualReorderEditBodyCommand implements UserCommand {

    public static final String COMMAND_NAME = "/manual_r_ed";

    @Override
    public MessageToUser execute(MessageDTO message) {
        var cd = new CallbackData(message.getText());
        if (!"subsectionId".equals(cd.getKey()) || cd.getValue() == null || cd.getValue().isBlank()) {
            return MessageToUser.builder()
                    .text("Некорректные данные.")
                    .needRewriting(true)
                    .build();
        }
        try {
            int subsectionId = Integer.parseInt(cd.getValue().trim());
            var subOpt = GuideRepository.findSubsectionById(subsectionId);
            if (subOpt.isEmpty()) {
                return MessageToUser.builder()
                        .text("Подраздел не найден.")
                        .needRewriting(true)
                        .build();
            }
            ContextHolder.endCommand(message.getChatId());
            ContextHolder.setNextCommand(message.getChatId(), new ManualEditBodyInputCommand());
            ContextHolder.setCommandData(message.getChatId(), ManualEditContext.awaitingInput(subsectionId));
            return GuideService.manualEditPromptWithCurrentView(subOpt.get());
        } catch (NumberFormatException e) {
            return MessageToUser.builder()
                    .text("Некорректный идентификатор.")
                    .needRewriting(true)
                    .build();
        } catch (InternalException e) {
            return MessageToUser.builder()
                    .text("Ошибка загрузки подраздела.")
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
