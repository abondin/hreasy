package ru.abondin.hreasy.platform.service.message.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.core.io.Resource;

import org.springframework.lang.NonNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Message to send via email
 */
@Data
@NoArgsConstructor
@ToString(of = {"clientUuid", "to", "cc", "title"})
public class HrEasyEmailMessage extends HrEasyMessage {

    private List<String> to = new ArrayList<>();
    private List<String> cc = new ArrayList<>();

    @NonNull
    private String from;

    private Map<String, Resource> attachments = new HashMap<>();

}
