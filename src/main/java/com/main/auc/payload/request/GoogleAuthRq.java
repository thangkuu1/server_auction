package com.main.auc.payload.request;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GoogleAuthRq {
    private String code;

    @SerializedName("client_id")
    private String clientId;

    @SerializedName("client_secret")
    private String clientSecret;

    @SerializedName("redirect_uri")
    private String redirectUri;

    @Builder.Default
    @SerializedName("grant_type")
    private String grantTye = "authorization_code";
}
