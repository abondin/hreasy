update dict.department set "name"='����� ���������� ������������ �����������' where id=3;
update dict.department set "name"='����� ������������ ������������ �����������' where id=5;
UPDATE dict."position" SET "name"='�������-����������� 1 ���.' WHERE id=9;
UPDATE dict."position" SET "name"='�������-����������� 2 ���.' WHERE id=10;
UPDATE dict."position" SET "name"='�������-����������� 3 ���.' WHERE id=11;
UPDATE dict."position" SET "name"='������� �� ������������ 3 ���.' WHERE id=12;
UPDATE dict."position" SET "name"='������� �� ������������ 2 ���.' WHERE id=13;
UPDATE dict."position" SET "name"='������� �� ������������ 1 ���������' WHERE id=32;
UPDATE dict."position" SET "name"='������������ ������ ������������ ��' WHERE id=24;
UPDATE dict."position" SET "name"='�������� ��������' WHERE id=18;
UPDATE dict."position" SET "name"='������� ����������� �� ��������� � ���������� ������������ �����������' WHERE id=40;
UPDATE dict."position" SET "name"='���������� �� ���������������-������������� ������������' WHERE id=35;
UPDATE dict."position" SET "name"='������������ ������ ��������� � ���������� ��' WHERE id=22;

update empl.employee set display_name=trim(regexp_replace(display_name, '\s+', ' ', 'g'));