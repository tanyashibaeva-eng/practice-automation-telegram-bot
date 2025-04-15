package ru.itmo;

import lombok.extern.java.Log;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import ru.itmo.bot.PracticeAutomationBot;
import ru.itmo.infra.storage.TelegramUserRepository;
import ru.itmo.util.PropertiesProvider;
import ru.itmo.application.AdminTokenService;

@Log
public class Main {
    public static void main(String[] args) {
        final String token = PropertiesProvider.getToken();

        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {

            Class.forName("ru.itmo.infra.storage.DatabaseManager");

            botsApplication.registerBot(token, new PracticeAutomationBot());
            log.info("Bot is up and ready to receive messages");

            if (TelegramUserRepository.getAdminsCount() == 0) {
                log.severe("Admin token: " + AdminTokenService.generateToken().getToken());
            }

            Thread.currentThread().join();
        } catch (Exception ex) {
            log.severe(ex.getMessage());
        }
    }
}