package ru.abondin.hreasy.platform.service.admin.imp;

import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.service.admin.imp.dto.ExcelImportRowDto;

/**
 * Commit changes from imported file to the database
 *
 * @param <R>
 */
public interface ExcelImportCommitter<R extends ExcelImportRowDto> {
    @Transactional()
    Mono<Integer> commitRow(AuthContext ctx, R row, Integer processId);
}
