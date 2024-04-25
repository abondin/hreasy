package ru.abondin.hreasy.platform.service.admin.imp;


import lombok.Data;

import java.util.stream.Stream;

/**
 * Collection of all imported attributes for one excel row
 * Each attribute contains current value from database,
 * display name from database,
 * raw imported value,
 * processed imported value (find dict key by dict value)
 */
@Data
public abstract class ExcelImportRowDto {

    /**
     * Row number in document
     */
    private Integer rowNumber;


    public int getErrorCount() {
        return (int) allProperties().filter(p -> p.getError() != null).count();
    }

    public int getUpdatedCellsCount() {
        return (int) allProperties().filter(p -> p.isUpdated()).count();
    }

    public abstract boolean isNew();

    protected abstract Stream<DataProperty<?>> allProperties();

    @Data
    public static class DataProperty<T> {
        private T currentValue;
        private T importedValue;
        private String raw;
        private String error;

        /**
         * @return false if some errors or current value and imported value the same or imported value is null
         */
        public boolean isUpdated() {
            return error == null && importedValue != null && !importedValue.equals(currentValue);
        }

        @Override
        public String toString() {
            return raw == null ? "NULL" : raw;
        }
    }

}
