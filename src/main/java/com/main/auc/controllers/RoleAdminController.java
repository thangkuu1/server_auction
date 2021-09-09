package com.main.auc.controllers;

import com.main.auc.service.RoleAdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/cms")
public class RoleAdminController {

    @Autowired
    private RoleAdminService roleAdminService;

    @GetMapping("/roles")
    public ResponseEntity<?> getListRoles(){
        return roleAdminService.getListRole();
    }

}
