package ru.abondin.hreasy.platform.service.ba;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountRepo;
import ru.abondin.hreasy.platform.service.ba.dto.BusinessAccountDto;
import ru.abondin.hreasy.platform.service.ba.dto.BusinessAccountMapper;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

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

    public Mono<BusinessAccountDto> get(int baId) {
        return baRepo.findDetailedById(baId)
                .map(mapper::fromEntry)
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(baId))));
    }

    public Flux<SimpleDictDto> findAllAsSimpleDict(boolean includeArchived) {
        return findAll(includeArchived).map(ba -> new SimpleDictDto(ba.getId(), ba.getName(), !ba.isArchived()));
    }
}
