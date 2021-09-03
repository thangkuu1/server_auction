package com.main.auc.controllers;

import com.google.gson.Gson;
import com.main.auc.models.ERole;
import com.main.auc.models.Role;
import com.main.auc.models.User;
import com.main.auc.payload.request.*;
import com.main.auc.payload.response.BaseClientErrorRp;
import com.main.auc.payload.response.JwtResponse;
import com.main.auc.payload.response.MessageResponse;
import com.main.auc.repsitory.RoleRepository;
import com.main.auc.repsitory.UserRepository;
import com.main.auc.security.jwt.JwtUtils;
import com.main.auc.security.services.UserDetailsImpl;
import com.main.auc.service.ForgotPassService;
import com.main.auc.service.UserService;
import com.main.auc.utils.Constants;
import com.main.auc.utils.SendMailUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

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
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getEmail());
        if(!userOpt.isPresent()){
            log.info("username not found");
            BaseClientErrorRp rp = BaseClientErrorRp.builder()
                    .code("01")
                    .desc("username not found")
                    .build();
            return ResponseEntity.badRequest().body(rp);
        }

        User user = userOpt.get();
        if(Constants.Login.SIGNUP_INIT.equals(user.getStatus())){
            log.info("user not confirm");
            BaseClientErrorRp rp = BaseClientErrorRp.builder()
                    .code("01")
                    .desc("user not confirm")
                    .build();
            return ResponseEntity.badRequest().body(rp);
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            BaseClientErrorRp rp = BaseClientErrorRp.builder()
                    .code("02")
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

        return ResponseEntity.ok(JwtResponse.builder()
                .id(userDetails.getId()).email(userDetails.getEmail())
                .username(userDetails.getUsername()).token(jwt)
                .roles(roles)
                .build());
    }

    @PostMapping("login/google")
    public ResponseEntity<?> loginGoogle(@Valid @RequestBody GoogleAuthClientRq rq){
        return userService.loginGoogle(rq);
    }


    @ApiOperation("Đăng nhập bằng facebook, clientID: 540092220598011")
    @PostMapping("login/facebook")
    public ResponseEntity<?> loginFacebook(@RequestBody FbAuthClientRq rq){
        return userService.loginFacebook(rq);
    }


    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) throws UnsupportedEncodingException, MessagingException, javax.mail.MessagingException {
        if (userRepository.existsByUsername(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(MessageResponse.builder().message ("Error: Username is already taken!").build());
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(MessageResponse.builder().message ("Error: Email is already in use!").build());
        }

        String regisCode = RandomString.make(40);
        log.info("regisCode: " + regisCode);
        // Create new user's account
        User user = User.builder()
                .username(signUpRequest.getEmail()).email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .status(Constants.Login.SIGNUP_INIT)
                .loginType(Constants.Login.LOGIN_TYPE_SYS)
                .codeRegis(regisCode)
                .build();


        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);
//        if (strRoles == null) {
//
//        } else {
//            strRoles.forEach(role -> {
//                switch (role) {
//                    case "admin":
//                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
//                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//                        roles.add(adminRole);
//
//                        break;
//                    case "mod":
//                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
//                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//                        roles.add(modRole);
//
//                        break;
//                    default:
//                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
//                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//                        roles.add(userRole);
//                }
//            });
//        }

        user.setRoles(roles);
        CompletableFuture.runAsync(() -> {
            try {
                log.info("begin send Email");
//                userService.sendVerificationEmail(user, Constants.Login.URI_CONFIRM_SIGNUP);
                sendMailUtils.sendSimpleEmailSignUp(user.getEmail(), regisCode);
                log.info("send email success");
            } catch (MessagingException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });

        userRepository.save(user);
//        try {
//            userService.sendVerificationEmail(user, Constants.Login.URI_CONFIRM_SIGNUP);
//        }catch (Exception e){
//            log.info("send email exception: " + e.toString());
//        }

        return ResponseEntity.ok(MessageResponse.builder().message("User registered successfully!").build());
    }

    @PostMapping("/signup/verify")
    public ResponseEntity<?> verifyUserSignUp(@Valid @RequestBody VerifySignUpClientRq rq){
        try {
            return userService.verifyUserSignUp(rq);
        }catch (Exception e) {
            log.info("verify user signup exception: " + e.toString());
            return ResponseEntity.badRequest().body("verify user exception");
        }
    }

    @PostMapping("/forgot/pass")
    public ResponseEntity<?> forgotPass(@RequestBody BaseClientRq rq){
        return forgotPassService.forgotPass(rq);
    }

    @PostMapping("/forgot/pass/verify")
    public ResponseEntity<?> verifyForgotPass(@RequestBody ForgotPassClientRq rq){
        return forgotPassService.verifyForgotPass(rq);
    }
}
