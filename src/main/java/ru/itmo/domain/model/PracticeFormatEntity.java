package ru.itmo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PracticeFormatEntity {
    private Long id;
    private String code;
    private String displayName;
    private boolean active;
}

