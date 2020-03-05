package ru.abondin.hreasy.platform.config

import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource


@Configuration
class WebConfig {
    @Bean
    fun messageSource(): MessageSource? {
        val messageSource = ReloadableResourceBundleMessageSource()
        messageSource.setBasenames("classpath:i18n/messages")
        messageSource.setDefaultEncoding("UTF-8")
        return messageSource
    }

}