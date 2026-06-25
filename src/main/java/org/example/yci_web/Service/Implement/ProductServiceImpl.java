package org.example.yci_web.Service.Implement;

import org.example.yci_web.Entity.*;
import org.example.yci_web.Model.DTO.ImageDTO;
import org.example.yci_web.Model.DTO.ProductDTO;
import org.example.yci_web.Model.DTO.RentalPriceDTO;
import org.example.yci_web.Model.Request.ProductRequest;
import org.example.yci_web.Model.Request.RentalPriceProductRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;
import org.example.yci_web.Repository.*;
import org.example.yci_web.Service.ProductService;
import org.example.yci_web.Utils.ConvertByteToBase64;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    ProductRepository productRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    StatusRepository statusRepository;
    @Autowired
    RentalTypeRepository rentalTypeRepository;
    @Autowired
    RentalPriceRepository rentalPriceRepository;
    @Autowired
    ImageRepository imageRepository;
    @Autowired
    ModelMapper modelMapper;

    @Override
    public Page<ProductDTO> getAllProducts(Integer page) {
        Pageable pageable = PageRequest.of(page - 1, 10);
        Page<ProductEntity> productEntities = productRepository.findAll(pageable);
        List<ProductDTO> productDTOS = new ArrayList<>();
        for (ProductEntity productEntity : productEntities) {
            ProductDTO productDTO = new ProductDTO();
            modelMapper.map(productEntity, productDTO);
            productDTO.setCategoryId(productEntity.getCategoryEntity().getIdCategory());
            productDTO.setCategoryName(productEntity.getCategoryEntity().getNameCategory());
            productDTO.setStatus(productEntity.getStatusEntity().getStatusCode());

            List<ImageDTO> imageDTOS = new ArrayList<>();
            for (ImageEntity imageEntity : productEntity.getImageEntities()) {
                ImageDTO imageDTO = new ImageDTO();
                imageDTO.setIdImage(imageEntity.getIdImage());
                imageDTO.setImageBase64(ConvertByteToBase64.toBase64(imageEntity.getImage()));
                imageDTOS.add(imageDTO);
            }
            productDTO.setImageDTOS(imageDTOS);

            List<RentalPriceDTO> rentalPriceDTOS = new ArrayList<>();
            for (RentalPriceEntity rentalPriceEntity : productEntity.getRentailPriceEntities()) {
                RentalPriceDTO rentalPriceDTO = new RentalPriceDTO();
                modelMapper.map(rentalPriceEntity, rentalPriceDTO);
                rentalPriceDTO.setRentalType(rentalPriceEntity.getRentalTypeEntity().getType());
                rentalPriceDTOS.add(rentalPriceDTO);
            }
            productDTO.setRentalPriceDTOS(rentalPriceDTOS);

            productDTOS.add(productDTO);
        }
        return new PageImpl<>(productDTOS, productEntities.getPageable(), productEntities.getTotalElements());
    }

    @Override
    public DataResponse getAllProducts() {
        List<ProductEntity> productEntities = productRepository.findAll();
        List<ProductDTO> productDTOS = new ArrayList<>();
        DataResponse dataResponse = new DataResponse();
        for (ProductEntity productEntity : productEntities) {
            ProductDTO productDTO = new ProductDTO();
            modelMapper.map(productEntity, productDTO);
            productDTO.setCategoryId(productEntity.getCategoryEntity().getIdCategory());
            productDTO.setCategoryName(productEntity.getCategoryEntity().getNameCategory());
            productDTO.setStatus(productEntity.getStatusEntity().getStatusCode());

            List<ImageDTO> imageDTOS = new ArrayList<>();
            for (ImageEntity imageEntity : productEntity.getImageEntities()) {
                ImageDTO imageDTO = new ImageDTO();
                imageDTO.setIdImage(imageEntity.getIdImage());
                imageDTO.setImageBase64(ConvertByteToBase64.toBase64(imageEntity.getImage()));
                imageDTOS.add(imageDTO);
            }
            productDTO.setImageDTOS(imageDTOS);

            List<RentalPriceDTO> rentalPriceDTOS = new ArrayList<>();
            for (RentalPriceEntity rentalPriceEntity : productEntity.getRentailPriceEntities()) {
                RentalPriceDTO rentalPriceDTO = new RentalPriceDTO();
                modelMapper.map(rentalPriceEntity, rentalPriceDTO);
                rentalPriceDTO.setRentalType(rentalPriceEntity.getRentalTypeEntity().getType());
                rentalPriceDTOS.add(rentalPriceDTO);
            }
            productDTO.setRentalPriceDTOS(rentalPriceDTOS);

            productDTOS.add(productDTO);
        }
        dataResponse.setMessage("Success");
        dataResponse.setData(productDTOS);
        dataResponse.setStatus(HttpStatus.OK);
        return dataResponse;
    }

    @Override
    public Object getAllProductsByCategory(Long idCategory) {
        MessageResponse messageResponse = new MessageResponse();
        DataResponse dataResponse = new DataResponse();
        CategoryEntity categoryEntity = null;
        try {
            categoryEntity = categoryRepository.findById(idCategory).get();
        } catch (NoSuchElementException ex) {
            messageResponse.setMessage("Không tìm thấy danh mục");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        List<ProductEntity> productEntities = productRepository.findByCategoryEntity(categoryEntity);
        List<ProductDTO> productDTOS = new ArrayList<>();
        for (ProductEntity productEntity : productEntities) {
            ProductDTO productDTO = new ProductDTO();
            modelMapper.map(productEntity, productDTO);
            productDTO.setCategoryId(productEntity.getCategoryEntity().getIdCategory());
            productDTO.setCategoryName(productEntity.getCategoryEntity().getNameCategory());
            productDTO.setStatus(productEntity.getStatusEntity().getStatusCode());

            List<ImageDTO> imageDTOS = new ArrayList<>();
            for (ImageEntity imageEntity : productEntity.getImageEntities()) {
                ImageDTO imageDTO = new ImageDTO();
                imageDTO.setIdImage(imageEntity.getIdImage());
                imageDTO.setImageBase64(ConvertByteToBase64.toBase64(imageEntity.getImage()));
                imageDTOS.add(imageDTO);
            }
            productDTO.setImageDTOS(imageDTOS);

            List<RentalPriceDTO> rentalPriceDTOS = new ArrayList<>();
            for (RentalPriceEntity rentalPriceEntity : productEntity.getRentailPriceEntities()) {
                RentalPriceDTO rentalPriceDTO = new RentalPriceDTO();
                modelMapper.map(rentalPriceEntity, rentalPriceDTO);
                rentalPriceDTO.setRentalType(rentalPriceEntity.getRentalTypeEntity().getType());
                rentalPriceDTOS.add(rentalPriceDTO);
            }
            productDTO.setRentalPriceDTOS(rentalPriceDTOS);

            productDTOS.add(productDTO);
        }
        dataResponse.setMessage("Success");
        dataResponse.setData(productDTOS);
        dataResponse.setStatus(HttpStatus.OK);
        return dataResponse;
    }

    @Override
    public Object getProductById(Long idProduct) {
        MessageResponse messageResponse = new MessageResponse();
        DataResponse dataResponse = new DataResponse();
        try {
            ProductEntity productEntity = productRepository.findById(idProduct).get();
            ProductDTO productDTO = new ProductDTO();
            modelMapper.map(productEntity, productDTO);
            productDTO.setCategoryId(productEntity.getCategoryEntity().getIdCategory());
            productDTO.setCategoryName(productEntity.getCategoryEntity().getNameCategory());
            productDTO.setStatus(productEntity.getStatusEntity().getStatusCode());

            List<ImageDTO> imageDTOS = new ArrayList<>();
            for (ImageEntity imageEntity : productEntity.getImageEntities()) {
                ImageDTO imageDTO = new ImageDTO();
                imageDTO.setIdImage(imageEntity.getIdImage());
                imageDTO.setImageBase64(ConvertByteToBase64.toBase64(imageEntity.getImage()));
                imageDTOS.add(imageDTO);
            }
            productDTO.setImageDTOS(imageDTOS);

            List<RentalPriceDTO> rentalPriceDTOS = new ArrayList<>();
            for (RentalPriceEntity rentalPriceEntity : productEntity.getRentailPriceEntities()) {
                RentalPriceDTO rentalPriceDTO = new RentalPriceDTO();
                modelMapper.map(rentalPriceEntity, rentalPriceDTO);
                rentalPriceDTO.setRentalType(rentalPriceEntity.getRentalTypeEntity().getType());
                rentalPriceDTOS.add(rentalPriceDTO);
            }
            productDTO.setRentalPriceDTOS(rentalPriceDTOS);

            dataResponse.setMessage("Success");
            dataResponse.setData(productDTO);
            dataResponse.setStatus(HttpStatus.OK);
        } catch (NoSuchElementException ex) {
            messageResponse.setMessage("Không tìm thấy thiết bị");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        return dataResponse;
    }

    @Override
    public MessageResponse addProduct(ProductRequest productRequest) {
        MessageResponse messageResponse = new MessageResponse();
        CategoryEntity categoryEntity = null;
        StatusEntity statusEntity = null;
        try {
            categoryEntity = categoryRepository.findById(productRequest.getIdCategory()).get();
        } catch (NoSuchElementException ex) {
            messageResponse.setMessage("Không tìm thấy danh mục");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        statusEntity = statusRepository.findByStatusCode("AVAILABLE");
        if (statusEntity == null) {
            messageResponse.setMessage("Không tìm thấy trạng thái thiết bị AVAILABLE");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        if (!hasUploadedImages(productRequest.getImages())) {
            messageResponse.setMessage("Vui lòng chọn ít nhất một hình ảnh thiết bị");
            messageResponse.setStatus(HttpStatus.BAD_REQUEST);
            return messageResponse;
        }
        ProductEntity productEntity = new ProductEntity();
        modelMapper.map(productRequest, productEntity);
        productEntity.setCategoryEntity(categoryEntity);
        productEntity.setStatusEntity(statusEntity);
        productEntity.setCreatedAt(LocalDateTime.now());
        productEntity.setUpdatedAt(LocalDateTime.now());
        List<ImageEntity> imageEntities = new ArrayList<>();
        for (MultipartFile image : productRequest.getImages()) {
            if (image == null || image.isEmpty()) {
                continue;
            }
            ImageEntity imageEntity = new ImageEntity();
            imageEntity.setProductEntity(productEntity);
            try {
                imageEntity.setImage(image.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            imageEntities.add(imageEntity);
        }
        productEntity.setImageEntities(imageEntities);
        productRepository.save(productEntity);
        messageResponse.setMessage("Thêm thiết bị thành công");
        messageResponse.setStatus(HttpStatus.OK);
        return messageResponse;
    }

    @Override
    public MessageResponse updateProduct(ProductRequest productRequest) {
        MessageResponse messageResponse = new MessageResponse();
        ProductEntity productEntity = null;
        CategoryEntity categoryEntity = null;
        StatusEntity statusEntity = null;
        try {
            categoryEntity = categoryRepository.findById(productRequest.getIdCategory()).get();
        } catch (NoSuchElementException ex) {
            messageResponse.setMessage("Không tìm thấy danh mục");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        statusEntity = statusRepository.findByStatusCode("AVAILABLE");
        if (statusEntity == null) {
            messageResponse.setMessage("Không tìm thấy trạng thái thiết bị AVAILABLE");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        try{
            productEntity = productRepository.findById(productRequest.getIdProduct()).get();
            modelMapper.map(productRequest, productEntity);
            productEntity.setCategoryEntity(categoryEntity);
            productEntity.setStatusEntity(statusEntity);
            productEntity.setUpdatedAt(LocalDateTime.now());
            if (hasUploadedImages(productRequest.getImages())) {
                for (MultipartFile image : productRequest.getImages()) {
                    if (image == null || image.isEmpty()) {
                        continue;
                    }

                    ImageEntity imageEntity = new ImageEntity();
                    imageEntity.setProductEntity(productEntity);
                    try {
                        imageEntity.setImage(image.getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    productEntity.getImageEntities().add(imageEntity);
                }
            }
            productEntity.setUpdatedAt(LocalDateTime.now());
            productRepository.save(productEntity);
            messageResponse.setMessage("Cập nhật thiết bị thành công");
            messageResponse.setStatus(HttpStatus.OK);
        }catch (NoSuchElementException ex){
            messageResponse.setMessage("Không tìm thấy thiết bị");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        return messageResponse;
    }

    @Override
    public MessageResponse deleteProduct(Long idProduct) {
        MessageResponse messageResponse = new MessageResponse();
        try {
            ProductEntity productEntity = productRepository.findById(idProduct).get();
            productRepository.delete(productEntity);
            messageResponse.setMessage("Xóa thiết bị thành công");
            messageResponse.setStatus(HttpStatus.OK);
            return messageResponse;
        }catch (NoSuchElementException ex){
            messageResponse.setMessage("Không tìm thấy thiết bị");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
    }

    @Override
    public MessageResponse deleteProductImage(Long idProduct, Long idImage) {
        MessageResponse messageResponse = new MessageResponse();
        return imageRepository.findByIdImageAndProductEntity_IdProduct(idImage, idProduct)
                .map(imageEntity -> {
                    imageRepository.delete(imageEntity);
                    messageResponse.setMessage("Xóa hình ảnh thành công");
                    messageResponse.setStatus(HttpStatus.OK);
                    return messageResponse;
                })
                .orElseGet(() -> {
                    messageResponse.setMessage("Không tìm thấy hình ảnh của thiết bị");
                    messageResponse.setStatus(HttpStatus.NOT_FOUND);
                    return messageResponse;
                });
    }

    @Override
    public MessageResponse addRentalPriceProduct(RentalPriceProductRequest rentalPriceProductRequest) {
        MessageResponse messageResponse = new MessageResponse();
        ProductEntity productEntity = null;
        RentalTypeEntity rentalTypeEntity = null;
        if (rentalPriceProductRequest.getProductId() == null
                || rentalPriceProductRequest.getTypeId() == null
                || rentalPriceProductRequest.getPrice() == null
                || rentalPriceProductRequest.getPrice() <= 0) {
            messageResponse.setMessage("Vui lòng nhập đủ thiết bị, loại giá và giá thuê hợp lệ");
            messageResponse.setStatus(HttpStatus.BAD_REQUEST);
            return messageResponse;
        }
        try{
            productEntity = productRepository.findById(rentalPriceProductRequest.getProductId()).get();
        }catch (NoSuchElementException ex){
            messageResponse.setMessage("Không tìm thấy thiết bị");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        try{
            rentalTypeEntity = rentalTypeRepository.findById(rentalPriceProductRequest.getTypeId()).get();
        }catch (NoSuchElementException ex){
            messageResponse.setMessage("Không tìm thấy loại giá thuê");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        RentalPriceEntity rentalPriceEntity = new RentalPriceEntity();
        rentalPriceEntity.setPrice(rentalPriceProductRequest.getPrice());
        rentalPriceEntity.setProductEntity(productEntity);
        rentalPriceEntity.setRentalTypeEntity(rentalTypeEntity);
        rentalPriceRepository.save(rentalPriceEntity);
        messageResponse.setMessage("Thêm giá thuê thành công");
        messageResponse.setStatus(HttpStatus.OK);
        return messageResponse;
    }

    @Override
    public MessageResponse updateRentalPriceProduct(RentalPriceProductRequest rentalPriceProductRequest) {
        MessageResponse messageResponse = new MessageResponse();
        if (rentalPriceProductRequest.getIdRentalPrice() == null) {
            messageResponse.setMessage("Thiếu mã giá thuê cần cập nhật");
            messageResponse.setStatus(HttpStatus.BAD_REQUEST);
            return messageResponse;
        }
        if (rentalPriceProductRequest.getPrice() != null && rentalPriceProductRequest.getPrice() <= 0) {
            messageResponse.setMessage("Giá thuê phải lớn hơn 0");
            messageResponse.setStatus(HttpStatus.BAD_REQUEST);
            return messageResponse;
        }
        try {
            RentalPriceEntity rentalPriceEntity = rentalPriceRepository.findById(rentalPriceProductRequest.getIdRentalPrice()).get();
            if (rentalPriceProductRequest.getPrice() != null) {
                rentalPriceEntity.setPrice(rentalPriceProductRequest.getPrice());
            }
            if (rentalPriceProductRequest.getProductId() != null) {
                ProductEntity productEntity = productRepository.findById(rentalPriceProductRequest.getProductId()).get();
                rentalPriceEntity.setProductEntity(productEntity);
            }
            if (rentalPriceProductRequest.getTypeId() != null) {
                RentalTypeEntity rentalTypeEntity = rentalTypeRepository.findById(rentalPriceProductRequest.getTypeId()).get();
                rentalPriceEntity.setRentalTypeEntity(rentalTypeEntity);
            }
            rentalPriceRepository.save(rentalPriceEntity);
            messageResponse.setMessage("Cập nhật giá thuê thành công");
            messageResponse.setStatus(HttpStatus.OK);
            return messageResponse;
        } catch (NoSuchElementException ex) {
            messageResponse.setMessage("Không tìm thấy giá thuê, thiết bị hoặc loại giá");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
    }

    @Override
    public MessageResponse deleteRentalPriceProduct(Long idRentalPrice) {
        MessageResponse messageResponse = new MessageResponse();
        try {
            RentalPriceEntity rentalPriceEntity = rentalPriceRepository.findById(idRentalPrice).get();
            rentalPriceRepository.delete(rentalPriceEntity);
            messageResponse.setMessage("Xóa giá thuê thành công");
            messageResponse.setStatus(HttpStatus.OK);
            return messageResponse;
        } catch (NoSuchElementException ex) {
            messageResponse.setMessage("Không tìm thấy giá thuê");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
    }

    private boolean hasUploadedImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return false;
        }
        for (MultipartFile image : images) {
            if (image != null && !image.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
