package com.main.auc.bo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class FacebookError {
    private String message;
    private long code = 0;
    private String type;

}
