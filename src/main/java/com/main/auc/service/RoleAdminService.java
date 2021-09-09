package com.main.auc.service;

import com.main.auc.models.Role;
import com.main.auc.payload.response.BaseClientErrorRp;
import com.main.auc.repsitory.RoleRepository;
import com.main.auc.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Service
public class RoleAdminService {

    @Autowired
    private RoleRepository roleRepository;


    public ResponseEntity<?> getListRole(){
        try {

            List<Role> listRole = roleRepository.findAll();
            return ResponseEntity.ok(listRole);
        }catch (Exception e){
            log.info("get list role exception: " + e.toString());
            return ResponseEntity.badRequest().body(
                    BaseClientErrorRp.builder().code(Constants.Base.EXCEPTION)
                    .desc("Exception").build()
            );
        }
    }
}
