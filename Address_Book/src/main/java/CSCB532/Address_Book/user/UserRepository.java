package CSCB532.Address_Book.user;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    @Transactional
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Method to check if a user is verified
    @Query("SELECT u.verified FROM User u WHERE u.id = :userId")
    Boolean isUserVerified(Integer userId);

    // Custom query to delete verification codes and then the user
    @Modifying
    @Transactional
    @Query("DELETE FROM Verification v WHERE v.user.id = :userId")
    void deleteVerificationsByUserId(Integer userId);


}
