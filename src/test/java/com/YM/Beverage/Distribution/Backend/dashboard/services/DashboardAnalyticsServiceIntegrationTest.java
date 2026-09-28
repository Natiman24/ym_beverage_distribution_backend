package com.YM.Beverage.Distribution.Backend.dashboard.services;

import com.YM.Beverage.Distribution.Backend.dashboard.dtos.DashboardAnalyticsDTO;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DashboardAnalyticsServiceIntegrationTest {
    @Autowired private DashboardAnalyticsService analyticsService;

    @Test
    void buildsThirtyDayAnalyticsFromReportingQueries() {
        ApiResponse response = analyticsService.getAnalytics(30);
        DashboardAnalyticsDTO analytics =
                (DashboardAnalyticsDTO) response.getDetails().get("analytics");

        assertNotNull(analytics);
        assertEquals(30, analytics.getPeriodDays());
        assertEquals(30, analytics.getSalesTrend().size());
        assertNotNull(analytics.getKpis());
        assertNotNull(analytics.getInventory());
    }
}
