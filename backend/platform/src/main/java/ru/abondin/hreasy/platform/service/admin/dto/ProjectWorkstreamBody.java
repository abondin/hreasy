package ru.abondin.hreasy.platform.service.admin.dto;

/** Editable workstream properties; the server assigns identity and audit metadata. */
public record ProjectWorkstreamBody(String externalId, String displayName, String description, boolean archived) {
}
