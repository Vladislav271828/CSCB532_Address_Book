package CSCB532.Address_Book.label;

import CSCB532.Address_Book.contact.Contact;
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
public class Label {
    @Id
    @GeneratedValue
    private Integer id;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "name")
    private String name;

    @Column(name = "colorRGB")
    private String colorRGB;

    @JsonIgnore//bez nego stava rekursiq :/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(name = "label_contact",
            joinColumns = @JoinColumn(name = "label_id"),
            inverseJoinColumns = @JoinColumn(name = "contact_id"))
    private List<Contact> contacts;


}

