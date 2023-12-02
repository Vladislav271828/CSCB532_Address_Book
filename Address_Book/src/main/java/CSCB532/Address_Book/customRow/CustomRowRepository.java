package CSCB532.Address_Book.customRow;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomRowRepository extends JpaRepository<CustomRow, Integer> {
    @Transactional
    Optional<CustomRow> findById(Integer userId);
    List<CustomRow> findByContactId(Integer contactId);

}
