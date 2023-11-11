package CSCB532.Address_Book.customRow;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomRowRepository extends JpaRepository<CustomRow, Integer> {

    List<CustomRow> findByContactId(Integer contactId);

}
