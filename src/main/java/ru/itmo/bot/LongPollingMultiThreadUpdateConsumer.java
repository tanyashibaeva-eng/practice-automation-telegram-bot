package ru.itmo.bot;

import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public interface LongPollingMultiThreadUpdateConsumer extends LongPollingUpdateConsumer {
    Executor updatesProcessorExecutor = Executors.newCachedThreadPool();

    default void consume(List<Update> updates) {
        updates.forEach(
                (update) -> updatesProcessorExecutor.execute(
                        () -> this.consume(update)
                )
        );
    }

    void consume(Update update);
}
