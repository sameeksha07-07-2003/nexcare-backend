package com.nexcare.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardSummaryResponse {

    private long totalDoctors;
    private long pendingDoctors;
    private long approvedDoctors;
    private long rejectedDoctors;
}
