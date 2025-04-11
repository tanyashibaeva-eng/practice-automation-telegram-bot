package ru.itmo.domain.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.itmo.domain.type.PracticePlace;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class ITMOPracticeInfoUpdateArgs {
    private long chatId;
    private PracticePlace practicePlace;
    private String companyName; // название подразделения в ИТМО
    private String companyLeadFullName; // ФИО куратора полностью
}
