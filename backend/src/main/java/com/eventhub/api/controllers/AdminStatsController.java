package com.eventhub.api.controllers;

import com.eventhub.api.dto.AdminStatsResponse;
import com.eventhub.api.services.AdminStatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    public AdminStatsController(AdminStatsService adminStatsService) {
        this.adminStatsService = adminStatsService;
    }

    @GetMapping("/api/admin/stats")
    public AdminStatsResponse getStats() {
        return adminStatsService.getStats();
    }
}
