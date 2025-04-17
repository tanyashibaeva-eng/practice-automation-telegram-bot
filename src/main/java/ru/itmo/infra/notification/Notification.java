package ru.itmo.infra.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor
public class Notification {
    private static final int MAX_RETRY_COUNT = 5;

    private long chatId;
    private String text;
    @Setter
    private int retryCount;

    public boolean countRetry() {
        return retryCount++ < MAX_RETRY_COUNT;
    }
}
