package ru.abondin.hreasy.platform;


import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Business Error Exception.
 * Give the client clear reason of error.
 */
@Data
public class BusinessError extends RuntimeException {


    public BusinessError(String code, List<String> localizationArgs) {
        this.code = code;
        this.localizationArgs = localizationArgs;
    }

    public BusinessError(String code, String... localizationArgs) {
        this(code, localizationArgs == null ? new ArrayList<>() : Arrays.asList(localizationArgs));
    }

    /**
     * Error code.
     * Uses in localization process.
     * Sends to the client side
     */
    private String code;
    /**
     * Arguments to resolve full message from the localization template
     */
    List<String> localizationArgs;
    /**
     * Additional attributes to send to the client
     */
    Map<String, String> attrs;
    /**
     * Default message if no localization found
     */
    String defaultMessage;


}
