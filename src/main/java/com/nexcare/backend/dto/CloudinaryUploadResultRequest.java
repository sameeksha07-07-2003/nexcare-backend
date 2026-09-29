package com.nexcare.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudinaryUploadResultRequest {

    @NotBlank(message = "Secure image URL is required")
    @Size(
            max = 1000,
            message = "Secure image URL must not exceed 1000 characters"
    )
    @JsonAlias("secure_url")
    private String secureUrl;

    @NotBlank(message = "Cloudinary public ID is required")
    @Size(
            max = 255,
            message = "Cloudinary public ID must not exceed 255 characters"
    )
    @JsonAlias("public_id")
    private String publicId;

    @NotNull(message = "Cloudinary image version is required")
    @Positive(message = "Cloudinary image version must be positive")
    private Long version;

    @NotBlank(message = "Cloudinary response signature is required")
    @Size(
            max = 255,
            message = "Cloudinary response signature must not exceed 255 characters"
    )
    private String signature;
}