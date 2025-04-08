package ru.itmo.application;

import org.junit.jupiter.api.*;
import ru.itmo.domain.model.AdminToken;
import ru.itmo.domain.model.TelegramUser;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;

import java.util.Optional;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminRegistrationTest {

    private static TelegramUser telegramUser = new TelegramUser(1, false, false, "user");
    private static final AdminToken token;

    static {
        try {
            token = AdminTokenService.generateToken();
        } catch (InternalException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Order(1)
    @Test
    void createAdminTokenFromStringTest_invalidFormat_fail() {
        String invalidFormatTokenString = "abcd";
        Assertions.assertThrows(
                BadRequestException.class,
                () -> new AdminToken(invalidFormatTokenString)
        );
    }

    @Order(2)
    @Test
    void registerAdminTest_invalidToken_fail() throws BadRequestException {
        String tokenString = "11111111-1111-1111-1111-111111111111";
        AdminToken invalidToken = new AdminToken(tokenString);

        Assertions.assertThrows(
                BadRequestException.class,
                () -> TelegramUserService.registerAdmin(telegramUser, invalidToken)
        );
    }

    @Order(3)
    @Test
    void registerAdminTest_validToken_ok() throws InternalException {

        Assertions.assertDoesNotThrow(() -> TelegramUserService.registerAdmin(telegramUser, token));

        Optional<TelegramUser> resultUser = TelegramUserService.findByChatId(telegramUser.getChatId());
        Assertions.assertTrue(resultUser.isPresent() && resultUser.get().isAdmin());

        telegramUser = resultUser.get();
    }

    @Order(4)
    @Test
    void registerAdminTest_usedToken_fail() {

        Assertions.assertThrows(
                BadRequestException.class,
                () -> TelegramUserService.registerAdmin(telegramUser, token)
        );
    }

    @Order(5)
    @Test
    void deleteAdminTest_ok() throws InternalException, BadRequestException {
        Assertions.assertTrue(TelegramUserService.deleteAdmin(telegramUser));
        Assertions.assertTrue(TelegramUserService.findByChatId(telegramUser.getChatId()).isEmpty());
    }

}
