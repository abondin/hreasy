package ru.abondin.hreasy.platform.service.admin;

import org.junit.jupiter.api.Test;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.DictProjectHistoryRepo;
import ru.abondin.hreasy.platform.repo.dict.DictProjectRepo;
import ru.abondin.hreasy.platform.repo.dict.ProjectWorkstreamRepo;
import ru.abondin.hreasy.platform.repo.employee.admin.SecAdminUserRolesRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.dto.ProjectDto;
import ru.abondin.hreasy.platform.service.admin.dto.ProjectDtoMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ProjectAdminServiceTest {

    @Test
    void rejectsRequiredWorkstreamSelectionWhenCreatingProject() {
        var projectRepo = mock(DictProjectRepo.class);
        var historyRepo = mock(DictProjectHistoryRepo.class);
        var workstreamRepo = mock(ProjectWorkstreamRepo.class);
        var service = new ProjectAdminService(projectRepo, historyRepo, mock(DateTimeService.class),
                mock(AdminSecurityValidator.class), mock(ProjectDtoMapper.class),
                mock(SecAdminUserRolesRepo.class), workstreamRepo);
        var body = new ProjectDto.CreateOrUpdateProjectDto();
        body.setName("Example project");
        body.setDepartmentId(301);
        body.setWorkstreamRequired(true);
        var auth = new AuthContext("admin", "admin@example.test", List.of(),
                new AuthContext.EmployeeInfo(201, null, null, List.of(), List.of(), List.of(), null, null));
        var createError = org.junit.jupiter.api.Assertions.assertThrows(ru.abondin.hreasy.platform.BusinessError.class,
                () -> service.create(auth, body));
        assertEquals("errors.project.workstream.required", createError.getCode());
        org.mockito.Mockito.verifyNoInteractions(projectRepo, historyRepo, workstreamRepo);
    }

    @Test
    void mapsExplicitWorkstreamRequirement() {
        var mapper = org.mapstruct.factory.Mappers.getMapper(ProjectDtoMapper.class);
        var body = new ProjectDto.CreateOrUpdateProjectDto();
        body.setName("Example project");
        body.setDepartmentId(301);
        body.setWorkstreamRequired(false);
        assertEquals(false, mapper.fromDto(body).isWorkstreamRequired());
        body.setWorkstreamRequired(true);
        var entry = mapper.fromDto(body);
        entry.setId(501);
        assertEquals(true, entry.isWorkstreamRequired());
        assertEquals(true, mapper.partialCopyHistory(entry).isWorkstreamRequired());
        body.setWorkstreamRequired(false);
        mapper.apply(entry, body);
        assertEquals(false, entry.isWorkstreamRequired());
    }

    @Test
    void requiresExplicitWorkstreamRequirement() {
        try (var factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var body = new ProjectDto.CreateOrUpdateProjectDto();
            assertEquals(1, validator.validateProperty(body, "workstreamRequired").size());
            body.setWorkstreamRequired(false);
            assertEquals(0, validator.validateProperty(body, "workstreamRequired").size());
            body.setWorkstreamRequired(true);
            assertEquals(0, validator.validateProperty(body, "workstreamRequired").size());
        }
    }

}
