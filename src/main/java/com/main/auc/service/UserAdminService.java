package com.main.auc.service;

import com.main.auc.dto.AdminUserDto;
import com.main.auc.dto.UserDto;
import com.main.auc.models.ERole;
import com.main.auc.models.Role;
import com.main.auc.models.User;
import com.main.auc.payload.request.UpdateRoleClientRq;
import com.main.auc.payload.response.BaseClientErrorRp;
import com.main.auc.repsitory.RoleRepository;
import com.main.auc.repsitory.UserRepository;
import com.main.auc.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserAdminService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    public ResponseEntity<?> getListUsers(){
        try {

            List<User> listUser = userRepository.findAll();
            List<AdminUserDto> listUserDto = listUser.stream().map(u -> {
                List<ERole> roles = u.getRoles().stream()
                        .map(item -> item.getName())
                        .collect(Collectors.toList());
                AdminUserDto userDto = AdminUserDto.builder()
                        .id(u.getId()).email(u.getEmail())
                        .fullName(u.getFullName()).address(u.getAddress())
                        .phoneNumber(u.getPhoneNumber())
                        .roles(roles)
                        .build();
                return  userDto;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(listUserDto);
        }catch (Exception e){
            log.info("get list users exception: " + e.toString());
            return ResponseEntity.badRequest().body(BaseClientErrorRp.builder().code(Constants.Base.EXCEPTION)
            .desc("exception").build());
        }
    }

    public ResponseEntity<?> updateUserRole(UpdateRoleClientRq rq){

        try {
            Optional<User> userOpt = userRepository.findById(Long.parseLong(rq.getId()));
            if(!userOpt.isPresent()){
                log.info("get user exception");
                return ResponseEntity.badRequest().body(BaseClientErrorRp.builder().code("UPDATE-ROLE-01")
                        .desc("user not found").build());
            }
            User user = userOpt.get();
            Set<Role> setRoleUser = user.getRoles();
            Role roleAdd = roleRepository.findById(Integer.parseInt(rq.getRoleId())).orElseThrow(null);
            if(setRoleUser.contains(roleAdd)){
                log.info("add role duplicate");
                return ResponseEntity.badRequest().body(BaseClientErrorRp.builder().code("UPDATE-ROLE-02")
                        .desc("update role duplicate").build());
            }
            setRoleUser.add(roleAdd);
            user.setRoles(setRoleUser);
            userRepository.save(user);
            return  ResponseEntity.ok().body(BaseClientErrorRp.builder().code(Constants.Base.SUCCESS).desc("success").build());
        }catch (Exception e){
            log.info("update user role exception: " + e.toString());
            return ResponseEntity.badRequest().body(BaseClientErrorRp.builder().code(Constants.Base.EXCEPTION)
                    .desc("exception").build());
        }
    }
}
