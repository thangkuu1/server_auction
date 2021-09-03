package com.main.auc.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifySignUpClientRq {
    private String username;
    private String code;
}
