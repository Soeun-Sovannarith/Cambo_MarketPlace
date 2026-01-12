package com.example.Cambo_MarketPlace.DTO;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public class ProductRequest {

    private String title;
    private String description;
    private Double price;
    private String status; // AVAILABLE / SOLD
    private List<MultipartFile> images;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<MultipartFile> getImages() { return images; }
    public void setImages(List<MultipartFile> images) { this.images = images; }
}
