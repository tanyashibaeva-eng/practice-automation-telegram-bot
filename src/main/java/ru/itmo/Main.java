package ru.itmo;

import lombok.extern.java.Log;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import ru.itmo.util.PropertiesProvider;

@Log
public class Main {
    public static void main(String[] args) {
        String token = PropertiesProvider.getToken();
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(token, new Bot());
            Thread.currentThread().join();
        } catch (Exception ex) {
            log.severe(ex.getMessage());
        }
    }
}