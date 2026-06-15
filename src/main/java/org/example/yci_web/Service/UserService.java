package org.example.yci_web.Service;

import org.example.yci_web.Model.DTO.UserDTO;
import org.example.yci_web.Model.Request.RegisterRequest;
import org.example.yci_web.Model.Request.UpdatePasswordRequest;
import org.example.yci_web.Model.Request.UpdateProfileRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;
import org.springframework.data.domain.Page;

public interface UserService {
    MessageResponse register(RegisterRequest registerRequest);
    Object login(String email, String password);
    MessageResponse updatePassword(UpdatePasswordRequest updatePasswordRequest);
    MessageResponse updateProfile(UpdateProfileRequest updateProfileRequest);
    Object getUserById(Long idUser);
    Page<UserDTO> getUsers(Integer page);
}
