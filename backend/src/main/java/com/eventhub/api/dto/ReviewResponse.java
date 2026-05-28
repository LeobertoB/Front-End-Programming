package com.eventhub.api.dto;

import java.time.Instant;

public record ReviewResponse(
        Long id,
        Long eventId,
        String eventTitle,
        Long userId,
        String userName,
        Integer rating,
        String comment,
        Instant createdAt,
        String officialReply,
        Instant repliedAt,
        Long repliedById,
        String repliedByName,
        String userFollowUp,
        Instant userFollowedUpAt
) {
}
