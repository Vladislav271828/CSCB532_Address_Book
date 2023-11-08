package CSCB532.Address_Book.contact;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Integer>{
    @Transactional
    Optional<Contact> findByUserId(Integer userId);

    @Transactional
    @Query("SELECT c FROM Contact c WHERE c.id = (SELECT MAX(cc.id) FROM Contact cc WHERE cc.user.id = :userId )")
    Optional<Contact> findTopByUserIdOrderByContactIdDesc(@Param("userId") Integer userId);

}
