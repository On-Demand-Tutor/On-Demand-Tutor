package com.example.user_service.entity;

//class này lưu các token đã logout vào database
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.util.Date;

@Builder
@AllArgsConstructor
@Entity
@Getter
@Setter
@NoArgsConstructor
public class InvalidatedToken {
    @Id
    private String id;
    private Date expiryTime;// dùng xóa sau thời gian nào đó
}
