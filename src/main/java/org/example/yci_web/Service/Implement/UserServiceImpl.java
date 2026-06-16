package org.example.yci_web.Service.Implement;

import org.example.yci_web.Entity.UserEntity;
import org.example.yci_web.Model.DTO.LoginDTO;
import org.example.yci_web.Model.DTO.UserDTO;
import org.example.yci_web.Model.Request.RegisterRequest;
import org.example.yci_web.Model.Request.UpdatePasswordRequest;
import org.example.yci_web.Model.Request.UpdateProfileRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;
import org.example.yci_web.Repository.UserRepository;
import org.example.yci_web.Service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public MessageResponse register(RegisterRequest registerRequest) {
        MessageResponse messageResponse = new MessageResponse();
        UserEntity userEntity = userRepository.findByEmail(registerRequest.getEmail());
        if (userEntity != null) {
            messageResponse.setMessage("Email already exists");
            messageResponse.setStatus(HttpStatus.BAD_GATEWAY);
        }else {
            UserEntity user = new UserEntity();
            modelMapper.map(registerRequest, user);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setRole(1);
            userRepository.save(user);
            messageResponse.setMessage("Success");
            messageResponse.setStatus(HttpStatus.CREATED);
        }
        return messageResponse;
    }

    @Override
    public Object login(String email, String password) {
        MessageResponse messageResponse = new MessageResponse();
        DataResponse dataResponse = new DataResponse();
        try {
            UserEntity userEntity = userRepository.findByEmail(email);
            if(userEntity.getPassword().equals(passwordEncoder.encode(password))) {
                LoginDTO loginDTO = new LoginDTO();
                loginDTO.setEmail(email);
                loginDTO.setIdUser(userEntity.getIdUser());
                dataResponse.setMessage("Success");
                dataResponse.setStatus(HttpStatus.OK);
                dataResponse.setData(loginDTO);
            }
        }catch (NoSuchElementException e) {
            messageResponse.setMessage("Can not find user");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        return dataResponse;
    }

    @Override
    public MessageResponse updatePassword(UpdatePasswordRequest updatePasswordRequest) {
        MessageResponse messageResponse = new MessageResponse();
        try {
            UserEntity userEntity = userRepository.findByEmail(updatePasswordRequest.getEmail());
            if (userEntity.getPassword().equals(passwordEncoder.encode(updatePasswordRequest.getOldPassword()))) {
                userEntity.setPassword(passwordEncoder.encode(updatePasswordRequest.getNewPassword()));
                userRepository.save(userEntity);
                messageResponse.setMessage("Success");
                messageResponse.setStatus(HttpStatus.OK);
            }else {
                messageResponse.setMessage("Old password does not match");
                messageResponse.setStatus(HttpStatus.BAD_REQUEST);
            }

        }catch (NoSuchElementException e) {
            messageResponse.setMessage("Can not find user");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        return messageResponse;
    }

    @Override
    public MessageResponse updateProfile(UpdateProfileRequest updateProfileRequest) {
        MessageResponse messageResponse = new MessageResponse();
        try {
            UserEntity userEntity = userRepository.findByEmail(updateProfileRequest.getEmail());
            modelMapper.map(updateProfileRequest, userEntity);
            userEntity.setUpdatedAt(LocalDateTime.now());
            userRepository.save(userEntity);
            messageResponse.setMessage("Success");
            messageResponse.setStatus(HttpStatus.OK);
        }catch (NoSuchElementException e) {
            messageResponse.setMessage("Can not find user");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
        }
        return messageResponse;
    }

    @Override
    public Object getUserById(Long idUser) {
        MessageResponse messageResponse = new MessageResponse();
        DataResponse dataResponse = new DataResponse();
        UserDTO userDTO = new UserDTO();
        try{
            UserEntity userEntity = userRepository.findById(idUser).get();
            modelMapper.map(userEntity, userDTO);
            dataResponse.setMessage("Success");
            dataResponse.setStatus(HttpStatus.OK);
            dataResponse.setData(userDTO);
        }catch (NoSuchElementException e) {
            messageResponse.setMessage("Can not find user");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        return dataResponse;
    }

    @Override
    public Page<UserDTO> getUsers(Integer page) {
        Pageable pageable = PageRequest.of(page - 1, 10);
        List<UserDTO> userDTOS = new ArrayList<>();
        Page<UserEntity> userEntities = userRepository.findAll(pageable);
        for (UserEntity userEntity : userEntities) {
            UserDTO userDTO = new UserDTO();
            modelMapper.map(userEntity, userDTO);
            userDTOS.add(userDTO);
        }
        return new PageImpl<>(userDTOS, userEntities.getPageable(), userEntities.getTotalElements());
    }
}
