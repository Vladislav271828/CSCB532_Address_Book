package CSCB532.Address_Book.contact;

import CSCB532.Address_Book.customRow.CustomRow;
import CSCB532.Address_Book.label.Label;
import CSCB532.Address_Book.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contact")
public class Contact {
    @Id
    @GeneratedValue//(strategy = GenerationType.IDENTITY) nz koe e po dobro
    private Integer id;

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

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "label_id")
    private Label label;

    @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomRow> customRows;

    @Override
    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", nameOfCompany='" + nameOfCompany + '\'' +
                ", address='" + address + '\'' +
                ", email='" + email + '\'' +
                ", fax='" + fax + '\'' +
                ", mobileNumber='" + mobileNumber + '\'' +
                ", comment='" + comment + '\'' +
                //", user=" + user +
                ", customRows=" + customRows +
                '}';
    }
}
