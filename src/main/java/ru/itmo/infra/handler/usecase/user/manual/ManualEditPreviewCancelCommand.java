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
import ru.itmo.exception.UnknownUserException;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.user.UserCommand;
import ru.itmo.infra.storage.GuideRepository;

@NoArgsConstructor
public class ManualEditPreviewCancelCommand implements UserCommand {

    public static final String COMMAND_NAME = "/manual_ed_back";

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
            Object raw = ContextHolder.getCommandData(message.getChatId());
            ManualEditContext ctx = ManualEditContext.fromCommandData(raw);
            if (ctx == null
                    || ctx.phase() != ManualEditContext.Phase.AWAIT_PREVIEW_CONFIRM
                    || ctx.subsectionId() != subsectionId) {
                ContextHolder.endCommand(message.getChatId());
                return MessageToUser.builder()
                        .text("Сессия устарела. Начните снова: /manual_edit")
                        .needRewriting(true)
                        .keyboardMarkup(Command.returnToStartInlineMarkup())
                        .build();
            }
            var subOpt = GuideRepository.findSubsectionById(subsectionId);
            if (subOpt.isEmpty()) {
                ContextHolder.endCommand(message.getChatId());
                return MessageToUser.builder()
                        .text("Подраздел не найден.")
                        .needRewriting(true)
                        .keyboardMarkup(Command.returnToStartInlineMarkup())
                        .build();
            }
            ContextHolder.setCommandData(message.getChatId(), ManualEditContext.awaitingInput(subsectionId));
            ContextHolder.setNextCommand(message.getChatId(), new ManualEditBodyInputCommand());
            return GuideService.manualEditRepromptAfterDraftCancel(subOpt.get());
        } catch (NumberFormatException e) {
            return MessageToUser.builder()
                    .text("Некорректный идентификатор.")
                    .needRewriting(true)
                    .build();
        } catch (UnknownUserException e) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text("Сессия не найдена. /manual_edit")
                    .needRewriting(true)
                    .keyboardMarkup(Command.returnToStartInlineMarkup())
                    .build();
        } catch (InternalException e) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text("Ошибка: " + e.getMessage())
                    .needRewriting(true)
                    .keyboardMarkup(Command.returnToStartInlineMarkup())
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
