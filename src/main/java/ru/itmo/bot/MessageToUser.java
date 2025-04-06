package ru.itmo.bot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MessageToUser {
    String text;
    File document;
}

