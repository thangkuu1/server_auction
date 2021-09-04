package com.main.auc.controllers;

import com.main.auc.payload.request.ChangePassClientRq;
import com.main.auc.service.ChangePassService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class ChangePassController {

    @Autowired
    private ChangePassService changePassService;

    @PostMapping("/change/pass")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request (USER-01: khong tim thay user, " +
                    "CHANGE-PASS-02: mat khau khong chinh xac, " +
                    "96: exception )")
    })
    public ResponseEntity<?> changePass(@RequestBody ChangePassClientRq rq){
        return changePassService.changePass(rq);
    }
}
