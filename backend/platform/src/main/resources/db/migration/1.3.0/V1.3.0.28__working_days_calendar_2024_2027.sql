-- Preserve calendars already populated manually, including production data.

INSERT INTO dict.working_days_calendar (year, region, type, calendar)
VALUES (2024, 'RU', 'default', '[
    {"day":"2024-01-01","type":1},
    {"day":"2024-01-02","type":1},
    {"day":"2024-01-03","type":1},
    {"day":"2024-01-04","type":1},
    {"day":"2024-01-05","type":1},
    {"day":"2024-01-06","type":1},
    {"day":"2024-01-07","type":1},
    {"day":"2024-01-08","type":1},
    {"day":"2024-02-23","type":1},
    {"day":"2024-03-08","type":1},
    {"day":"2024-04-27","type":2},
    {"day":"2024-04-29","type":4},
    {"day":"2024-04-30","type":4},
    {"day":"2024-05-01","type":1},
    {"day":"2024-05-09","type":1},
    {"day":"2024-05-10","type":4},
    {"day":"2024-06-12","type":1},
    {"day":"2024-11-04","type":1},
    {"day":"2024-12-28","type":2},
    {"day":"2024-12-30","type":4},
    {"day":"2024-12-31","type":4}
]'::jsonb)
ON CONFLICT DO NOTHING;

INSERT INTO dict.working_days_calendar (year, region, type, calendar)
VALUES (2025, 'RU', 'default', '[
    {"day":"2025-01-01","type":1},
    {"day":"2025-01-02","type":1},
    {"day":"2025-01-03","type":1},
    {"day":"2025-01-04","type":1},
    {"day":"2025-01-05","type":1},
    {"day":"2025-01-06","type":1},
    {"day":"2025-01-07","type":1},
    {"day":"2025-01-08","type":1},
    {"day":"2025-02-23","type":1},
    {"day":"2025-03-07","type":2},
    {"day":"2025-03-08","type":1},
    {"day":"2025-03-09","type":4},
    {"day":"2025-04-30","type":2},
    {"day":"2025-05-01","type":1},
    {"day":"2025-05-02","type":4},
    {"day":"2025-05-08","type":4},
    {"day":"2025-05-09","type":1},
    {"day":"2025-06-11","type":2},
    {"day":"2025-06-12","type":1},
    {"day":"2025-06-13","type":4},
    {"day":"2025-11-01","type":2},
    {"day":"2025-11-03","type":4},
    {"day":"2025-11-04","type":1},
    {"day":"2025-12-31","type":4}
]'::jsonb)
ON CONFLICT DO NOTHING;

INSERT INTO dict.working_days_calendar (year, region, type, calendar)
VALUES (2026, 'RU', 'default', '[
    {"day":"2026-01-01","type":1},
    {"day":"2026-01-02","type":1},
    {"day":"2026-01-03","type":1},
    {"day":"2026-01-04","type":1},
    {"day":"2026-01-05","type":1},
    {"day":"2026-01-06","type":1},
    {"day":"2026-01-07","type":1},
    {"day":"2026-01-08","type":1},
    {"day":"2026-01-09","type":4},
    {"day":"2026-02-23","type":1},
    {"day":"2026-03-08","type":1},
    {"day":"2026-03-09","type":4},
    {"day":"2026-04-30","type":2},
    {"day":"2026-05-01","type":1},
    {"day":"2026-05-08","type":2},
    {"day":"2026-05-09","type":1},
    {"day":"2026-05-11","type":4},
    {"day":"2026-06-11","type":2},
    {"day":"2026-06-12","type":1},
    {"day":"2026-11-03","type":2},
    {"day":"2026-11-04","type":1},
    {"day":"2026-12-31","type":4}
]'::jsonb)
ON CONFLICT DO NOTHING;

-- 2027 dates verified against:

-- https://www.consultant.ru/law/ref/calendar/proizvodstvennye/2027/

-- https://www.consultant.ru/document/cons_doc_LAW_544706/

INSERT INTO dict.working_days_calendar (year, region, type, calendar)
VALUES (2027, 'RU', 'default', '[
    {"day":"2027-01-01","type":1},
    {"day":"2027-01-02","type":1},
    {"day":"2027-01-03","type":1},
    {"day":"2027-01-04","type":1},
    {"day":"2027-01-05","type":1},
    {"day":"2027-01-06","type":1},
    {"day":"2027-01-07","type":1},
    {"day":"2027-01-08","type":1},
    {"day":"2027-02-20","type":2},
    {"day":"2027-02-22","type":4},
    {"day":"2027-02-23","type":1},
    {"day":"2027-03-08","type":1},
    {"day":"2027-04-30","type":2},
    {"day":"2027-05-01","type":1},
    {"day":"2027-05-03","type":4},
    {"day":"2027-05-09","type":1},
    {"day":"2027-05-10","type":4},
    {"day":"2027-06-11","type":2},
    {"day":"2027-06-12","type":1},
    {"day":"2027-06-14","type":4},
    {"day":"2027-11-03","type":2},
    {"day":"2027-11-04","type":1},
    {"day":"2027-11-05","type":4},
    {"day":"2027-12-31","type":4}
]'::jsonb)
ON CONFLICT DO NOTHING;
