package com.nexcare.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DoctorAppointmentSummaryDto {

    private long today;
    private long upcoming;
    private long needsAction;
    private long completed;
    private long cancelled;
    private long noShow;
}