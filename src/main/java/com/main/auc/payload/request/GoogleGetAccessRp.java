package com.main.auc.payload.request;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleGetAccessRp {

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("expires_in")
    private String expiresIn;

    @SerializedName("error")
    private String error;

    @SerializedName("error_description")
    private String errorDesc;
}
