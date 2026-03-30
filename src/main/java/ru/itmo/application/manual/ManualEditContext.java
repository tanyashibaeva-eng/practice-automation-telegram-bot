package ru.itmo.application.manual;

public record ManualEditContext(int subsectionId, String draftBody, Phase phase) {

    public enum Phase {
        AWAIT_NEW_BODY,
        AWAIT_PREVIEW_CONFIRM
    }

    public static ManualEditContext awaitingInput(int subsectionId) {
        return new ManualEditContext(subsectionId, null, Phase.AWAIT_NEW_BODY);
    }

    public ManualEditContext withDraft(String draft) {
        return new ManualEditContext(subsectionId, draft, Phase.AWAIT_PREVIEW_CONFIRM);
    }

    public static ManualEditContext fromCommandData(Object data) {
        if (data instanceof ManualEditContext c) {
            return c;
        }
        return null;
    }
}
