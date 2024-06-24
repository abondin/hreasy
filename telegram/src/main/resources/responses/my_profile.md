Добро пожаловать [[${e.displayName}]]

• Ваш проект: [[${e.projectName}]]
• Ваши менеджеры: [[${e.projectManagers}]]

Предстоящие отпуска: 
[# th:each="v : ${e.upcomingVacations}"]
• [(${v?.startDate})] - [(${v?.endDate})]
[/]
