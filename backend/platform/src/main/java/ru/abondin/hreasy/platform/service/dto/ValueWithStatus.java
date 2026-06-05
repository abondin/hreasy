package ru.abondin.hreasy.platform.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Wrap value with status.
 * For example 2 months without report for junior is ok, 4 is warning, 6 is error
 *
 * @param <T>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValueWithStatus<T> {
    public static final int STATUS_OK = 1;
    public static final int STATUS_WARNING = 2;
    public static final int STATUS_ERROR = 3;
    private T value;
    private int status;

    public static <T> ValueWithStatus<T> value(T value, ValueWithStatusPropsThreshold<T> thresholds) {
        var result = Objects.compare(value, thresholds.getError(), thresholds.getComparator());
        var status = STATUS_OK;
        if (result > 0) {
            status = STATUS_ERROR;
        } else {
            result = Objects.compare(value, thresholds.getWaring(), thresholds.getComparator());
            if (result > 0) {
                status = STATUS_WARNING;
            }
        }
        return new ValueWithStatus<>(value, status);
    }

    public static <T> ValueWithStatus<T> ok(T value){
        return new ValueWithStatus<>(value, STATUS_OK);
    }
}
