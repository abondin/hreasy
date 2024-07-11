package ru.abondin.hreasy.platform.service.udr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessErrorFactory;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.employee.EmployeeRepo;
import ru.abondin.hreasy.platform.repo.udr.UdrAccessRepo;
import ru.abondin.hreasy.platform.repo.udr.UdrEmployeeRepo;
import ru.abondin.hreasy.platform.repo.udr.UdrRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.udr.dto.UdrDto;
import ru.abondin.hreasy.platform.service.udr.dto.UdrMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class UdrService {

    private final DateTimeService dateTimeService;
    private final EmployeeRepo employeeRepo;
    private final UdrEmployeeRepo udrEmployeeRepo;
    private final UdrRepo udrRepo;
    private final UdrAccessRepo udrAccessRepo;
    private final UdrSecurityValidator securityValidator;
    private final UdrMapper mapper;

    public Mono<UdrDto> getUdr(AuthContext auth, int id) {
        log.info("Get UDR {} by {}", id, auth.getUsername());
        // 1. Validate if user have access to open UDR
        return securityValidator.validateViewUdr(auth, id)
                .flatMap(v ->
                        // 2. Get UDR
                        udrRepo.findById(id)
                                .switchIfEmpty(BusinessErrorFactory.entityNotFound("UDR", id))
                                .map(mapper::toDto));
    }

}
