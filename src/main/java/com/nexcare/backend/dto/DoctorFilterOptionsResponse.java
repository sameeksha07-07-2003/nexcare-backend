package com.nexcare.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DoctorFilterOptionsResponse {

    private List<String> cities;
    private List<String> specializations;
    private List<String> qualifications;
}