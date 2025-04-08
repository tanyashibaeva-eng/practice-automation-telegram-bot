package ru.itmo.domain.dto.command;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class StudentRegistrationArgs {
    long chatId;
    String eduStreamName;
    int isu;
}
