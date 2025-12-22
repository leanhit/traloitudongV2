package com.chatbot.userInfo.model;

import java.util.List;

import com.chatbot.address.model.Address;
import com.chatbot.auth.model.Auth;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_info")
@Getter @Setter
@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class UserInfo {
    @Id
    private Long id; // Sẽ dùng chung ID với Auth

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private Auth auth;

    private String fullName;
    private String phoneNumber;
    private String avatar;
    private String gender;
    
    @Column(name = "bio", length = 500)
    private String bio;

    @OneToMany(
        mappedBy = "user",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Address> addresses;
}