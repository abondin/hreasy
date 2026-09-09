package ru.abondin.hreasy.platform.service.dto;

/**
 * Active project workstream exposed to web and external consumers.
 */
public record ProjectWorkstreamDto(Integer id, String externalId, String displayName, String description) {
}
