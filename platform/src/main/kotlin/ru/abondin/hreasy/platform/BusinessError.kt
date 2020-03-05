package ru.abondin.hreasy.platform

/**
 * Business Error Exception.
 * Give the client clear reason of error.
 */
class BusinessError(
        /**
         * Error code.
         * Uses in localization process.
         * Sends to the client side
         */
        val code: String,
        /**
         * Arguments to resolve full message from the localization template
         */
        val localizationArgs: Array<out Any>,
        /**
         * Additional attributes to send to the client
         */
        val attrs: Map<String, String>,
        /**
         * Default message if no localization found
         */
        val defaultMessage: String?,
        /**
         * Business Error Cause
         */
        cause: Exception?) : RuntimeException(defaultMessage, cause) {
    constructor(code: String, localizationArgs: Array<out Any>, cause: Exception?) : this(code, localizationArgs, mapOf(), null, cause);
    constructor(code: String, cause: Exception?) : this(code, arrayOf(), cause);
    constructor(code: String, localizationArgs: Array<out Any>) : this(code, localizationArgs, null)
 }