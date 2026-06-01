package com.financialapp.investments.domain.model.holding;

import java.time.LocalDateTime;

public record NotificationTimestamps(LocalDateTime lastGainNotifiedAt, LocalDateTime lastLossNotifiedAt) {

    public static NotificationTimestamps empty() {
        return new NotificationTimestamps(null, null);
    }

    public NotificationTimestamps withGainNotifiedAt(LocalDateTime timestamp) {
        return new NotificationTimestamps(timestamp, this.lastLossNotifiedAt);
    }

    public NotificationTimestamps withLossNotifiedAt(LocalDateTime timestamp) {
        return new NotificationTimestamps(this.lastGainNotifiedAt, timestamp);
    }
}
