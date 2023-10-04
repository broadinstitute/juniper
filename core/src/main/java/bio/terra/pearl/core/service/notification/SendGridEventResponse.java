package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.dao.notification.SendgridEvent;

import java.util.List;

public record SendGridEventResponse(
        List<SendgridEvent> messages
) {
}
