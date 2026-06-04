package ru.abondin.hreasy.platform.service.udr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.service.udr.dto.JuniorExportDto;

import java.io.IOException;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

@Component
@Slf4j
@RequiredArgsConstructor
@AllArgsConstructor
public class AdminJuniorRegistryExcelExporter {


    @Value("${classpath:jxls/admin_junior_registry_template.xlsx}")
    private Resource template;

    @Autowired
    private I18Helper i18Helper;

    @Data
    @Builder
    public static class AdminJuniorRegistryExportBundle {
        private String exportedBy;
        private OffsetDateTime exportedAt;
        private List<JuniorExportDto> items;
        private Locale locale;
    }


    public void exportJuniors(AdminJuniorRegistryExportBundle bundle, OutputStream out) throws IOException {
        try (var is = template.getInputStream()) {
            var context = new Context();
            context.putVar("items", i18n(bundle.locale, bundle.items));
            context.putVar("exportedAt", bundle.getExportedAt().toLocalDate());
            context.putVar("exportedBy", bundle.getExportedBy());
            JxlsHelper.getInstance().processTemplate(is, out, context);
        }
    }

    private List<JuniorExportDto> i18n(Locale locale, List<JuniorExportDto> items) {
        return items;
    }
}
