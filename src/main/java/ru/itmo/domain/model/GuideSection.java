package ru.itmo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GuideSection {
    private final int id;
    private final String slug;
    private final String title;
    private final int menuOrder;
    private final String command;
    private final boolean active;
    private final boolean hidden;
}
