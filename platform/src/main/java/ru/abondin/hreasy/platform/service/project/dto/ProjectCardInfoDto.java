package ru.abondin.hreasy.platform.service.project.dto;

import lombok.Data;
import org.springframework.lang.Nullable;
import ru.abondin.hreasy.platform.service.dto.ManagerInfoDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import org.springframework.lang.NonNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProjectCardInfoDto {
    private int id;

    @NonNull
    private String name;

    @Nullable
    private String customer;

    @Nullable
    private LocalDate startDate;
    @Nullable
    private LocalDate endDate;
    @Nullable
    private LocalDate planStartDate;
    @Nullable
    private LocalDate planEndDate;

    @NonNull
    private SimpleDictDto department;

    @Nullable
    private SimpleDictDto businessAccount;

    private boolean active;

    private String info;

    private List<ManagerInfoDto> managers = new ArrayList<>();
    private List<ManagerInfoDto> baManagers = new ArrayList<>();
}
