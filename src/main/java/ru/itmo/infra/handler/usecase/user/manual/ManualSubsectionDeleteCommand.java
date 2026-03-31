package ru.itmo.infra.handler.usecase.user.manual;

import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.itmo.application.SimpleMarkdownToTelegramHtml;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.handler.usecase.user.UserCommand;
import ru.itmo.infra.storage.GuideRepository;

@NoArgsConstructor
public class ManualSubsectionDeleteCommand implements UserCommand {

    public static final String COMMAND_NAME = "/manual_sub_del";

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
            var sub = subOpt.get();
            var markup = InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(
                            InlineKeyboardButton.builder()
                                    .text("✅ Удалить")
                                    .callbackData(CallbackData.builder()
                                            .command(ManualSubsectionDeleteConfirmCommand.COMMAND_NAME)
                                            .key("subsectionId")
                                            .value(String.valueOf(subsectionId))
                                            .build()
                                            .toString())
                                    .build(),
                            InlineKeyboardButton.builder()
                                    .text("✖ Отмена")
                                    .callbackData(CallbackData.builder()
                                            .command(ManualEditSectionCommand.COMMAND_NAME)
                                            .key("sectionId")
                                            .value(String.valueOf(sub.getSectionId()))
                                            .build()
                                            .toString())
                                    .build()
                    ))
                    .build();
            return MessageToUser.builder()
                    .text(SimpleMarkdownToTelegramHtml.escapeHtml(
                            "Удалить подраздел «" + sub.getTitle() + "»? Это действие нельзя отменить."))
                    .parseMode("HTML")
                    .keyboardMarkup(markup)
                    .needRewriting(true)
                    .build();
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
