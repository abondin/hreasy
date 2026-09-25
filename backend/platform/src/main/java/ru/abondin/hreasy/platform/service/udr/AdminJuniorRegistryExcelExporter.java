package ru.abondin.hreasy.platform.service.udr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jxls.transform.poi.JxlsPoiTemplateFillerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.service.udr.dto.JuniorExportDto;

import java.io.IOException;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.HashMap;
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
            var context = new HashMap<String, Object>();
            context.put("items", i18n(bundle.locale, bundle.items));
            context.put("exportedAt", i18Helper.formatDateTime(bundle.getLocale(), bundle.getExportedAt()));
            context.put("exportedBy", bundle.getExportedBy());
            JxlsPoiTemplateFillerBuilder.newInstance().withTemplate(is).buildAndFill(context, () -> out);
        }
    }

    private List<JuniorExportDto> i18n(Locale locale, List<JuniorExportDto> items) {
        return items;
    }
}
