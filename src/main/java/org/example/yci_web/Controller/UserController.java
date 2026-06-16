package org.example.yci_web.Controller;

import org.example.yci_web.Model.DTO.UserDTO;
import org.example.yci_web.Model.Request.RegisterRequest;
import org.example.yci_web.Model.Request.UpdatePasswordRequest;
import org.example.yci_web.Model.Request.UpdateProfileRequest;
import org.example.yci_web.Model.Response.DataPageResponse;
import org.example.yci_web.Model.Response.MessageResponse;
import org.example.yci_web.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    @Autowired
    UserService userService;

    @PostMapping(value = "/api/login")
    public ResponseEntity<Object> login(@RequestParam(name = "email") String email, @RequestParam(name = "password") String password) {
        Object result = userService.login(email, password);
        if (result instanceof MessageResponse) {
            return new ResponseEntity<>(result, ((MessageResponse) result).getStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/api/register")
    public ResponseEntity<Object> register(@RequestBody RegisterRequest registerRequest) {
        MessageResponse response = userService.register(registerRequest);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @PutMapping(value = "/api/password")
    public ResponseEntity<Object> updatePassword(@RequestBody UpdatePasswordRequest updatePasswordRequest) {
        MessageResponse response = userService.updatePassword(updatePasswordRequest);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @PutMapping(value = "/api/profile")
    public ResponseEntity<Object> updateProfile(@RequestBody UpdateProfileRequest updateProfileRequest) {
        MessageResponse response = userService.updateProfile(updateProfileRequest);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @GetMapping(value = "/api/user/id={idUser}")
    public ResponseEntity<Object> getUser(@PathVariable("idUser") Long idUser) {
        Object result = userService.getUserById(idUser);
        if(result instanceof MessageResponse) {
            return new ResponseEntity<>(result, ((MessageResponse) result).getStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = "/api/user")
    public ResponseEntity<Object> getUsers(@RequestParam(name = "page", defaultValue = "1") Integer page) {
        Page<UserDTO> userDTOS = userService.getUsers(page);
        DataPageResponse dataPageResponse = new DataPageResponse();
        dataPageResponse.setData(userDTOS.getContent());
        dataPageResponse.setTotalPage(userDTOS.getTotalPages());
        dataPageResponse.setMessage("Success");
        dataPageResponse.setCurrentPage(page);
        dataPageResponse.setStatus(HttpStatus.OK);
        return new ResponseEntity<>(dataPageResponse, HttpStatus.OK);
    }
}
