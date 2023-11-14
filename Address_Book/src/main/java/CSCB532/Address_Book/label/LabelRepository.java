package CSCB532.Address_Book.label;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabelRepository extends JpaRepository<Label, Integer> {

    List<Label> findAllByUserId(Integer id);
}
