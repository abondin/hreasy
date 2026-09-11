package ru.abondin.hreasy.platform;


import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Business Error Exception.
 * Give the client clear reason of error.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BusinessError extends RuntimeException {


    /**
     * Try to use methods from {@link BusinessErrorFactory} instead of directly using this constructor
     * @param code
     * @param localizationArgs
     */
    public BusinessError(String code, String... localizationArgs) {
        this(HttpStatus.UNPROCESSABLE_ENTITY, code, localizationArgs);
    }

    /**
     * Creates a localized business error with a non-default HTTP status.
     */
    public BusinessError(HttpStatus status, String code, String... localizationArgs) {
        this.status = status;
        this.code = code;
        this.localizationArgs = localizationArgs;
    }

    private HttpStatus status;

    /**
     * Error code.
     * Uses in localization process.
     * Sends to the client side
     */
    private String code;
    /**
     * Arguments to resolve full message from the localization template
     */
    String[] localizationArgs;
    /**
     * Additional attributes to send to the client
     */
    Map<String, Object> attrs;
    /**
     * Default message if no localization found
     */
    String defaultMessage;


}
