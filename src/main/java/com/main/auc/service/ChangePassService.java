package com.main.auc.service;

import com.main.auc.models.User;
import com.main.auc.payload.request.ChangePassClientRq;
import com.main.auc.payload.response.BaseClientErrorRp;
import com.main.auc.repsitory.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class ChangePassService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;



    public ResponseEntity<?> changePass(ChangePassClientRq rq){

        try {
            Optional<User> userOpt = userRepository.findByUsername(rq.getEmail());
            if(!userOpt.isPresent()){
                log.info("not found user");
                BaseClientErrorRp rp = BaseClientErrorRp.builder()
                        .code("USER-01")
                        .desc("User not found")
                        .build();
                return ResponseEntity.badRequest().body(rp);
            }
            User user = userOpt.get();
//            String encodePassOld = passwordEncoder.encode(rq.getPassOld());
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if(!passwordEncoder.matches(rq.getPassOld(), user.getPassword())){
                BaseClientErrorRp rp = BaseClientErrorRp.builder()
                        .code("USER-02")
                        .desc("Password incorrect")
                        .build();
                return ResponseEntity.badRequest().body(rp);
            }
            user.setPassword(encoder.encode(rq.getPassNew()));
            userRepository.save(user);
            BaseClientErrorRp rp = BaseClientErrorRp.builder()
                    .code("00")
                    .desc("Change password success")
                    .build();
            return ResponseEntity.ok().body(rp);

        }catch (Exception e){
            log.info("change password exception: " + e.toString());
            BaseClientErrorRp rp = BaseClientErrorRp.builder()
                    .code("96")
                    .desc("Exception")
                    .build();
            return ResponseEntity.badRequest().body(rp);
        }

    }
}
