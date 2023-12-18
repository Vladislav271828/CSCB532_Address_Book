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
@Entity public class Label {
    @Id
    @GeneratedValue
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "colorRGB")
    private String colorRGB;

    @JsonIgnore//bez nego stava rekursiq :/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @JsonIgnore
    @OneToMany(mappedBy = "label", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<Contact> contacts;


}

