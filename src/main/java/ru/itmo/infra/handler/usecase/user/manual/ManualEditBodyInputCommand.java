package ru.itmo.infra.handler.usecase.user.manual;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.GuideService;
import ru.itmo.application.TelegramEntitiesToHtml;
import ru.itmo.application.manual.ManualEditContext;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.exception.InternalException;
import ru.itmo.exception.UnknownUserException;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.storage.GuideRepository;

public class ManualEditBodyInputCommand implements Command {

    @Override
    public MessageToUser execute(MessageDTO message) {
        ManualEditContext ctx;
        try {
            Object raw = ContextHolder.getCommandData(message.getChatId());
            ctx = ManualEditContext.fromCommandData(raw);
            if (ctx == null) {
                ContextHolder.endCommand(message.getChatId());
                return staleSessionMessage();
            }
        } catch (UnknownUserException e) {
            return MessageToUser.builder()
                    .text("Сессия редактирования не найдена. /manual_edit")
                    .needRewriting(true)
                    .keyboardMarkup(Command.returnToStartInlineMarkup())
                    .build();
        }

        if (ctx.phase() == ManualEditContext.Phase.AWAIT_PREVIEW_CONFIRM) {
            return MessageToUser.builder()
                    .text("Сначала подтвердите или отмените предпросмотр кнопками под предыдущим сообщением.")
                    .parseMode("HTML")
                    .needRewriting(true)
                    .build();
        }

        String text = message.getText() == null ? "" : message.getText();
        if (text.startsWith("/")) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text("Редактирование отменено. Снова: /manual_edit (средняя кнопка в списке подразделов).")
                    .needRewriting(true)
                    .keyboardMarkup(Command.returnToStartInlineMarkup())
                    .build();
        }

        String draft;
        if (message.getEntities() != null && !message.getEntities().isEmpty()) {
            draft = TelegramEntitiesToHtml.convert(text, message.getEntities());
        } else {
            draft = text.stripLeading();
        }
        if (draft.isEmpty()) {
            return MessageToUser.builder()
                    .text("Текст пустой. Отправьте непустой текст или отмените строкой, начинающейся с /.")
                    .needRewriting(true)
                    .build();
        }

        try {
            var subOpt = GuideRepository.findSubsectionById(ctx.subsectionId());
            if (subOpt.isEmpty()) {
                ContextHolder.endCommand(message.getChatId());
                return MessageToUser.builder()
                        .text("Подраздел не найден.")
                        .needRewriting(true)
                        .keyboardMarkup(Command.returnToStartInlineMarkup())
                        .build();
            }
            var sub = subOpt.get();
            ContextHolder.setCommandData(message.getChatId(), ctx.withDraft(draft));
            ContextHolder.setNextCommand(message.getChatId(), new ManualEditPreviewGuardCommand());

            String previewBlock = GuideService.previewSubsectionHtml(sub, draft);
            String full = GuideService.wrapManualEditPreviewHtml(previewBlock);

            return MessageToUser.builder()
                    .text(full)
                    .parseMode("HTML")
                    .keyboardMarkup(ManualEditPreviewKeyboard.build(ctx.subsectionId()))
                    .needRewriting(true)
                    .build();
        } catch (InternalException e) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text("Не удалось загрузить подраздел: " + e.getMessage())
                    .needRewriting(true)
                    .keyboardMarkup(Command.returnToStartInlineMarkup())
                    .build();
        }
    }

    private static MessageToUser staleSessionMessage() {
        return MessageToUser.builder()
                .text("Сессия редактирования устарела. Начните снова: /manual_edit")
                .needRewriting(true)
                .keyboardMarkup(Command.returnToStartInlineMarkup())
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "";
    }

    static final class ManualEditPreviewKeyboard {
        private ManualEditPreviewKeyboard() {
        }

        static InlineKeyboardMarkup build(int subsectionId) {
            String sid = String.valueOf(subsectionId);
            return InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(
                            InlineKeyboardButton.builder()
                                    .text("✅ Подтвердить")
                                    .callbackData(CallbackData.builder()
                                            .command(ManualEditConfirmCommand.COMMAND_NAME)
                                            .key("subsectionId")
                                            .value(sid)
                                            .build()
                                            .toString())
                                    .build(),
                            InlineKeyboardButton.builder()
                                    .text("❌ Отменить")
                                    .callbackData(CallbackData.builder()
                                            .command(ManualEditPreviewCancelCommand.COMMAND_NAME)
                                            .key("subsectionId")
                                            .value(sid)
                                            .build()
                                            .toString())
                                    .build()
                    ))
                    .keyboardRow(new InlineKeyboardRow(
                            InlineKeyboardButton.builder()
                                    .text(Command.returnIcon + " В меню")
                                    .callbackData(CallbackData.builder()
                                            .command("/start")
                                            .build()
                                            .toString())
                                    .build()
                    ))
                    .build();
        }
    }
}
