package com.nexcare.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Uploads an image to Cloudinary under a "nexcare/patient-photos" folder
     * and returns the resulting secure (https) URL.
     */
    public String uploadPatientPhoto(MultipartFile file) {
        try {
            // Cloudinary's Java SDK expects a Transformation object here,
            // not a plain Map — passing a Map caused
            // "Invalid transformation component - {width=512".
            Transformation transformation = new Transformation()
                    .width(512)
                    .height(512)
                    .crop("limit")
                    .quality("auto");

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "nexcare/patient-photos",
                            "resource_type", "image",
                            "transformation", transformation
                    )
            );
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload photo to Cloudinary", e);
        }
    }
}