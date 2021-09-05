package com.main.auc.models;

import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.context.annotation.Configuration;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
@Entity
@Table(name = "user",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = "username"),
            @UniqueConstraint(columnNames = "email")
        })
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    private String username;

//    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @Size(max = 120)
    private String password;

    @Size(max = 50)
    @Column(name = "code_regis")
    private String codeRegis;

    @Column(name = "status")
    private String status;

    @Column(name = "login_type")
    private String loginType;

    @Column(name = "code_forgot")
    private String codeForgot;

    @Column(name = "id_auth")
    private String idAuth;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "acc_type")
    private String accType;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "dob")
    private String dob;

    @Column(name = "sex")
    private String sex;

    @Column(name = "id_number")
    private String idNumber;

    @Column(name = "date_issued")
    private String dateIssued;

    @Column(name = "place_issue")
    private String placeIssue;

    @Column(name = "front_idnumber")
    private String frontIdNumber;

    @Column(name = "back_idnumber")
    private String backIdNumber;

    @Column(name = "account_no")
    private String accountNo;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "login_first")
    private String loginFirst;

    @Builder.Default
    @Column(name = "create_date")
    private LocalDateTime createDate = LocalDateTime.now();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(	name = "role_user",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
}
