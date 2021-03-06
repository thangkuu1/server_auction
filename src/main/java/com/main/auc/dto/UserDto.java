package com.main.auc.dto;

import com.main.auc.models.ERole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import java.util.List;

@Getter
@Setter
@Builder
public class UserDto {

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

    private String frontIdNumber;

    private String backIdNumber;

    private String accountNo;

    private String bankName;

    private String branchName;

    private String accountName;
    private List<ERole> roles;
}
