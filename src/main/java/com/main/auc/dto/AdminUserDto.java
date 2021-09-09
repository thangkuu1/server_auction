package com.main.auc.dto;

import com.main.auc.models.ERole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class AdminUserDto {

    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;

    private String address;
    private List<ERole> roles;
}
