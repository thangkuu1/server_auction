package com.main.auc.service;

import com.google.gson.Gson;
import com.main.auc.bo.FacebookAuthData;
import com.main.auc.bo.GoogleAuthUserInfoData;
import com.main.auc.dto.UserDto;
import com.main.auc.models.ERole;
import com.main.auc.models.Role;
import com.main.auc.models.User;
import com.main.auc.payload.request.FbAuthClientRq;
import com.main.auc.payload.request.GoogleAuthClientRq;
import com.main.auc.payload.request.UserUpdateClientRq;
import com.main.auc.payload.request.VerifySignUpClientRq;
import com.main.auc.payload.response.BaseClientErrorRp;
import com.main.auc.payload.response.JwtResponse;
import com.main.auc.repsitory.RoleRepository;
import com.main.auc.repsitory.UserRepository;
import com.main.auc.security.jwt.JwtUtils;
import com.main.auc.security.services.UserDetailsImpl;
import com.main.auc.utils.Constants;
import com.main.auc.utils.FacebookUtils;
import com.main.auc.utils.GoogleUtils;
import com.main.auc.utils.SendMailUtils;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private GoogleUtils googleUtils;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    Gson gson;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    private FacebookUtils facebookUtils;

    @Autowired
    private SendMailUtils sendMail;


    public ResponseEntity<?> loginGoogle(GoogleAuthClientRq rq){
        try {
            String accessToken = googleUtils.getToken(rq.getCode());
            log.info("accessToken: " + accessToken);
            if("ERROR".equals(accessToken)){
                log.info("get accessToken fail");
                BaseClientErrorRp rp = BaseClientErrorRp.builder()
                        .code("TOKEN-01")
                        .desc("accessToken fail")
                        .build();
                return ResponseEntity.badRequest().body(rp);
            }
//            String accessToken = "ya29.a0ARrdaM-OrvHDKAvDc_Q-nW09KAP243jcC8m48mqqHAnBC_CajkEkeOj5AOrWP9Zs_BOsY4X8t6gjwy6cYJPUfwqXdqh6MyEu8fC9Nzho77_4ZkilUzH2E0RsRt-5IjFPvAG_kYxGWXwbKJ1JLY1vYuQHYBaB";
            GoogleAuthUserInfoData userInfoData = googleUtils.getUserInfo(accessToken);
            UserDetailsImpl userDetails = googleUtils.buildUser(userInfoData);
            // neu username email ko co thi insert DB
            String loginFirst = "0";
            if (!userRepository.existsByEmail(userDetails.getEmail())) {
                User user = User.builder()
                        .username(userDetails.getEmail())
                        .email(userDetails.getEmail())
                        .build();

                Set<Role> roles = new HashSet<>();
                Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
                user.setRoles(roles);
                loginFirst = "1";
                userRepository.save(user);
            }

            User userInfo = userRepository.findByUsername(userDetails.getEmail()).orElseThrow(() -> new RuntimeException("User null"));

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetailsImpl userDetails_ = (UserDetailsImpl) authentication.getPrincipal();
            log.info("userDetails: " + gson.toJson(userDetails_));
            List<String> roles = userDetails_.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());
            String jwt = jwtUtils.generateJwtToken(authentication);
            JwtResponse rp = JwtResponse.builder()
                    .id(userInfo.getId())
                    .token(jwt)
                    .username(userDetails.getEmail())
                    .email(userDetails.getEmail())
                    .roles(roles)
                    .loginFirst(loginFirst)
                    .build();
            return ResponseEntity.ok(rp);
        }catch (Exception e){
            log.info("login google exception: " + e.toString());
            return ResponseEntity.badRequest().body("Exception");
        }
    }

    public ResponseEntity<?> loginFacebook(FbAuthClientRq rq){
        try {
            String token = facebookUtils.getToken(rq.getCode());
//            String token = "EAAlTXxMZACJkBAOQorKDigtW2yxKuxEEJZCTPOzP6Tr7Th2ybHn6j1gH6Tl5AcWRfS09sHZBZB8fKnkZCwqsDnLhMQdkoymDbmNTIzseggIs6r7Tu43iuQxibKrKE7CzOxXq5WGCZChcemhmahkcaHQGUWZAKRtqJ8zQytZBZAoEvcdFfvnGwDrPsHqIf28D9BPWinQIicu4PmxEnLSKkYAjPa57dAYh5R9YNU4IZB57EyFwZDZD";
            if("ERROR".equals(token)){
                log.info("get accessToken fail");
                BaseClientErrorRp rp = BaseClientErrorRp.builder()
                        .code("TOKEN-01")
                        .desc("accessToken fail")
                        .build();
                return ResponseEntity.badRequest().body(rp);
            }
            String userInfo = facebookUtils.getUserInfo(token);
            log.info("userinfo: " + userInfo);
            FacebookAuthData fbData = gson.fromJson(userInfo, FacebookAuthData.class);
            UserDetailsImpl userDetails = facebookUtils.buildUser(fbData);
            // search id and username in DB
            Optional<User> userOptional = userRepository.findByUsernameAndIdAuth(fbData.getName(), fbData.getId());
            String loginFirst = "0";
            if(!userOptional.isPresent()){
                // save to DB
                User user = User.builder()
                        .username(fbData.getName())
                        .loginType(Constants.Login.LOGIN_TYPE_FB)
                        .idAuth(fbData.getId())
                        .build();
                userRepository.save(user);
                Set<Role> roles = new HashSet<>();
                Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
                user.setRoles(roles);
                loginFirst = "1";
            }
            User user = userRepository.findByUsernameAndIdAuth(fbData.getName(), fbData.getId()).orElseThrow(() -> new RuntimeException("User null"));

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            UserDetailsImpl userDetails_ = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails_.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());
            JwtResponse rp = JwtResponse.builder()
                    .id(user.getId())
                    .token(jwt)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(roles)
                    .loginFirst(loginFirst)
                    .build();

            return ResponseEntity.ok().body(rp);
        }catch (Exception e){
            log.info("Login facebook exception: " + e.toString());
            BaseClientErrorRp rp = BaseClientErrorRp.builder()
                    .code("96")
                    .desc("Exception")
                    .build();
            return ResponseEntity.badRequest().body(rp);
        }
    }


    public ResponseEntity<?> getUser(String token){
        try {
            String username = jwtUtils.parseUser(token.substring(7, token.length()));
            log.info("username: " + username);
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (!userOpt.isPresent()){
                log.info("user not found");
                return ResponseEntity.badRequest().body(BaseClientErrorRp.builder()
                        .code("USER-01").desc("user not found").build());
            }
            User user = userOpt.get();

            log.info("user: " + gson.toJson(user));
            List<Role> lstRoles = new ArrayList<>(user.getRoles());
            List<ERole> roles = lstRoles.stream()
                    .map(item -> item.getName())
                    .collect(Collectors.toList());
            UserDto rp = UserDto.builder()
                    .id(user.getId())
                    .username(user.getUsername()).email(user.getEmail())
                    .fullName(user.getFullName()).accType(user.getAccType())
                    .phoneNumber(user.getPhoneNumber()).address(user.getAddress())
                    .dob(user.getDob()).sex(user.getSex()).idNumber(user.getIdNumber())
                    .dateIssued(user.getDateIssued()).placeIssue(user.getPlaceIssue())
                    .frontIdNumber(user.getFrontIdNumber()).backIdNumber(user.getBackIdNumber())
                    .accountNo(user.getAccountNo()).bankName(user.getBankName())
                    .accountName(user.getAccountName())
                    .roles(roles)
                    .build();
            return ResponseEntity.ok(rp);
        }catch (Exception e){
            log.info("get info user exception: " + e.toString());
            BaseClientErrorRp rp = BaseClientErrorRp.builder()
                    .code(Constants.Base.EXCEPTION)
                    .desc("Exception")
                    .build();
            return ResponseEntity.badRequest().body(rp);
        }
    }

    public ResponseEntity<?> verifyUserSignUp(VerifySignUpClientRq rq){
        try{
            Optional<User> userOpt = userRepository.findByUsername(rq.getEmail());
            if(!userOpt.isPresent()){
                log.info("User null");
                BaseClientErrorRp rp = BaseClientErrorRp.builder()
                        .desc("user not found")
                        .code("VERIFY-SIGN-03")
                        .build();
                return ResponseEntity.badRequest().body(rp);
            }
            User user = userOpt.get();
            if(rq.getCode().equals(user.getCodeRegis())){
                if(Constants.Login.SIGNUP_CONFIRM.equals(user.getStatus())){
                    log.info("email verified");
                    BaseClientErrorRp rp = BaseClientErrorRp.builder()
                            .desc("email verified")
                            .code("VERIFY-SIGN-01")
                            .build();
                    return ResponseEntity.badRequest().body(rp);
                }
                user.setStatus(Constants.Login.SIGNUP_CONFIRM);
                userRepository.save(user);
                BaseClientErrorRp rp = BaseClientErrorRp.builder()
                        .desc("verify user success")
                        .code(Constants.Base.SUCCESS)
                        .build();
                return ResponseEntity.ok().body(rp);
            }else{
                BaseClientErrorRp rp = BaseClientErrorRp.builder()
                        .desc("Verify user fail, token fail")
                        .code("VERIFY-02")
                        .build();
                return ResponseEntity.badRequest().body(rp);
            }
        }catch (Exception e){
            BaseClientErrorRp rp = BaseClientErrorRp.builder()
                    .code(Constants.Base.EXCEPTION)
                    .desc("Exception")
                    .build();
            return ResponseEntity.badRequest().body(rp);
        }

    }

    public void sendVerificationEmail(User user, String siteURL)
            throws MessagingException, UnsupportedEncodingException, javax.mail.MessagingException {
        String toAddress = user.getEmail();
        String fromAddress = "dinhthangms96@gmail.com";
        String senderName = "Auction VVI";
        String subject = "Please verify your registration";
        String content = "Dear [[name]],<br>"
                + "Please click the link below to verify your registration:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>"
                + "Thank you,<br>"
                + ".";

//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message);
//
//        helper.setFrom(fromAddress, senderName);
//        helper.setTo(toAddress);
//        helper.setSubject(subject);

        content = content.replace("[[name]]", user.getEmail());
        String verifyURL = siteURL + "/verify?code=" + user.getCodeRegis() + "&username=" + user.getEmail();

        content = content.replace("[[URL]]", verifyURL);


        sendMail.sendSimpleEmail(subject, content, user.getEmail());

    }


    @Transactional
    public ResponseEntity<?> updateUser(MultipartFile imgFront, MultipartFile imgBack, String userJson){
        try {
            log.info("user: " + userJson);

            UserUpdateClientRq userDto = gson.fromJson(userJson, UserUpdateClientRq.class);
            Optional<User> userOpt = userRepository.findById(userDto.getId());
            if(!userOpt.isPresent()){
                log.info("update user null");
                BaseClientErrorRp rp = BaseClientErrorRp.builder()
                        .code("UPDATE-USER-01")
                        .desc("null user")
                        .build();
                return ResponseEntity.badRequest().body(rp);
            }
            User user = userOpt.get();

            user.setEmail(userDto.getEmail() == null ? user.getEmail() : userDto.getEmail());
            user.setUsername(userDto.getUsername() == null ? user.getUsername() : userDto.getUsername());
            user.setPhoneNumber(userDto.getPhoneNumber() == null ? user.getPhoneNumber() : userDto.getPhoneNumber());
            user.setAddress(userDto.getAddress() == null ?user.getAddress() : userDto.getAddress());
            user.setDob(userDto.getDob() == null ? user.getDob() : userDto.getDob());
            user.setSex(userDto.getSex() == null ? user.getSex() : userDto.getSex());
            user.setIdNumber(userDto.getIdNumber() == null ? user.getIdNumber() : userDto.getIdNumber());
            user.setDateIssued(userDto.getDateIssued() == null ? user.getDateIssued() : userDto.getDateIssued());
            user.setPlaceIssue(userDto.getPlaceIssue() == null ? user.getPlaceIssue() : userDto.getPlaceIssue());
            user.setAccountNo(userDto.getAccountNo() == null ? user.getAccountNo() : userDto.getAccountNo());
            user.setBankName(userDto.getBankName() == null ? user.getBankName() : userDto.getBankName());
            user.setBranchName(userDto.getBranchName() == null ? user.getBranchName() : userDto.getBranchName());
            user.setAccountName(userDto.getAccountName() == null ? user.getAccountName() : userDto.getBranchName());
            log.info(gson.toJson(user));
            if(!imgFront.isEmpty()){
                String dataImgFront = "data:image/png;base64," + Base64.getEncoder().encodeToString(imgFront.getBytes());
                user.setFrontIdNumber(dataImgFront);
            }

            if(!imgBack.isEmpty()){
                String dataImgBack = "data:image/png;base64," + Base64.getEncoder().encodeToString(imgFront.getBytes());
                user.setBackIdNumber(dataImgBack);
            }

//            log.info(gson.toJson(user));
            userRepository.save(user);

//            log.info(dataImgFront);
            return ResponseEntity.ok().body(
                    BaseClientErrorRp.builder().code("00").desc("Success").build()
            );
        }catch (Exception e){
            log.info("update user exception: " + e.toString());
            BaseClientErrorRp rp = BaseClientErrorRp.builder()
                    .code(Constants.Base.EXCEPTION)
                    .desc("Exception")
                    .build();
            return ResponseEntity.badRequest().body(rp);
        }
    }
}
