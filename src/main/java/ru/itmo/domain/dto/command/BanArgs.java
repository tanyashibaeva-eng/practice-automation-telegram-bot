package ru.itmo.domain.dto.command;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class BanArgs {
    long chatId;
}
