package com.example.Cambo_MarketPlace.Controller;

import com.example.Cambo_MarketPlace.Models.Product;
import com.example.Cambo_MarketPlace.Models.ProductImage;
import com.example.Cambo_MarketPlace.Repository.ProductRepository;
import com.example.Cambo_MarketPlace.Repository.ProductImageRepository;
import com.example.Cambo_MarketPlace.DTO.ProductRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    private final String UPLOAD_DIR = "uploads/"; // local folder to store images

    // GET all products
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    // GET product by id
    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST new product with images
    @PostMapping
    public ResponseEntity<?> uploadProduct(@ModelAttribute ProductRequest request) {

        try {
            Product product = new Product();
            product.setTitle(request.getTitle());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());
            product.setStatus(request.getStatus());

            // TODO: set a default seller or null for testing
            product.setSeller(null);

            // Save product first
            Product savedProduct = productRepository.save(product);

            // Handle images
            List<ProductImage> imageList = new ArrayList<>();
            if (request.getImages() != null) {
                for (MultipartFile file : request.getImages()) {
                    String fileName = StringUtils.cleanPath(file.getOriginalFilename());
                    File saveFile = new File(UPLOAD_DIR + fileName);
                    saveFile.getParentFile().mkdirs(); // create directory if not exist
                    file.transferTo(saveFile);

                    ProductImage productImage = new ProductImage();
                    productImage.setImageUrl(UPLOAD_DIR + fileName);
                    productImage.setProduct(savedProduct);
                    imageList.add(productImage);
                }
                productImageRepository.saveAll(imageList);
                savedProduct.setImages(imageList);
            }

            return ResponseEntity.ok(savedProduct);

        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to upload product: " + e.getMessage());
        }
    }
}
