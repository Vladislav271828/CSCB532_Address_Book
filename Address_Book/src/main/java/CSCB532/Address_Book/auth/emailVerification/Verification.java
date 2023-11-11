package CSCB532.Address_Book.auth.emailVerification;

import CSCB532.Address_Book.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "verification")
public class Verification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(unique = true)
    private String verificationCode;

    private boolean expired;

    private LocalDateTime expirationTime;

    // Set expiration time for the verification code
    @PrePersist
    protected void onCreate() {
        expirationTime = LocalDateTime.now().plusHours(24); // or whatever timeframe you see fit
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationTime);
    }
}
