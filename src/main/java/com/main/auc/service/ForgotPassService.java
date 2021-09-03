package com.main.auc.service;

import com.main.auc.models.User;
import com.main.auc.payload.request.BaseClientRq;
import com.main.auc.payload.request.ForgotPassClientRq;
import com.main.auc.payload.response.BaseClientErrorRp;
import com.main.auc.repsitory.UserRepository;
import com.main.auc.utils.Constants;
import com.main.auc.utils.SendMailUtils;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class ForgotPassService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    SendMailUtils sendMailUtils;

    @Autowired
    PasswordEncoder encoder;

    public ResponseEntity<?> forgotPass(BaseClientRq rq){
        try{

            Optional<User> userOptional = userRepository.findByUsernameAndLoginType(rq.getEmail(), Constants.Login.LOGIN_TYPE_SYS);
            if(!userOptional.isPresent()){
                log.info("not found user ");
                BaseClientErrorRp rp = BaseClientErrorRp.builder()
                        .code("01")
                        .desc("not found user")
                        .build();
                return ResponseEntity.badRequest().body(rp);

            }
            String codeForgot = RandomString.make(20);
            User user = userOptional.get();
            user.setCodeForgot(codeForgot);
            userRepository.save(user);
            CompletableFuture.runAsync(() -> {
                try {
                    log.info("begin send Email");
                    sendMailUtils.sendSimpleEmailForgotPass(rq.getEmail(), codeForgot);
                    log.info("send email success");
                } catch (MessagingException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            });

            BaseClientErrorRp rp = BaseClientErrorRp.builder()
                    .code("00")
                    .desc("Success")
                    .build();
            return ResponseEntity.ok().body(rp);
        }catch (Exception e){
            log.info("forgot password exception: " + e.toString());
            BaseClientErrorRp rp = BaseClientErrorRp.builder()
                    .code("96")
                    .desc("Exception")
                    .build();
            return ResponseEntity.badRequest().body(rp);
        }
    }

    public ResponseEntity<?> verifyForgotPass(ForgotPassClientRq rq){
        try {
            Optional<User> userOptional = userRepository.findByUsernameAndLoginType(rq.getEmail(), Constants.Login.LOGIN_TYPE_SYS);
            if(!userOptional.isPresent()){
                log.info("not found user ");
                BaseClientErrorRp rp = BaseClientErrorRp.builder()
                        .code("01")
                        .desc("not found user")
                        .build();
                return ResponseEntity.badRequest().body(rp);

            }
            User user = userOptional.get();
            if(!rq.getCode().equals(user.getCodeForgot())){
                log.info("verify code fail");
                BaseClientErrorRp rp = BaseClientErrorRp.builder()
                        .code("02")
                        .desc("verify code forgot pass fail")
                        .build();
                return ResponseEntity.badRequest().body(rp);
            }
            user.setPassword(encoder.encode(rq.getPassNew()));
            user.setCodeForgot("");
            userRepository.save(user);
            BaseClientErrorRp rp = BaseClientErrorRp.builder()
                    .code("00")
                    .desc("forgot password success")
                    .build();
            return ResponseEntity.ok().body(rp);
        }catch (Exception e){
            log.info("forgot password verify exception: " + e.toString());
            BaseClientErrorRp rp = BaseClientErrorRp.builder()
                    .code("96")
                    .desc("Exception")
                    .build();
            return ResponseEntity.badRequest().body(rp);
        }
    }
}
