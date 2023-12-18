package CSCB532.Address_Book.auth.emailVerification;


import CSCB532.Address_Book.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.Optional;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, Long> {

    Optional<Verification> findByVerificationCode(String verificationCode);

    @Query("SELECT v FROM Verification v WHERE v.user.email = :email AND v.expired = false ORDER BY v.id DESC")
    Optional<Verification> findLatestByUserEmail(String email);
    @Transactional
    void deleteByUser(User userToDelete);
}
