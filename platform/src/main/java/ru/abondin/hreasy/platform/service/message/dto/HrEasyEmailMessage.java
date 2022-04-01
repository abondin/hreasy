package ru.abondin.hreasy.platform.service.message.dto;

import lombok.Data;
import lombok.ToString;
import org.springframework.core.io.Resource;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Message to send via email
 */
@Data
@ToString(of = {"clientUuid", "to", "title"})
public class HrEasyEmailMessage extends HrEasyMessage {

    private List<String> to = new ArrayList<>();
    private List<String> cc = new ArrayList<>();

    @NotNull
    private String from;

    private Map<String, Resource> attachments = new HashMap<>();

}
