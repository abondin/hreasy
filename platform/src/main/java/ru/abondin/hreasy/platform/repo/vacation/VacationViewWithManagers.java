package ru.abondin.hreasy.platform.repo.vacation;

import lombok.Data;

import java.util.Set;

@Data
public class VacationViewWithManagers extends VacationView {
    private Set<String> managersEmails;
}
