package com.main.auc.controllers;

import com.main.auc.service.UserService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request (USER-01: khong tim thay user, " +
                    "96: exception )")
    })
    public ResponseEntity<?> getUser(@RequestHeader(value = "Authorization") String rq){
        return userService.getUser(rq);
    }
}
