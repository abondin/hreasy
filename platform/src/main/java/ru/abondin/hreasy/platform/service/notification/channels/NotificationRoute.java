package ru.abondin.hreasy.platform.service.notification.channels;

import lombok.Data;

import java.util.List;

@Data
public
class NotificationRoute {
    private final Integer sourceEmployeeId;
    private final List<Integer> destinationEmployeeIds;

    public static NotificationRoute fromSystem(List<Integer> destinationEmployeeIds) {
        return new NotificationRoute(null, destinationEmployeeIds);
    }

    public static NotificationRoute fromEmployee(int employeeId, List<Integer> destinationEmployeeIds) {
        return new NotificationRoute(employeeId, destinationEmployeeIds);
    }

    private NotificationRoute(Integer sourceEmployeeId, List<Integer> destinationEmployeeIds) {
        this.sourceEmployeeId = sourceEmployeeId;
        this.destinationEmployeeIds = destinationEmployeeIds;
    }

    @Override
    public String toString() {
        return "{from: " + (sourceEmployeeId == null ? "system" : "employee:" + sourceEmployeeId) + ", to:" + destinationEmployeeIds + "}";
    }
}
