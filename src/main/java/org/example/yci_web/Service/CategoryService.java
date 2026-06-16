package org.example.yci_web.Service;
import org.example.yci_web.Model.Request.CategoryRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;


public interface CategoryService {
    DataResponse getAllCategory();
    Object getCategoryById(Long idCategory);
    MessageResponse addCategory(CategoryRequest categoryRequest);
    MessageResponse updateCategory(CategoryRequest categoryRequest);
    MessageResponse deleteCategory(Long idCategory);
}
