package ru.itmo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GuideSubsection {
    private final int id;
    private final int sectionId;
    private final String title;
    private final String body;
    private final Integer prevSubsectionId;
    private final Integer nextSubsectionId;
    private final int itemOrder;
}
