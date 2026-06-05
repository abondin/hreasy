package ru.abondin.hreasy.notifyms.service;

public enum DeliveryStatus {
    queued,
    deferred,
    sending,
    sent,
    retry_scheduled,
    failed_permanent,
    retry_exhausted
}
