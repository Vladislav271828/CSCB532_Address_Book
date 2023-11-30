package CSCB532.Address_Book.customRow;

import CSCB532.Address_Book.contact.Contact;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "custom_row")
public class CustomRow {
    @Id
    @GeneratedValue//(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "custom_name")
    private String customName;

    @Column(name = "custom_field")
    private String customField;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "contact_id")
    private Contact contact;


}
