package ru.abondin.hreasy.platform.service.ba;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountRepo;
import ru.abondin.hreasy.platform.service.ba.dto.BusinessAccountDto;
import ru.abondin.hreasy.platform.service.ba.dto.BusinessAccountMapper;

@Service
@RequiredArgsConstructor
public class BusinessAccountService {

    private final BusinessAccountRepo baRepo;
    private final BusinessAccountMapper mapper;

    /**
     * List of all business accounts.
     *
     * @return
     */
    public Flux<BusinessAccountDto> findAll(boolean includeArchived) {
        // Public information. No security required.
        return (includeArchived ? baRepo.findDetailed() : baRepo.findActive()).map(mapper::fromEntry);
    }
}
