package org.example.yci_web.Service.Implement;

import org.example.yci_web.Entity.CategoryEntity;
import org.example.yci_web.Model.DTO.CategoryDTO;
import org.example.yci_web.Model.Request.CategoryRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;
import org.example.yci_web.Repository.CategoryRepository;
import org.example.yci_web.Service.CategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    ModelMapper modelMapper;

    @Autowired
    CategoryRepository categoryRepository;

    @Override
    public DataResponse getAllCategory() {
        List<CategoryDTO> categoryDTOS = new ArrayList<>();
        DataResponse dataResponse = new DataResponse();
        List<CategoryEntity> categoryEntities = categoryRepository.findAll();
        for (CategoryEntity categoryEntity : categoryEntities) {
            CategoryDTO categoryDTO = new CategoryDTO();
            modelMapper.map( categoryEntity, categoryDTO );
            categoryDTOS.add( categoryDTO );
        }
        dataResponse.setData(categoryDTOS);
        dataResponse.setMessage("Success");
        dataResponse.setStatus(HttpStatus.OK);
        return dataResponse;
    }

    @Override
    public Object getCategoryById(Long idCategory) {
        MessageResponse messageResponse = new MessageResponse();
        DataResponse dataResponse = new DataResponse();
        try{
            CategoryDTO categoryDTO = new CategoryDTO();
            CategoryEntity categoryEntity = categoryRepository.findById(idCategory).get();
            modelMapper.map(categoryEntity, categoryDTO);
            dataResponse.setData(categoryDTO);
            dataResponse.setMessage("Success");
            dataResponse.setStatus(HttpStatus.OK);
        }catch (NoSuchElementException ex){
            messageResponse.setMessage("No such Category");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        return dataResponse;
    }

    @Override
    public MessageResponse addCategory(CategoryRequest categoryRequest) {
        MessageResponse messageResponse = new MessageResponse();
        CategoryEntity categoryEntity = new CategoryEntity();
        modelMapper.map(categoryRequest, categoryEntity);
        categoryRepository.save(categoryEntity);
        categoryEntity.setCreatedAt(LocalDateTime.now());
        messageResponse.setMessage("Success");
        messageResponse.setStatus(HttpStatus.OK);
        return messageResponse;
    }

    @Override
    public MessageResponse updateCategory(CategoryRequest categoryRequest) {
        MessageResponse messageResponse = new MessageResponse();
        try{
            CategoryEntity categoryEntity = categoryRepository.findById(categoryRequest.getIdCategory()).get();
            modelMapper.map(categoryRequest, categoryEntity);
            categoryRepository.save(categoryEntity);
            messageResponse.setMessage("Success");
            messageResponse.setStatus(HttpStatus.OK);
        }catch (NoSuchElementException ex){
            messageResponse.setMessage("No such Category");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
        }
        return messageResponse;
    }

    @Override
    public MessageResponse deleteCategory(Long idCategory) {
        MessageResponse messageResponse = new MessageResponse();
        try {
            CategoryEntity categoryEntity = categoryRepository.findById(idCategory).get();
            categoryRepository.delete(categoryEntity);
            messageResponse.setMessage("Success");
            messageResponse.setStatus(HttpStatus.OK);
        }catch (NoSuchElementException ex){
            messageResponse.setMessage("No such Category");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
        }
        return messageResponse;
    }
}
