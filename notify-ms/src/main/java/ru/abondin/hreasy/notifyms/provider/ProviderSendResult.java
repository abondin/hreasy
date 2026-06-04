package ru.abondin.hreasy.notifyms.provider;

public record ProviderSendResult(
        boolean success,
        boolean retryable,
        String externalMessageId,
        String statusCode,
        String errorCode,
        String errorMessage
) {
    public static ProviderSendResult success(String externalMessageId, String statusCode) {
        return new ProviderSendResult(true, false, externalMessageId, statusCode, null, null);
    }

    public static ProviderSendResult retryable(String statusCode, String errorCode, String errorMessage) {
        return new ProviderSendResult(false, true, null, statusCode, errorCode, errorMessage);
    }

    public static ProviderSendResult permanent(String statusCode, String errorCode, String errorMessage) {
        return new ProviderSendResult(false, false, null, statusCode, errorCode, errorMessage);
    }
}
