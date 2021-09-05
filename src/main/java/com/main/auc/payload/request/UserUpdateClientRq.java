package com.main.auc.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateClientRq {
    private Long id;
    private String username;
    private String email;
    private String fullName;

    private String accType;

    private String phoneNumber;

    private String address;

    private String dob;

    private String sex;

    private String idNumber;

    private String dateIssued;

    private String placeIssue;

    private String accountNo;

    private String bankName;

    private String branchName;

    private String accountName;
}
