package com.chatbot.address.model;

import com.chatbot.userInfo.model.UserInfo;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter @Setter
@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false)
    private OwnerType ownerType;

    private String houseNumber; // Số nhà
    private String street;      // Tên đường
    private String ward;        // Phường/Xã
    private String district;    // Quận/Huyện
    private String city;        // Tỉnh/Thành phố
    private String province;    // Bang/Vùng (nếu có)
    private String country;     // Quốc gia

    @Builder.Default
    @Column(name = "is_default")
    private boolean isDefault = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserInfo user;
}