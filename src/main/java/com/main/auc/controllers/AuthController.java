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
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request (LOGIN-01: khong tim thay user, " +
                    "LOGIN-02: email chua xac thuc, " +
                    "LOGIN-03: mat khau khong chinh xac )")
    })
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

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
        String loginFist = "0";
        if("0".equals(user.getLoginFirst())){
            log.info("login first " + user.getEmail());
            loginFist = "1";
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

        return ResponseEntity.ok(JwtResponse.builder()
                .id(userDetails.getId()).email(userDetails.getEmail())
                .username(userDetails.getUsername()).token(jwt)
                .loginFirst(loginFist)
                .roles(roles)
                .build());
    }


    @PostMapping("login/google")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request (TOKEN-01: lay accessToken loi, 96: Exception)")
    })
    public ResponseEntity<?> loginGoogle(@Valid @RequestBody GoogleAuthClientRq rq){
        return userService.loginGoogle(rq);
    }


    @ApiOperation("Đăng nhập bằng facebook, clientID: 540092220598011")
    @PostMapping("login/facebook")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request (TOKEN-01: lay accessToken loi, 96: Exception)")
    })
    public ResponseEntity<?> loginFacebook(@RequestBody FbAuthClientRq rq){
        return userService.loginFacebook(rq);
    }


    @PostMapping("/signup")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request (SIGNUP-01: lay accessToken loi, 96: Exception)")
    })
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) throws UnsupportedEncodingException, MessagingException, javax.mail.MessagingException {
        if (userRepository.existsByUsername(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(BaseClientErrorRp.builder()
                            .code("SIGNUP-01").desc("email is already in use")
                            .build());
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(BaseClientErrorRp.builder()
                            .code("SIGNUP-01").desc("email is already in use")
                            .build());
        }

        String regisCode = RandomString.make(40);
        log.info("regisCode: " + regisCode);
        // Create new user's account
        User user = User.builder()
                .username(signUpRequest.getEmail()).email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .fullName(signUpRequest.getFullName())
                .loginFirst("0")
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
                log.info("begin send Email: " + user.getEmail());
                sendMailUtils.sendSimpleEmailSignUp(user.getEmail(), regisCode);
                log.info("send email success" + user.getEmail());
            } catch (MessagingException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });

        userRepository.save(user);

        return ResponseEntity.ok(BaseClientErrorRp.builder().code(Constants.Base.SUCCESS).desc("sign up success").build());
    }

    @PostMapping("/signup/verify")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request (VERIFY-SIGN-03: khong tim thay user," +
                    "VERIFY-SIGN-01: email da duoc verify" + "VERIFY-02: token xac thuc khong chinh xac"
                    + " 96: Exception)")
    })
    public ResponseEntity<?> verifyUserSignUp(@Valid @RequestBody VerifySignUpClientRq rq){
        try {
            return userService.verifyUserSignUp(rq);
        }catch (Exception e) {
            log.info("verify user signup exception: " + e.toString());
            return ResponseEntity.badRequest().body("verify user exception");
        }
    }

    @PostMapping("/forgot/pass")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request (FORGOT-01: khong tim thay user, " +
                    " 96: Exception)")
    })
    public ResponseEntity<?> forgotPass(@RequestBody BaseClientRq rq){
        return forgotPassService.forgotPass(rq);
    }

    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request (VERIFY-FORGOT-01: khong tim thay user, " +
                    "VERIFY-FORGOT-02: code khong chinh xac, "
                    + " 96: Exception)")
    })
    @PostMapping("/forgot/pass/verify")
    public ResponseEntity<?> verifyForgotPass(@RequestBody ForgotPassClientRq rq){
        return forgotPassService.verifyForgotPass(rq);
    }
}
