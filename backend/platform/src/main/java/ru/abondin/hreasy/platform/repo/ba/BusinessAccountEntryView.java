package ru.abondin.hreasy.platform.repo.ba;

import io.r2dbc.postgresql.codec.Json;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Business account with joined additional information
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BusinessAccountEntryView extends BusinessAccountEntry {
    private Json managersJson;
}
