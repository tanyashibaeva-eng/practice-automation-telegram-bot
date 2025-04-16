package ru.itmo.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.itmo.exception.BadRequestException;
import ru.itmo.util.TextUtils;

import java.time.LocalDate;
import java.util.StringJoiner;

@Getter
@ToString
@EqualsAndHashCode
public class EduStream {
    private final String name;
    private Integer year;
    private LocalDate dateFrom;
    private LocalDate dateTo;

    public EduStream(String name) throws BadRequestException {
        name = trimName(name);
        if (name.isEmpty())
            throw new BadRequestException("Имя потока не может быть пустым");

        this.name = name;
    }

    public EduStream(String name, int year, LocalDate dateFrom, LocalDate dateTo) throws BadRequestException {
        StringJoiner stringJoiner = new StringJoiner("\n");

        name = trimName(name);
        if (name.isEmpty())
            stringJoiner.add("Имя потока не может быть пустым");

        if (year <= 0)
            stringJoiner.add("Год потока должен быть положительным числом");

        if (dateFrom.isAfter(dateTo))
            stringJoiner.add("Дата начала практики должна быть раньше даты конца практики");

        if (stringJoiner.length() != 0)
            throw new BadRequestException(stringJoiner.toString());

        this.name = name;
        this.year = year;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    private static String trimName(String name) {
        return (name == null)
                ? ""
                : TextUtils.removeRedundantSpaces(name);
    }
}
