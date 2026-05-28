package com.eventhub.integrations.dto;

import java.util.List;

public record CountryInfoResponse(
        String commonName,
        String officialName,
        String region,
        String subregion,
        List<String> capital,
        List<String> currencies,
        List<String> languages,
        String flag
) {
}
