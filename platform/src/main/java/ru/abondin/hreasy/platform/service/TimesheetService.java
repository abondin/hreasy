package ru.abondin.hreasy.platform.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.repo.ts.TimesheetRecordRepo;
import ru.abondin.hreasy.platform.service.ts.dto.TimesheetRecordDto;

@Service
@Slf4j
@RequiredArgsConstructor
public class TimesheetService {
    private final DateTimeService dateTimeService;
    private final TimesheetRecordRepo repo;


    public Flux<TimesheetRecordDto> 
}
