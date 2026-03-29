package ru.itmo.infra.handler.usecase.user.manual;

import lombok.NoArgsConstructor;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.manual.ManualEditContext;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.exception.UnknownUserException;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.user.UserCommand;
import ru.itmo.infra.storage.GuideRepository;

@NoArgsConstructor
public class ManualEditConfirmCommand implements UserCommand {

    public static final String COMMAND_NAME = "/manual_ed_ok";

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
                    || ctx.draftBody() == null
                    || ctx.subsectionId() != subsectionId) {
                ContextHolder.endCommand(message.getChatId());
                return MessageToUser.builder()
                        .text("Сессия устарела или не совпадает с кнопкой. Начните снова: /manual_edit")
                        .needRewriting(true)
                        .keyboardMarkup(Command.returnToStartInlineMarkup())
                        .build();
            }
            GuideRepository.updateSubsectionBody(subsectionId, ctx.draftBody());
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text("Содержимое подраздела обновлено.")
                    .needRewriting(true)
                    .keyboardMarkup(Command.returnToStartInlineMarkup())
                    .build();
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
        } catch (BadRequestException e) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text(e.getMessage())
                    .needRewriting(true)
                    .keyboardMarkup(Command.returnToStartInlineMarkup())
                    .build();
        } catch (InternalException e) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text("Не удалось сохранить: " + e.getMessage())
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
