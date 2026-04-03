package com.finSecure.service;

import com.finSecure.dto.response.DashboardSummaryResponse;
import org.springframework.security.core.Authentication;

public interface DashboardService {

    DashboardSummaryResponse getSummary(Authentication auth);

}
