package com.main.auc.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePassClientRq extends BaseClientRq{
    private String passOld;
    private String passNew;

}
