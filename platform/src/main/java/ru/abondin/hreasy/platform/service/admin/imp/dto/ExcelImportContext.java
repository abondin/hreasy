package ru.abondin.hreasy.platform.service.admin.imp.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Locale;

/**
 * Uses in {@link ru.abondin.hreasy.platform.service.admin.employee.imp.AdminEmployeeImportProcessor}
 * @param <E> - Entry type
 */
public record ExcelImportContext<E>(List<E> entries, Locale locale) {}
