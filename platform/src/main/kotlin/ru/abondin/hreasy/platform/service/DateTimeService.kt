package ru.abondin.hreasy.platform.service

import org.springframework.stereotype.Component
import java.time.OffsetDateTime

/**
 * Service to deal with dates and time.
 * Can be used to do some actions in the past or in the future if required
 */
@Component
class DateTimeService {
    fun now() = OffsetDateTime.now();
}