#!/bin/bash

# Получаем текущий год и следующий год
current_year=$(date +%Y)
next_year=$((current_year + 1))

# Используем переданный год или следующий по умолчанию
year=${1:-$next_year}

# Ссылка для загрузки XML-файла
url="https://xmlcalendar.ru/data/ru/${year}/calendar.xml"

# Временный файл для скачивания XML
input_xml="calendar_${year}.xml"

# Скачиваем XML-файл
curl -k -o $input_xml $url

# SQL файл для вывода
output_sql="insert_days_${year}.sql"

# Начало SQL запроса
echo "insert into dict.working_days_calendar(year,region,type,calendar) values (${year},'RU','default', '[" > $output_sql

# Парсинг XML файла и обработка строк
cat $input_xml | grep '<day ' | while read -r line; do
  # Получаем день, тип, флаг (если есть)
  day=$(echo $line | sed -n 's/.* d="\([^"]*\)".*/\1/p')
  type=$(echo $line | sed -n 's/.* t="\([^"]*\)".*/\1/p')
  f_flag=$(echo $line | grep 'f=')

  # Форматируем дату
  formatted_day="${year}-${day//./-}"

  # Если есть атрибут f, меняем тип на 4
  if [[ -n $f_flag ]]; then
    type="4"
  fi

  # Пишем строку в SQL файл
  echo "{\"day\":\"$formatted_day\", \"type\":$type}," >> $output_sql
done

# Закрываем SQL запрос
echo "]') on conflict do nothing;" >> $output_sql

# Убираем последнюю запятую в SQL файле (чистим JSON)
sed -i '$ s/,$//' $output_sql

# Удаляем временный XML файл
rm $input_xml

# Сообщение об успешной генерации
echo "SQL файл $output_sql успешно создан."
