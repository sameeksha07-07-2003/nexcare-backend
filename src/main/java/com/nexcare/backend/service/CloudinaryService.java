package com.nexcare.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
public class CloudinaryService {

    private static final String PATIENT_PHOTO_FOLDER =
            "nexcare/patient-photos";

    private static final String DOCTOR_PHOTO_FOLDER =
            "nexcare/doctor-photos";

    private static final String PROFILE_PUBLIC_ID = "profile";

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Generates a signed upload request for one authenticated patient.
     *
     * Example Cloudinary asset:
     * nexcare/patient-photos/12/profile
     */
    public Map<String, Object> generatePatientPhotoUploadSignature(
            Long patientId
    ) {
        validateOwnerId(patientId);

        String folder =
                PATIENT_PHOTO_FOLDER + "/" + patientId;

        return generateProfilePhotoUploadSignature(folder);
    }

    /**
     * Generates a signed upload request for one authenticated doctor.
     *
     * Example Cloudinary asset:
     * nexcare/doctor-photos/7/profile
     */
    public Map<String, Object> generateDoctorPhotoUploadSignature(
            Long doctorId
    ) {
        validateOwnerId(doctorId);

        String folder =
                DOCTOR_PHOTO_FOLDER + "/" + doctorId;

        return generateProfilePhotoUploadSignature(folder);
    }

    /**
     * Verifies the result returned by Cloudinary after the browser
     * completes a direct signed upload.
     *
     * The backend must verify this result before storing the URL.
     */
    public void verifyProfilePhotoUpload(
            String secureUrl,
            String publicId,
            Long version,
            String responseSignature,
            String expectedPublicId
    ) {
        if (secureUrl == null || secureUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Cloudinary secure URL is required."
            );
        }

        if (publicId == null || publicId.isBlank()) {
            throw new IllegalArgumentException(
                    "Cloudinary public ID is required."
            );
        }

        if (version == null || version <= 0) {
            throw new IllegalArgumentException(
                    "Cloudinary image version is invalid."
            );
        }

        if (responseSignature == null || responseSignature.isBlank()) {
            throw new IllegalArgumentException(
                    "Cloudinary response signature is required."
            );
        }

        if (!publicId.equals(expectedPublicId)) {
            throw new IllegalArgumentException(
                    "The uploaded image does not belong to this profile."
            );
        }

        boolean validSignature =
                cloudinary.verifyApiResponseSignature(
                        publicId,
                        String.valueOf(version),
                        responseSignature
                );

        if (!validSignature) {
            throw new IllegalArgumentException(
                    "Cloudinary upload verification failed."
            );
        }

        validateCloudinarySecureUrl(secureUrl);
    }

    /**
     * Deletes a profile image from Cloudinary.
     *
     * A missing image is treated as already deleted.
     */
    public void deleteImage(String publicId) {

        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            Map<?, ?> result = cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "invalidate", true
                    )
            );

            Object deletionResult = result.get("result");

            if (
                    !"ok".equals(deletionResult) &&
                            !"not found".equals(deletionResult)
            ) {
                throw new IllegalStateException(
                        "Cloudinary image could not be deleted."
                );
            }

        } catch (IllegalStateException exception) {
            throw exception;

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Cloudinary image deletion failed.",
                    exception
            );
        }
    }

    public String buildPatientProfilePublicId(Long patientId) {
        validateOwnerId(patientId);

        return PATIENT_PHOTO_FOLDER
                + "/"
                + patientId
                + "/"
                + PROFILE_PUBLIC_ID;
    }

    public String buildDoctorProfilePublicId(Long doctorId) {
        validateOwnerId(doctorId);

        return DOCTOR_PHOTO_FOLDER
                + "/"
                + doctorId
                + "/"
                + PROFILE_PUBLIC_ID;
    }

    private Map<String, Object> generateProfilePhotoUploadSignature(
            String folder
    ) {
        long timestamp = System.currentTimeMillis() / 1000L;

        Map<String, Object> parametersToSign = new TreeMap<>();

        parametersToSign.put("timestamp", timestamp);
        parametersToSign.put("folder", folder);
        parametersToSign.put("public_id", PROFILE_PUBLIC_ID);
        parametersToSign.put("overwrite", true);
        parametersToSign.put("invalidate", true);

        String signature = cloudinary.apiSignRequest(
                parametersToSign,
                cloudinary.config.apiSecret,
                cloudinary.config.signatureVersion
        );

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("signature", signature);
        response.put("timestamp", timestamp);
        response.put("apiKey", cloudinary.config.apiKey);
        response.put("cloudName", cloudinary.config.cloudName);

        response.put("folder", folder);
        response.put("publicId", PROFILE_PUBLIC_ID);
        response.put("overwrite", true);
        response.put("invalidate", true);

        response.put("expectedPublicId",
                folder + "/" + PROFILE_PUBLIC_ID);

        response.put("uploadUrl", buildUploadUrl());

        return response;
    }

    private String buildUploadUrl() {
        return "https://api.cloudinary.com/v1_1/"
                + cloudinary.config.cloudName
                + "/image/upload";
    }

    private void validateCloudinarySecureUrl(String secureUrl) {

        URI uri;

        try {
            uri = URI.create(secureUrl);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Cloudinary secure URL is invalid."
            );
        }

        boolean validScheme =
                "https".equalsIgnoreCase(uri.getScheme());

        boolean validHost =
                "res.cloudinary.com".equalsIgnoreCase(uri.getHost());

        String expectedPathPrefix =
                "/"
                        + cloudinary.config.cloudName
                        + "/image/upload/";

        boolean validPath =
                uri.getPath() != null &&
                        uri.getPath().startsWith(expectedPathPrefix);

        if (!validScheme || !validHost || !validPath) {
            throw new IllegalArgumentException(
                    "The image URL is not a valid NexCare Cloudinary URL."
            );
        }
    }

    private void validateOwnerId(Long ownerId) {
        if (ownerId == null || ownerId <= 0) {
            throw new IllegalArgumentException(
                    "Profile owner ID is invalid."
            );
        }
    }

    public String buildPatientPhotoPublicId(
            Long patientId
    ) {
        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException(
                    "A valid patient ID is required."
            );
        }

        return PATIENT_PHOTO_FOLDER
                + "/"
                + patientId
                + "/profile";
    }
    public String buildDoctorPhotoPublicId(
            Long doctorId
    ) {
        if (doctorId == null || doctorId <= 0) {
            throw new IllegalArgumentException(
                    "A valid doctor ID is required."
            );
        }

        return DOCTOR_PHOTO_FOLDER
                + "/"
                + doctorId
                + "/profile";
    }
}
