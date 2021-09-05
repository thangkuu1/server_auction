package com.main.auc.controllers;


import com.google.gson.Gson;
import com.main.auc.models.ERole;
import com.main.auc.models.Role;
import com.main.auc.models.User;
import com.main.auc.payload.request.LoginRequest;
import com.main.auc.payload.response.BaseClientErrorRp;
import com.main.auc.payload.response.JwtResponse;
import com.main.auc.repsitory.RoleRepository;
import com.main.auc.repsitory.UserRepository;
import com.main.auc.security.jwt.JwtUtils;
import com.main.auc.security.services.UserDetailsImpl;
import com.main.auc.service.ForgotPassService;
import com.main.auc.service.UserService;
import com.main.auc.utils.Constants;
import com.main.auc.utils.SendMailUtils;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/cms/auth")
public class AuthAdminController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserService userService;

    @Autowired
    Gson gson;

    @Autowired
    ForgotPassService forgotPassService;

    @Autowired
    SendMailUtils sendMailUtils;

    @PostMapping("/login")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request (LOGIN-01: khong tim thay user, " +
                    "LOGIN-02: email chua xac thuc, " +
                    "LOGIN-03: mat khau khong chinh xac, " +
                    "LOGIN-05: dang nhap sai role)")
    })
    public ResponseEntity<?> authenticateUserAdmin(@Valid @RequestBody LoginRequest loginRequest) {

        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getEmail());
        if(!userOpt.isPresent()){
            log.info("username not found");
            BaseClientErrorRp rp = BaseClientErrorRp.builder()
                    .code("LOGIN-01")
                    .desc("username not found")
                    .build();
            return ResponseEntity.badRequest().body(rp);
        }

        User user = userOpt.get();
        if(Constants.Login.SIGNUP_INIT.equals(user.getStatus())){
            log.info("user not confirm");
            BaseClientErrorRp rp = BaseClientErrorRp.builder()
                    .code("LOGIN-02")
                    .desc("user not confirm")
                    .build();
            return ResponseEntity.badRequest().body(rp);
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            BaseClientErrorRp rp = BaseClientErrorRp.builder()
                    .code("LOGIN-03")
                    .desc("Password incorrect")
                    .build();
            return ResponseEntity.badRequest().body(rp);
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        log.info("authentication: " + gson.toJson(authentication));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        log.info("userDetails: " + gson.toJson(userDetails));
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());
        log.info("role : " + gson.toJson(roles));
        boolean checkA = roles.contains("ROLE_ADMIN");
        log.info("checkA: " + checkA);
        boolean checkB = roles.contains("ROLE_MODERATOR");
        log.info("checkB: " + checkB);
        if(!checkA && !checkB){
            log.info("role other");
            return ResponseEntity.badRequest().body(BaseClientErrorRp.builder().code("LOGIN-05").desc("check role fail").build());
        }

        return ResponseEntity.ok(JwtResponse.builder()
                .id(userDetails.getId()).email(userDetails.getEmail())
                .username(userDetails.getUsername()).token(jwt)
                .roles(roles)
                .build());
    }

}
