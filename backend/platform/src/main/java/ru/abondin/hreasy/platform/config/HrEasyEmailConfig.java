package ru.abondin.hreasy.platform.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

@org.springframework.context.annotation.Configuration
public class HrEasyEmailConfig {

    @Bean
    @Description("Thymeleaf Template Resolver")
    public SpringResourceTemplateResolver templateResolver() {
        var templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setPrefix("classpath:/mail/template/");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        return templateResolver;
    }

    @Bean
    @Description("Thymeleaf Template Engine")
    public TemplateEngine templateEngine(MessageSource messageSource) {
        var templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        //templateEngine.addDialect(new Java8TimeDialect());
        templateEngine.setTemplateEngineMessageSource(messageSource);
        return templateEngine;
    }
}
