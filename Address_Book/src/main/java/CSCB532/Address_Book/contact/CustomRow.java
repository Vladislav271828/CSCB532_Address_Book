package CSCB532.Address_Book.contact;

import CSCB532.Address_Book.user.User;
import jakarta.persistence.*;

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

    @ManyToOne
    @JoinColumn(name = "contact_id")
    private Contact contact;


}
