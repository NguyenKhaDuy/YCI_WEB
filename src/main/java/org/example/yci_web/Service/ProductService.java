package org.example.yci_web.Service;

import org.example.yci_web.Model.DTO.ProductDTO;
import org.example.yci_web.Model.Request.ProductRequest;
import org.example.yci_web.Model.Request.RentalPriceProductRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    Page<ProductDTO> getAllProducts(Integer page);
    DataResponse getAllProducts();
    Object getAllProductsByCategory(Long idCategory);
    Object getProductById(Long idProduct);
    MessageResponse addProduct(ProductRequest productRequest);
    MessageResponse updateProduct(ProductRequest productRequest);
    MessageResponse deleteProduct(Long idProduct);
    MessageResponse deleteProductImage(Long idProduct, Long idImage);
    MessageResponse addRentalPriceProduct(RentalPriceProductRequest rentalPriceProductRequest);
    MessageResponse updateRentalPriceProduct(RentalPriceProductRequest rentalPriceProductRequest);
    MessageResponse deleteRentalPriceProduct(Long idRentalPrice);
}
