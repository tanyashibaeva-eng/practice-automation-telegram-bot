package ru.itmo.application;

import ru.itmo.domain.model.AdminToken;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.storage.AdminTokenRepository;

public class AdminTokenService {

    public static AdminToken generateToken() throws InternalException {
        AdminToken adminToken = new AdminToken();
        AdminTokenRepository.save(adminToken);
        return adminToken;
    }

    public static boolean deleteToken(AdminToken adminToken) throws InternalException {
        return AdminTokenRepository.delete(adminToken);
    }

    public static boolean deleteAllTokens() throws InternalException {
        return AdminTokenRepository.deleteAll();
    }
}
