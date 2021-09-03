package com.main.auc.utils;

import com.google.gson.Gson;
import com.main.auc.bo.GoogleAuthUserInfoData;
import com.main.auc.payload.request.GoogleAuthRq;
import com.main.auc.payload.request.GoogleGetAccessRp;
import com.main.auc.security.services.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class GoogleUtils {

    @Autowired
    private Gson gson;

    @Autowired
    private OkHttpClient okHttpClient;

    public String getToken(String code) throws IOException {
        GoogleAuthRq rqAuth = GoogleAuthRq.builder()
                .code(code).clientId(Constants.Login.GG_CLIENT_ID)
                .clientSecret(Constants.Login.GG_CLIENT_SECRET)
                .redirectUri(Constants.Login.GG_DIRECT_SIT)
                .build();
        log.info("rq: " + gson.toJson(rqAuth));
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), gson.toJson(rqAuth));
        Request request = (new Request.Builder()).header("Accept", "application/json")
                .header("Content-Type", "application/json").url("https://accounts.google.com/o/oauth2/token").post(body).build();
        Response response = okHttpClient.newCall(request).execute();
        String rs = response.body().string();
        log.info("rs: " + rs);
        GoogleGetAccessRp ggAccess = gson.fromJson(rs, GoogleGetAccessRp.class);
        if(!Strings.isEmpty(ggAccess.getError())){
            log.info("call api access token fail");
            return "ERROR";
        }
//            String accToken = "ya29.a0ARrdaM-CeKTGc7VjbaUJSGSuebBV0x-YoJJfaWjkK38lo3cYgrF52TwxPkYECFM8xdeeu894-ks-Cyj0jhea_af57ttoz-9E-e-C6-zewaZoY-PO5hQC42WbL_wBK424jlr1WUM6BbrkJIkjlbgo-dweCATh";
        return ggAccess.getAccessToken();
    }

    public GoogleAuthUserInfoData getUserInfo(final String accessToken) throws IOException {
        Request requestUserInfo = (new Request.Builder()).header("Accept", "application/json")
                .header("Content-Type", "application/json").url("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + accessToken).build();
        Response responseUserInfo = okHttpClient.newCall(requestUserInfo).execute();

        String userInfoData = responseUserInfo.body().string();
        log.info("userinfo: " + userInfoData);
        return gson.fromJson(userInfoData, GoogleAuthUserInfoData.class);
    }

    public UserDetailsImpl buildUser(GoogleAuthUserInfoData data){
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        User user = new User(data.getEmail(), "",
                true, true, true, true,  authorities);
        return new UserDetailsImpl(0l, data.getEmail(), data.getEmail(), "", authorities);
//        return userDetails;
    }

}
