update dict.department set "name"='Отдел разработки программного обеспечения' where id=3;
update dict.department set "name"='Отдел тестирования программного обеспечения' where id=5;
UPDATE dict."position" SET "name"='Инженер-программист 1 кат.' WHERE id=9;
UPDATE dict."position" SET "name"='Инженер-программист 2 кат.' WHERE id=10;
UPDATE dict."position" SET "name"='Инженер-программист 3 кат.' WHERE id=11;
UPDATE dict."position" SET "name"='Инженер по тестированию 3 кат.' WHERE id=12;
UPDATE dict."position" SET "name"='Инженер по тестированию 2 кат.' WHERE id=13;
UPDATE dict."position" SET "name"='Инженер по тестированию 1 категории' WHERE id=32;
UPDATE dict."position" SET "name"='Руководитель отдела тестирования ПО' WHERE id=24;
UPDATE dict."position" SET "name"='Менеджер проектов' WHERE id=18;
UPDATE dict."position" SET "name"='Младший консультант по внедрению и интеграции программного обеспечения' WHERE id=40;
UPDATE dict."position" SET "name"='Специалист по административно-хозяйственной деятельности' WHERE id=35;
UPDATE dict."position" SET "name"='Руководитель отдела внедрения и интеграции ПО' WHERE id=22;

update empl.employee set display_name=trim(regexp_replace(display_name, '\s+', ' ', 'g'));