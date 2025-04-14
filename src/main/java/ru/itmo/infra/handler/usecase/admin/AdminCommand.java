package ru.itmo.infra.handler.usecase.admin;

import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.UnknownUserException;
import ru.itmo.infra.handler.usecase.Command;

import java.util.Optional;
import java.util.regex.Pattern;

public interface AdminCommand extends Command {
    @Override
    default boolean isAdminCommand() {
        return true;
    }

    default MessageToUser returnToMainMenuWithError(long chatId, String errorMessage) {
        ContextHolder.endCommand(chatId);
        return MessageToUser.builder()
                .text(errorMessage)
                .build();
    }

    default Optional<String> getEduStreamNameFromMessage(String message) {
        var pattern = Pattern.compile("^/[a-z_]+ +(.+?)$");
        var matcher = pattern.matcher(message);

        if (matcher.matches()) {
            return Optional.of(matcher.group(1).trim().replaceAll("\"", "").replaceAll(" +", " "));
        }
        return Optional.empty();
    }

    default String getEduStreamNameOrThrow(MessageDTO message) throws BadRequestException, UnknownUserException {
        var streamNameOpt = getEduStreamNameFromMessage(message.getText());
        if (streamNameOpt.isPresent()) {
            String streamName = streamNameOpt.get();
            ContextHolder.setEduStreamName(message.getChatId(), streamName);
            return streamName;
        } else if (message.getText().trim().replace(getName(), "").isEmpty()) {
            throw new BadRequestException("Неверный формат команды: %s".formatted(getDescription()));
        } else {
           return ContextHolder.getEduStreamName(message.getChatId());
        }
    }
}
