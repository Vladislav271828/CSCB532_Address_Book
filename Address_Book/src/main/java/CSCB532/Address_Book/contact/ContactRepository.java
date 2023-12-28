package CSCB532.Address_Book.contact;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Integer>{
    @Transactional
    Optional<Contact> findById(Integer contactId);

    @Transactional
    List<Contact> findAllByUserId(Integer userId);

    @Transactional
    @Query("SELECT c FROM Contact c WHERE c.id = (SELECT MAX(cc.id) FROM Contact cc WHERE cc.user.id = :userId )")
    Optional<Contact> findTopByUserIdOrderByContactIdDesc(@Param("userId") Integer userId);

    @Query("SELECT c FROM Contact c " +
            "WHERE c.user.id = :userId " +
            "AND (:name IS NULL OR c.name = :name) " +
            "AND (:lastName IS NULL OR c.lastName = :lastName)")
    List<Contact> findAllByUserIdAndNameAndOrLastName(
            @Param("userId") Integer userId,
            @Param("name") String name,
            @Param("lastName") String lastName
    );

    @Query("SELECT c FROM Contact c " +
            "WHERE (:name IS NULL OR c.name = :name) " +
            "AND (:lastName IS NULL OR c.lastName = :lastName)")
    List<Contact> findAllByNameAndOrLastName(
            @Param("name") String name,
            @Param("lastName") String lastName
    );

//    @Query("SELECT c FROM Contact c " +
//            "WHERE c.label.id = :labelId ")
    @Query("SELECT c FROM Contact c JOIN c.labels l WHERE l.id = :labelId")
    List<Contact> findAllWithLabelId(
            @Param("labelId") Integer labelId
    );

//    @Query("SELECT c FROM Contact c " +
//            "JOIN c.label mostCommonLabel " +
//            "WHERE c.user.id = :userId " +
//            "AND mostCommonLabel.id = (" +
//            "   SELECT c2.label.id " +
//            "   FROM Contact c2 " +
//            "   WHERE c2.user.id = :userId " +
//            "   GROUP BY c2.label " +
//            "   ORDER BY COUNT(c2.label) DESC " +
//            "   LIMIT 1" +
//            ")")
//    List<Contact> findAllWithMostCommonLabelByUserId(
//            @Param("userId") Integer userId
//    );
//
//    @Query("SELECT c FROM Contact c " +
//            "JOIN c.label mostCommonLabel " +
//            "WHERE mostCommonLabel.id = (" +
//            "   SELECT c2.label.id " +
//            "   FROM Contact c2 " +
//            "   GROUP BY c2.label " +
//            "   ORDER BY COUNT(c2.label) DESC " +
//            "   LIMIT 1" +
//            ")")
//    List<Contact> findAllWithMostCommonLabel();
}
