package ru.abondin.hreasy.platform.repo.employee.projecttransfer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProjectTransferRequestView extends ProjectTransferRequestEntry {
    private String fromProjectName;
    private String toProjectName;
    private String createdByDisplayName;
    private String approverDisplayName;
}
