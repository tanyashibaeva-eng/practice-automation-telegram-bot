package ru.itmo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeOption {
    private Long id;
    private String title;
    private boolean enabled;
    private boolean requiresItmoInfo;
    private boolean requiresCompanyInfo;
}
