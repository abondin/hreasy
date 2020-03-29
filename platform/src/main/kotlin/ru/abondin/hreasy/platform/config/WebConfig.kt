package ru.abondin.hreasy.platform.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.web.reactive.config.WebFluxConfigurer


@Configuration
class WebConfig {
    @Bean
    fun messageSource(): MessageSource? {
        val messageSource = ReloadableResourceBundleMessageSource()
        messageSource.setBasenames("classpath:i18n/messages")
        messageSource.setDefaultEncoding("UTF-8")
        return messageSource
    }

    @Bean
    fun javatimeModule(): JavaTimeModule? {
        return JavaTimeModule()
    }

    @Bean
    fun jackson2ObjectMapperBuilderCustomizer(): Jackson2ObjectMapperBuilderCustomizer? {
        return Jackson2ObjectMapperBuilderCustomizer { jacksonObjectMapperBuilder: Jackson2ObjectMapperBuilder ->
            jacksonObjectMapperBuilder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }
    }


    @Bean
    fun jackson2JsonEncoder(mapper: ObjectMapper?): Jackson2JsonEncoder? {
        return Jackson2JsonEncoder(mapper!!)
    }

    @Bean
    fun jackson2JsonDecoder(mapper: ObjectMapper?): Jackson2JsonDecoder? {
        return Jackson2JsonDecoder(mapper!!)
    }

    @Bean
    fun webFluxConfigurer(encoder: Jackson2JsonEncoder, decoder: Jackson2JsonDecoder): WebFluxConfigurer? {
        return object : WebFluxConfigurer {
            override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
                configurer.defaultCodecs().jackson2JsonEncoder(encoder)
                configurer.defaultCodecs().jackson2JsonDecoder(decoder)
            }
        }
    }
}