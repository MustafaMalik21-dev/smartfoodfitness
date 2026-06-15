package com.mustafa.smartfoodfitness.dto;

public class AnalyzeFoodRequest {
    private String imageBase64;
    private String mediaType;

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
}
