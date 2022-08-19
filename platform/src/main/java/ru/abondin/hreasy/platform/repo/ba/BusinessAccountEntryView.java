package ru.abondin.hreasy.platform.repo.ba;

import lombok.Data;

/**
 * Business account with joined additional information
 */
@Data
public class BusinessAccountEntryView extends BusinessAccountEntry{
    private String responsibleEmployeeName;
}
