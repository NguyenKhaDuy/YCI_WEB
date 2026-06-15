package org.example.yci_web.Controller;

import org.example.yci_web.Model.Request.CategoryRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;
import org.example.yci_web.Service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CategoryController {
    @Autowired
    CategoryService categoryService;

    @GetMapping(value = "/api/category")
    public ResponseEntity<Object> getAllCategory() {
        DataResponse dataResponse = categoryService.getAllCategory();
        return new ResponseEntity<>(dataResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/category/idcate={idCategory}")
    public ResponseEntity<Object> getCategoryById(@PathVariable Long idCategory) {
        Object result = categoryService.getCategoryById(idCategory);
        if(result instanceof MessageResponse){
            return new ResponseEntity<>(result, ((MessageResponse) result).getStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/api/admin/category")
    public ResponseEntity<Object> addCategory(@RequestBody CategoryRequest categoryRequest) {
        MessageResponse messageResponse = categoryService.addCategory(categoryRequest);
        return new ResponseEntity<>(messageResponse, HttpStatus.OK);
    }

    @PutMapping(value = "/api/admin/update")
    public ResponseEntity<Object> updateCategory(@RequestBody CategoryRequest categoryRequest) {
        MessageResponse messageResponse = categoryService.updateCategory(categoryRequest);
        return new ResponseEntity<>(messageResponse, HttpStatus.OK);
    }

    @DeleteMapping(value = "/api/admin/category/idCate={idCategory}")
    public ResponseEntity<Object> deleteCategory(@PathVariable Long idCategory) {
        MessageResponse messageResponse = categoryService.deleteCategory(idCategory);
        return new ResponseEntity<>(messageResponse, HttpStatus.OK);
    }
}
