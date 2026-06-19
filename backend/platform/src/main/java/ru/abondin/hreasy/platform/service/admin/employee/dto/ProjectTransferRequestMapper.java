package ru.abondin.hreasy.platform.service.admin.employee.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.abondin.hreasy.platform.repo.employee.projecttransfer.ProjectTransferRequestEntry;
import ru.abondin.hreasy.platform.repo.employee.projecttransfer.ProjectTransferRequestView;
import ru.abondin.hreasy.platform.repo.manager.ManagerRecipient;

import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public interface ProjectTransferRequestMapper {

    CurrentProjectTransferApproverDto fromApprover(ManagerRecipient entry);

    CurrentProjectTransferRequestDto fromView(ProjectTransferRequestView entry);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employeeId", source = "employeeId")
    @Mapping(target = "fromProjectId", source = "body.fromProjectId")
    @Mapping(target = "toProjectId", source = "body.toProjectId")
    @Mapping(target = "requestedProjectRole", source = "body.role")
    @Mapping(target = "approverEmployeeId", source = "body.approverEmployeeId")
    @Mapping(target = "state", constant = "1")
    @Mapping(target = "decisionComment", ignore = true)
    @Mapping(target = "appliedEmployeeHistoryId", ignore = true)
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    ProjectTransferRequestEntry toPendingEntry(CurrentProjectTransferApprovalRequestBody body,
                                               int employeeId,
                                               OffsetDateTime createdAt,
                                               Integer createdBy);

}
