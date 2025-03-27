package ru.itmo;

import lombok.extern.java.Log;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.itmo.util.PropertiesProvider;

@Log
public class Bot implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient = new OkHttpTelegramClient(PropertiesProvider.getToken());

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            SendMessage sendMessage = SendMessage
                    .builder()
                    .chatId(chatId)
                    .text(text)
                    .build();

            try {
                telegramClient.execute(sendMessage);
            } catch (TelegramApiException ex) {
                log.severe(ex.getMessage());
            }
        }
    }
}
