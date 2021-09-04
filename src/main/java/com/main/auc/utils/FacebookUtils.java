package com.main.auc.utils;

import com.google.gson.Gson;
import com.main.auc.bo.FacebookAuthAccess;
import com.main.auc.bo.FacebookAuthData;
import com.main.auc.bo.FacebookError;
import com.main.auc.bo.GoogleAuthUserInfoData;
import com.main.auc.security.services.UserDetailsImpl;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class FacebookUtils {

    @Autowired
    Gson gson;

    @Autowired
    OkHttpClient okHttpClient;

    public String getToken(String code) throws IOException {
        try{
            Request requestUserInfo = (new Request.Builder()).header("Accept", "application/json")
                    .header("Content-Type", "application/json").url("https://graph.facebook.com/oauth/access_token?client_id=" + Constants.Login.FB_CLIENT_ID
                            + "&client_secret=" + Constants.Login.FB_CLIENT_SECRET + "&redirect_uri=" + Constants.Login.FB_DIRECT_SIT + "&code=" + code
                    ).build();
            Response responseUserInfo = okHttpClient.newCall(requestUserInfo).execute();
            String rp = responseUserInfo.body().string();
            log.info("rp access token: " + rp);
            FacebookAuthAccess accObj = gson.fromJson(rp, FacebookAuthAccess.class);


            return accObj.getAccessToken();
        }catch (Exception e){
            log.info("get token facebook exception: " + e.toString());
            return "ERROR";
        }

    }

    public String getUserInfo(String accessToken){
        FacebookClient facebookClient = new DefaultFacebookClient(accessToken, Constants.Login.FB_CLIENT_SECRET,
                Version.LATEST);

        return facebookClient.fetchObject("me", String.class);
    }

    public UserDetailsImpl buildUser(FacebookAuthData data){
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        org.springframework.security.core.userdetails.User user = new User(data.getName(), "",
                true, true, true, true,  authorities);
        return new UserDetailsImpl(0l, data.getName(), data.getId(), "", authorities);
//        return userDetails;
    }


}
