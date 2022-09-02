package ru.abondin.hreasy.platform.repo.vacation;

import lombok.Data;

import java.util.List;

@Data
public class VacationViewWithManagers extends VacationView {
    private List<String> managersEmails;
}
