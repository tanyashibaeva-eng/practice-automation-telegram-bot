package ru.itmo.bot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CallbackData {
    private String command;
    private String key;
    private String value;

    public CallbackData(String callbackDataString) {
        var fields = callbackDataString.split("#");
        if (fields.length > 0) {
            command = fields[0];
        }
        if (fields.length > 1) {
            key = fields[1];
        }
        if (fields.length > 2) {
            value = fields[2];
        }
    }

    public String toString() {
        return command + "#" + (key == null ? "" : key) + "#" + (value == null ? "" : value);
    }
}
