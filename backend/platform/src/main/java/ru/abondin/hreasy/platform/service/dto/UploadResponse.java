package ru.abondin.hreasy.platform.service.dto;

import lombok.Data;

/**
 * Generic HTTP response if file was uploaded
 */
@Data
public class UploadResponse {
    private String status = "OK";
}
