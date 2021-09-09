package com.main.auc.controllers;


import com.main.auc.payload.request.UpdateRoleClientRq;
import com.main.auc.service.UserAdminService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/cms")
public class UserAdminController {

    @Autowired
    private UserAdminService userAdminService;

    @GetMapping("/users")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request (96: exception )")
    })
    public ResponseEntity<?> getAllUsers(){
        return userAdminService.getListUsers();
    }


    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request (UPDATE-ROLE-01: khong tim thay user, " +
                    "UPDATE-ROLE-02: update role duplicate ,96: exception )")
    })
    @PostMapping("/user/role")
    public ResponseEntity<?> updateRoleUser(@RequestBody UpdateRoleClientRq rq){
        return userAdminService.updateUserRole(rq);
    }
}
