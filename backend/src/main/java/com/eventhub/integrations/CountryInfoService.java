package com.eventhub.integrations;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.eventhub.integrations.dto.CountryInfoResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class CountryInfoService {

    private final RestTemplate restTemplate;
    private final String restCountriesBaseUrl;

    public CountryInfoService(
            RestTemplate restTemplate,
            @Value("${app.integrations.rest-countries-base-url}") String restCountriesBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.restCountriesBaseUrl = restCountriesBaseUrl;
    }

    public CountryInfoResponse getCountryInfo(String country) {
        URI uri = UriComponentsBuilder.fromUriString(restCountriesBaseUrl + "/name/{country}")
                .queryParam("fullText", "true")
                .build(country);

        JsonNode response = restTemplate.getForObject(uri, JsonNode.class);
        if (response == null || !response.isArray() || response.isEmpty()) {
            throw new IllegalArgumentException("Country information is unavailable");
        }

        JsonNode countryNode = response.get(0);
        return new CountryInfoResponse(
                countryNode.path("name").path("common").asText(),
                countryNode.path("name").path("official").asText(),
                countryNode.path("region").asText(),
                countryNode.path("subregion").asText(),
                toTextList(countryNode.path("capital")),
                objectFieldNames(countryNode.path("currencies")),
                objectValues(countryNode.path("languages")),
                countryNode.path("flag").asText()
        );
    }

    private List<String> toTextList(JsonNode node) {
        List<String> values = new ArrayList<>();
        if (node.isArray()) {
            node.forEach(value -> values.add(value.asText()));
        }
        return values;
    }

    private List<String> objectFieldNames(JsonNode node) {
        List<String> values = new ArrayList<>();
        Iterator<String> names = node.fieldNames();
        while (names.hasNext()) {
            values.add(names.next());
        }
        return values;
    }

    private List<String> objectValues(JsonNode node) {
        List<String> values = new ArrayList<>();
        node.fields().forEachRemaining(entry -> values.add(entry.getValue().asText()));
        return values;
    }
}
