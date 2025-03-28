package ru.itmo;

import lombok.extern.java.Log;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import ru.itmo.util.PropertiesProvider;

@Log
public class Main {
    public static void main(String[] args) {
        final String token = PropertiesProvider.getToken();

        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(token, new Bot());
            log.info("Bot is up and ready to receive messages");
            Thread.currentThread().join();
        } catch (Exception ex) {
            log.severe(ex.getMessage());
        }
    }
}