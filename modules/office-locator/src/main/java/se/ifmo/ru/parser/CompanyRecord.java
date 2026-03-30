package se.ifmo.ru.parser;

import lombok.Value;

@Value
public class CompanyRecord {
    private String name;
    private String INN;
    private String address;
}
