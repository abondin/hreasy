package ru.abondin.hreasy.telegram.common;

import org.telegram.abilitybots.api.objects.Ability;
import ru.abondin.hreasy.telegram.HrEasyBot;

public interface HrEasyAbilityFactory {
    String name();

    Ability create(HrEasyBot bot);
}
