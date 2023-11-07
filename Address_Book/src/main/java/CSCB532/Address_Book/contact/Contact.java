package CSCB532.Address_Book.contact;

import CSCB532.Address_Book.user.User;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "contact")
public class Contact {
    @Id
    @GeneratedValue//(strategy = GenerationType.IDENTITY) nz koe e po dobro
    private Integer id;

    @Column(name = "imporance")
    private int importance;

    @Column(name = "name")
    private String name;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "name_of_company")
    private String nameOfCompany;

    @Column(name = "address")
    private String address;

    @Column(name = "email")
    private String email;

    @Column(name = "fax")
    private String fax;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "comment")
    private String comment;

    @ManyToOne
    @JoinColumn(name = "_user_id")
    private User user;

    @OneToMany(mappedBy = "contact")
    private List<CustomRow> customRows;


}
