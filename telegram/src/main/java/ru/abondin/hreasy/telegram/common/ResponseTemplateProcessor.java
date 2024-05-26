package ru.abondin.hreasy.telegram.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.TemplateSpec;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.function.Consumer;

/**
 * Using thymeleaf fot template processing
 * Message templates are stored in classpath
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResponseTemplateProcessor {
    private final SpringTemplateEngine templateEngine;


    public String process(String templateName, Consumer<Context> contextPopulator) {
        log.debug("Process template {}", templateName);
        var context = defaultContext();
        contextPopulator.accept(context);
        var templateSpec = new TemplateSpec(templateName, TemplateMode.TEXT);
        return templateEngine.process(templateSpec, context);
    }

    private Context defaultContext() {
        var context = new Context();
        // Add some default global variables from props
        return context;
    }
}
