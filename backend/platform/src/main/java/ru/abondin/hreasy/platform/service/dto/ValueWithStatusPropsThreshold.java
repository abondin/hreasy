package ru.abondin.hreasy.platform.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;

/**
 * Thresholds for {@link ValueWithStatus}
 *
 * @param <T>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValueWithStatusPropsThreshold<T> {
    private T waring;
    private T error;
    private Comparator<T> comparator;

    public static <T extends Comparable> ValueWithStatusPropsThreshold<T> numberThresholds(T waring, T error) {
        return new ValueWithStatusPropsThreshold<T>(waring, error, Comparator.naturalOrder());
    }
}
