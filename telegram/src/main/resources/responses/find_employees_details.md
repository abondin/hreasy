Информация по сотруднику [[${e.displayName}]]

• Почта: [[${e.email}]]
• Telegram: [[${e.telegram != null ? e.telegram : '-'}]] [# th:if="${!e.telegramConfirmed}"]⚠️не подтверждён[/]
• Проект: [[${e.projectName != null ? e.projectName : '-'}]]
[# th:if="${e.projectRole != null and !e.projectRole.empty}"]• Роль на проекте: [[${e.projectRole}]][/]
• Рабочее место: [[${e.officeLocation != null ? e.officeLocation : '-'}]]
• Предстоящие отпуска:[# th:each="v : ${e.upcomingVacations}"]
  • [(${v?.startDate})] - [(${v?.endDate})]
[/]