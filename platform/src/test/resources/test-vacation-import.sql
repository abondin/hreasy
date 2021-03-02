insert into vacation (employee,[year],start_date,end_date,days_number,stat,documents,created_at, created_by)
select e.id, v.[Год отпуска],
	convert(datetime, v.[Начало отпуска], 4),
	convert(datetime, v.[Конец отпуска ] , 4), v.[Количество дней],
	case lower(v.Статус)
		when 'отгулял' then 1
		when 'компенсация' then 2
		else 0
	end,
	v.Документы,
	SYSDATETIMEOFFSET(),
	5
from vacation_import v
	left join employee e on lower(replace(v.[﻿ФИО], 'ё', 'е')) like
lower(
replace(
	concat(
	  e.lastname
	  ,concat(' ',substring(e.firstname,0, 2),'.')
	), 'ё', 'е'
))+'%';
