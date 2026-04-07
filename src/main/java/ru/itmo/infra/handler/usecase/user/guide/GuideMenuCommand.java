package ru.itmo.infra.handler.usecase.user.guide;

import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.storage.GuideRepository;

@NoArgsConstructor
public class GuideMenuCommand implements Command {

    public static final String COMMAND_NAME = "/manual_open";

    @Override
    public MessageToUser execute(MessageDTO message) {
        ContextHolder.endCommand(message.getChatId());
        try {
            var sections = GuideRepository.findAllActiveSectionsVisibleInMenuOrdered();
            var markup = InlineKeyboardMarkup.builder();
            for (var sec : sections) {
                String label = sec.getTitle().length() <= 64 ? sec.getTitle() : sec.getTitle().substring(0, 61) + "...";
                markup.keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(label)
                                .callbackData(sec.getCommand())
                                .build()
                ));
            }
            markup.keyboardRow(new InlineKeyboardRow(
                    InlineKeyboardButton.builder()
                            .text(ru.itmo.infra.handler.usecase.Command.returnIcon + " Вернуться в меню")
                            .callbackData("/start")
                            .build()
            ));
            return MessageToUser.builder()
                    .text("Справка: выберите раздел.")
                    .keyboardMarkup(markup.build())
                    .needRewriting(true)
                    .build();
        } catch (InternalException e) {
            return MessageToUser.builder()
                    .text("Не удалось загрузить разделы.")
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
        return "Открыть мануал";
    }
}
