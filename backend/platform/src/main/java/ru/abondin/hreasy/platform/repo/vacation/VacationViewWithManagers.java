package ru.abondin.hreasy.platform.repo.vacation;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class VacationViewWithManagers extends VacationView {
    private List<String> managersEmails;
}
