Информация по сотруднику [[${e.displayName}]]

• Почта: [[${e.email}]]
• Telegram: [[${e.telegram}]]
• Проект: [[${e.projectName}]]
• Рабочее место: [[${e.officeLocation}]]

Предстоящие отпуска: 
[# th:each="v : ${e.upcomingVacations}"]
• [(${v?.startDate})] - [(${v?.endDate})]
[/]
