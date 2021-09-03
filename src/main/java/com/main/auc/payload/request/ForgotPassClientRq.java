package com.main.auc.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPassClientRq {
    private String email;
    private String passNew;
    private String code;
}
