package org.example.yci_web.Service.Implement;

import org.example.yci_web.Entity.UserEntity;
import org.example.yci_web.Model.DTO.LoginDTO;
import org.example.yci_web.Model.DTO.UserDTO;
import org.example.yci_web.Model.Request.RegisterRequest;
import org.example.yci_web.Model.Request.UpdatePasswordRequest;
import org.example.yci_web.Model.Request.UpdateProfileRequest;
import org.example.yci_web.Model.Request.UpdateUserRoleRequest;
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
            messageResponse.setMessage("Email đã được sử dụng");
            messageResponse.setStatus(HttpStatus.BAD_REQUEST);
        }else {
            UserEntity user = new UserEntity();
            modelMapper.map(registerRequest, user);
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setRole(1);
            userRepository.save(user);
            messageResponse.setMessage("Đăng ký tài khoản thành công");
            messageResponse.setStatus(HttpStatus.CREATED);
        }
        return messageResponse;
    }

    @Override
    public Object login(String email, String password) {
        MessageResponse messageResponse = new MessageResponse();
        DataResponse dataResponse = new DataResponse();
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null) {
            messageResponse.setMessage("Không tìm thấy tài khoản");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        if (!isPasswordMatched(password, userEntity.getPassword())) {
            messageResponse.setMessage("Email hoặc mật khẩu không đúng");
            messageResponse.setStatus(HttpStatus.UNAUTHORIZED);
            return messageResponse;
        }
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail(email);
        loginDTO.setIdUser(userEntity.getIdUser());
        dataResponse.setMessage("Success");
        dataResponse.setStatus(HttpStatus.OK);
        dataResponse.setData(loginDTO);
        return dataResponse;
    }

    @Override
    public MessageResponse updatePassword(UpdatePasswordRequest updatePasswordRequest) {
        MessageResponse messageResponse = new MessageResponse();
        try {
            UserEntity userEntity = userRepository.findByEmail(updatePasswordRequest.getEmail());
            if (userEntity == null) {
                messageResponse.setMessage("Không tìm thấy tài khoản");
                messageResponse.setStatus(HttpStatus.NOT_FOUND);
                return messageResponse;
            }
            if (isPasswordMatched(updatePasswordRequest.getOldPassword(), userEntity.getPassword())) {
                userEntity.setPassword(passwordEncoder.encode(updatePasswordRequest.getNewPassword()));
                userRepository.save(userEntity);
                messageResponse.setMessage("Đổi mật khẩu thành công");
                messageResponse.setStatus(HttpStatus.OK);
            }else {
                messageResponse.setMessage("Mật khẩu cũ không đúng");
                messageResponse.setStatus(HttpStatus.BAD_REQUEST);
            }

        }catch (NoSuchElementException e) {
            messageResponse.setMessage("Không tìm thấy tài khoản");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        return messageResponse;
    }

    private boolean isPasswordMatched(String rawPassword, String savedPassword) {
        if (rawPassword == null || savedPassword == null) {
            return false;
        }
        if (savedPassword.startsWith("$2a$") || savedPassword.startsWith("$2b$") || savedPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, savedPassword);
        }
        return savedPassword.equals(rawPassword);
    }

    @Override
    public MessageResponse updateProfile(UpdateProfileRequest updateProfileRequest) {
        MessageResponse messageResponse = new MessageResponse();
        try {
            UserEntity userEntity = null;
            if (updateProfileRequest.getIdUser() != null) {
                userEntity = userRepository.findById(updateProfileRequest.getIdUser()).orElse(null);
            }
            if (userEntity == null) {
                userEntity = userRepository.findByEmail(updateProfileRequest.getEmail());
            }
            if (userEntity == null) {
                messageResponse.setMessage("Không tìm thấy tài khoản");
                messageResponse.setStatus(HttpStatus.NOT_FOUND);
                return messageResponse;
            }
            modelMapper.map(updateProfileRequest, userEntity);
            userEntity.setUpdatedAt(LocalDateTime.now());
            userRepository.save(userEntity);
            messageResponse.setMessage("Cập nhật thông tin thành công");
            messageResponse.setStatus(HttpStatus.OK);
        }catch (NoSuchElementException e) {
            messageResponse.setMessage("Không tìm thấy tài khoản");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
        }
        return messageResponse;
    }

    @Override
    public MessageResponse updateRole(UpdateUserRoleRequest updateUserRoleRequest) {
        MessageResponse messageResponse = new MessageResponse();
        if (updateUserRoleRequest.getRole() == null || updateUserRoleRequest.getRole() < 0 || updateUserRoleRequest.getRole() > 2) {
            messageResponse.setMessage("Vai trò không hợp lệ");
            messageResponse.setStatus(HttpStatus.BAD_REQUEST);
            return messageResponse;
        }
        try {
            UserEntity userEntity = userRepository.findById(updateUserRoleRequest.getIdUser()).get();
            userEntity.setRole(updateUserRoleRequest.getRole());
            userEntity.setUpdatedAt(LocalDateTime.now());
            userRepository.save(userEntity);
            messageResponse.setMessage("Cập nhật phân quyền thành công");
            messageResponse.setStatus(HttpStatus.OK);
            return messageResponse;
        } catch (NoSuchElementException e) {
            messageResponse.setMessage("Không tìm thấy tài khoản");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
    }

    @Override
    public Object getUserById(Long idUser) {
        MessageResponse messageResponse = new MessageResponse();
        DataResponse dataResponse = new DataResponse();
        UserDTO userDTO = new UserDTO();
        try{
            UserEntity userEntity = userRepository.findById(idUser).get();
            modelMapper.map(userEntity, userDTO);
            dataResponse.setMessage("Thành công");
            dataResponse.setStatus(HttpStatus.OK);
            dataResponse.setData(userDTO);
        }catch (NoSuchElementException e) {
            messageResponse.setMessage("Không tìm thấy tài khoản");
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
