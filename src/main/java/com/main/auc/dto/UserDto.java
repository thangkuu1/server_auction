package com.main.auc.dto;

import com.main.auc.models.ERole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserDto {

    private Long id;
    private String username;
    private String email;
    private List<ERole> roles;
}
