package ru.itmo.util;

import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.domain.type.PracticePlace;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.BadRequestException;

public class TextParser {
    public static int parseInt(String text) throws BadRequestException {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException("должно быть числом");
        }
    }

    public static int parseDoubleToInt(String text) throws BadRequestException {
        try {
            return (int) Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException("должно быть числом");
        }
    }

    public static String parsePhone(String text) throws BadRequestException {
        if (text == null || text.trim().isEmpty()) {
            throw new BadRequestException("должно быть строкой, представляющей номер телефона.");
        }

        String phoneRegex = "^\\+?(?:[0-9] ?){6,14}[0-9]$";
        if (!text.matches(phoneRegex)) {
            throw new BadRequestException("неверный формат номера телефона");
        }
        return text.trim();
    }

    public static String parseEmail(String text) throws BadRequestException {
        if (text == null || text.trim().isEmpty()) {
            throw new BadRequestException("должно быть строкой, представляющей email.");
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (!text.matches(emailRegex)) {
            throw new BadRequestException("неверный формат email");
        }
        return text.trim();
    }

    public static StudentStatus parseStatus(String text) throws BadRequestException {
        if (text == null || text.trim().isEmpty()) {
            throw new BadRequestException("должно быть строкой, представляющей статус.");
        }
        var enumVal = StudentStatus.getByUserName(text);
        if (enumVal == null) {
            throw new BadRequestException("неверный статус");
        }
        return enumVal;
    }

    public static PracticeFormat parsePracticeFormat(String text) throws BadRequestException {
        if (text == null || text.trim().isEmpty()) {
            throw new BadRequestException("должно быть строкой, представляющей формат прохождения практики.");
        }
        return PracticeFormat.getByUserName(text);
    }

    public static PracticePlace parsePracticePlace(String text) throws BadRequestException {
        if (text == null || text.trim().isEmpty()) {
            throw new BadRequestException("должно быть строкой, представляющей место прохождения практики.");
        }
        return PracticePlace.getByUserName(text);
    }
}
