package ru.abondin.hreasy.platform.excel;


import lombok.Data;

import java.util.Date;

@Data
public class ImportExcelTestDataDto {
    private String name;
    private Integer age;

    private Date birthday;
}
