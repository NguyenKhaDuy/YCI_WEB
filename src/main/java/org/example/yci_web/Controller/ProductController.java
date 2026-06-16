package org.example.yci_web.Controller;

import org.example.yci_web.Model.DTO.ProductDTO;
import org.example.yci_web.Model.Request.ProductRequest;
import org.example.yci_web.Model.Request.RentalPriceProductRequest;
import org.example.yci_web.Model.Response.DataPageResponse;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;
import org.example.yci_web.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductController {
    @Autowired
    ProductService productService;

    @GetMapping(value = "/api/product")
    public ResponseEntity<Object> getProduct(@RequestParam(name = "page", defaultValue = "1") Integer page) {
        DataPageResponse dataPageResponse = new DataPageResponse();
        Page<ProductDTO> productDTOS = productService.getAllProducts(page);
        dataPageResponse.setData(productDTOS.getContent());
        dataPageResponse.setMessage("Success");
        dataPageResponse.setStatus(HttpStatus.OK);
        dataPageResponse.setCurrentPage(page);
        dataPageResponse.setTotalPage(productDTOS.getTotalPages());
        return new ResponseEntity<>(dataPageResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/api/product/all")
    public ResponseEntity<Object> getProduct() {
        DataResponse dataResponse = productService.getAllProducts();
        return new ResponseEntity<>(dataResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/api/product/id-product={idProduct}")
    public ResponseEntity<Object> getProductById(@PathVariable("idProduct") Long idProduct) {
        Object result = productService.getProductById(idProduct);
        if (result instanceof MessageResponse){
            return new ResponseEntity<>(result, ((MessageResponse) result).getStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = "/api/product/id-category={idCategory}")
    public ResponseEntity<Object> getProductByIdCategory(@PathVariable("idCategory") Long idCategory) {
        Object result = productService.getAllProductsByCategory(idCategory);
        if (result instanceof MessageResponse){
            return new ResponseEntity<>(result, ((MessageResponse) result).getStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/api/admin/product")
    public ResponseEntity<Object> addProduct(@ModelAttribute ProductRequest productRequest) {
        MessageResponse messageResponse = productService.addProduct(productRequest);
        return new ResponseEntity<>(messageResponse, HttpStatus.OK);
    }

    @PutMapping(value = "/api/admin/product")
    public ResponseEntity<Object> updateProduct(@ModelAttribute ProductRequest productRequest) {
        MessageResponse messageResponse = productService.updateProduct(productRequest);
        return new ResponseEntity<>(messageResponse, messageResponse.getStatus());
    }

    @DeleteMapping(value = "/api/admin/product/id-product={idProduct}")
    public ResponseEntity<Object> deleteProduct(@PathVariable("idProduct") Long idProduct) {
        MessageResponse messageResponse = productService.deleteProduct(idProduct);
        return new ResponseEntity<>(messageResponse, messageResponse.getStatus());
    }

    @PostMapping(value = "/api/admin/product/rental-price")
    public ResponseEntity<Object> addRentalPrice(@RequestBody RentalPriceProductRequest rentalPriceProductRequest) {
        MessageResponse messageResponse = productService.addRentalPriceProduct(rentalPriceProductRequest);
        return new ResponseEntity<>(messageResponse, messageResponse.getStatus());
    }
}
