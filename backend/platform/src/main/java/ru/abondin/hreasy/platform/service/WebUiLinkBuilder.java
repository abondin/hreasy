package ru.abondin.hreasy.platform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import ru.abondin.hreasy.platform.config.HrEasyCommonProps;

/**
 * Builds absolute links to web UI routes used outside the web application.
 */
@Component
@RequiredArgsConstructor
public class WebUiLinkBuilder {
    private static final String EMPLOYEE_PROJECT_CHANGE_PATH = "/employees/{employeeId}/changeCurrentProject";

    private final HrEasyCommonProps commonProps;

    public String employeeProjectChangeDialog(int employeeId) {
        return UriComponentsBuilder.fromUri(commonProps.getWebBaseUrl())
                .path(EMPLOYEE_PROJECT_CHANGE_PATH)
                .build(employeeId)
                .toString();
    }
}
