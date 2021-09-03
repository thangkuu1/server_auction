package com.main.auc.bo;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class GoogleAuthUserInfoData {

    @SerializedName("id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("verified_email")
    private String verifyEmail;

//    @SerializedName("name")
//    private String name;
//
//    @SerializedName("given_name")
//    private String givenName;
//
//    @SerializedName("family_name")
//    private String familyName;

    @SerializedName("picture")
    private String picture;

//    @SerializedName("locale")
//    private String locale;
}
