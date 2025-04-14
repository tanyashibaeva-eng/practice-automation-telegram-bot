package ru.itmo.util;

import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.domain.type.PracticePlace;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.BadRequestException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TextParser {
    public static int parseIsu(String text) throws BadRequestException {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException("Номер ИСУ должен быть числом");
        }
    }

    public static int parseDoubleToInt(String text) throws BadRequestException {
        try {
            return (int) Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException("должно быть числом");
        }
    }

    public static long parseDoubleToLong(String text) throws BadRequestException {
        try {
            return (long) Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException("должно быть числом");
        }
    }

    public static String parsePhone(String text) throws BadRequestException {
        if (text == null || text.trim().isEmpty()) {
            throw new BadRequestException("должно быть строкой, представляющей номер телефона.");
        }

        String phoneRegex = "^((8|\\+7)[\\- ]?)?(\\(?\\d{3}\\)?[\\- ]?)?[\\d\\- ]{7,10}$";
        if (!text.matches(phoneRegex)) {
            throw new BadRequestException("Неверный формат номера телефона");
        }
        return text.trim();
    }

    public static String parseEmail(String text) throws BadRequestException {
        if (text == null || text.trim().isEmpty()) {
            throw new BadRequestException("должно быть строкой, представляющей email.");
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (!text.matches(emailRegex)) {
            throw new BadRequestException("Неверный формат email");
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

    public static LocalDate parseDate(String text) throws BadRequestException {
        if (text == null || text.trim().isEmpty()) {
            throw new BadRequestException("должно быть строкой, представляющей дату в формате чч.мм.гггг.");
        }

        var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        try {
            return LocalDate.parse(text.trim(), formatter);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Неверный формат даты. Ожидается чч.мм.гггг");
        }
    }
}
