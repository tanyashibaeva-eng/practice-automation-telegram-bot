package ru.itmo.domain.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class UserRegistrationArgs {
    private long chatId;
    private String username;
    private String eduStreamName;
    private int isu;
}
