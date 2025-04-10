package ru.itmo.domain.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class ITMOPracticeInfoUpdateArgs {
    private long chatId;
    private String companyName; // название подразделения в ИТМО
    private String companyLeadFullname; // ФИО куратора полностью
}
