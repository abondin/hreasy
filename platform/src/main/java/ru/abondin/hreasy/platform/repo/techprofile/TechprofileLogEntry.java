package ru.abondin.hreasy.platform.repo.techprofile;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

/**
 * Log upload and delete tech profiles to database
 */
@Data
@Table("techprofile.techprofile_log")
public class TechprofileLogEntry {
    @Id
    private Integer id;
    private int employee;
    private String filename;
    private long contentLength;
    private OffsetDateTime createdAt;
    private int createdBy;
    private OffsetDateTime deletedAt;
    private int deletedBy;
}
