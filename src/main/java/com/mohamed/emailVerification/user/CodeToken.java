package com.mohamed.emailVerification.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeToken {
    @Id
    @GeneratedValue
    private Integer id;

    private String codeToken;
    private LocalDateTime createdAt;
    private LocalDateTime validatedAt;
    private LocalDateTime expireAt;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
