package ru.abondin.hreasy.platform.api;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AnyExceptionDto {
    private final String message;

    public AnyExceptionDto(Throwable ex) {
        this(ExceptionUtils.getMessage(ex));
    }
}
