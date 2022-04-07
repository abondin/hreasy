package ru.abondin.hreasy.apifacade;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "hreasy.api.discovery")
@Data
public class ApiDiscoveryProps {

    private String corems = "http://hreasy-core-ms";
}
