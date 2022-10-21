CREATE TABLE IF NOT EXISTS dict.working_days_calendar (
    year integer NOT NULL,
    region varchar(255) NOT NULL,
    type varchar(255) NOT NULL DEFAULT 'default',
    calendar JSONB not null,
    PRIMARY KEY (year,region,type)
);


COMMENT ON TABLE dict.working_days_calendar IS 'Business days, weekend days and holidays';
COMMENT ON COLUMN dict.working_days_calendar.year IS 'Year';
COMMENT ON COLUMN dict.working_days_calendar.region IS 'Region (country) [RU]';
COMMENT ON COLUMN dict.working_days_calendar.type IS 'If you have several working calendars';
COMMENT ON COLUMN dict.working_days_calendar.calendar IS '
[{
    day: "2022-10-06", // YYYY-MM-DD format
    type: 1, // 1 - holiday, 2 - working day (can be weekend), 4 - not working day for moved holiday (t=1 + f field in http://xmlcalendar.ru/)
    // if no day found:
    //   monday-friday - working days
    //   saturday,sunday - weekend days
}]
';

COMMENT ON COLUMN history.history.entity_type IS '[empl_manager] - Entity type, [working_days] - Working Days Calendar';

-- Default Russian 2022 calendar. Got from http://xmlcalendar.ru/
insert into dict.working_days_calendar(year,region,type,calendar) values (2022,'RU','default',
'
[
    {"day":"2022-01-01", "type":1},
    {"day":"2022-01-02", "type":1},
    {"day":"2022-01-03", "type":1},
    {"day":"2022-01-04", "type":1},
    {"day":"2022-01-05", "type":1},
    {"day":"2022-01-06", "type":1},
    {"day":"2022-01-07", "type":1},
    {"day":"2022-01-08", "type":1},
    {"day":"2022-02-22", "type":2},
    {"day":"2022-02-23", "type":1},
    {"day":"2022-03-05", "type":2},
    {"day":"2022-03-07", "type":4},
    {"day":"2022-03-08", "type":1},
    {"day":"2022-05-01", "type":1},
    {"day":"2022-05-02", "type":4},
    {"day":"2022-05-03", "type":4},
    {"day":"2022-05-09", "type":1},
    {"day":"2022-05-10", "type":4},
    {"day":"2022-06-12", "type":1},
    {"day":"2022-06-13", "type":4},
    {"day":"2022-11-03", "type":2},
    {"day":"2022-11-04", "type":1}
]') on conflict do nothing;

-- Default Russian 2023 calendar. Got from http://xmlcalendar.ru/
insert into dict.working_days_calendar(year,region,type,calendar) values (2023,'RU','default','
[
    {"day":"2023-01-01", "type":1},
    {"day":"2023-01-02", "type":1},
    {"day":"2023-01-03", "type":1},
    {"day":"2023-01-04", "type":1},
    {"day":"2023-01-05", "type":1},
    {"day":"2023-01-06", "type":1},
    {"day":"2023-01-07", "type":1},
    {"day":"2023-01-08", "type":1},
    {"day":"2023-02-22", "type":2},
    {"day":"2023-02-23", "type":1},
    {"day":"2023-02-24", "type":1},
    {"day":"2023-03-07", "type":2},
    {"day":"2023-03-08", "type":1},
    {"day":"2023-05-01", "type":1},
    {"day":"2023-05-08", "type":1},
    {"day":"2023-05-09", "type":1},
    {"day":"2023-06-12", "type":1},
    {"day":"2023-11-03", "type":2},
    {"day":"2023-11-04", "type":1},
    {"day":"2023-11-06", "type":1}
]') on conflict do nothing;