package com.example.Cambo_MarketPlace.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.Cambo_MarketPlace.Models.Product;
import com.example.Cambo_MarketPlace.Models.ProductImage;
import com.example.Cambo_MarketPlace.Models.User;
import com.example.Cambo_MarketPlace.Repository.ProductImageRepository;
import com.example.Cambo_MarketPlace.Repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ImageStorageService imageStorageService;

    public ProductService(ProductRepository productRepository,
                          ProductImageRepository productImageRepository,
                          ImageStorageService imageStorageService) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.imageStorageService = imageStorageService;
    }

    @Transactional
    public Product uploadProduct(
            String title,
            String description,
            Double price,
            List<MultipartFile> images,
            User seller) {

        Product product = new Product();
        product.setTitle(title);
        product.setDescription(description);
        product.setPrice(price);
        product.setStatus("AVAILABLE");
        product.setSeller(seller);

        Product savedProduct = productRepository.save(product);

        List<ProductImage> imageList = new ArrayList<>();

        for (MultipartFile file : images) {
            String imageUrl = imageStorageService.uploadImage(file);

            ProductImage image = new ProductImage();
            image.setImageUrl(imageUrl);
            image.setProduct(savedProduct);

            imageList.add(productImageRepository.save(image));
        }

        savedProduct.setImages(imageList);
        return savedProduct;
    }
}
