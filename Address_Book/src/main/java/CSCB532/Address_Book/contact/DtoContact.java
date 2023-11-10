package CSCB532.Address_Book.contact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DtoContact {

    private Integer id;
    private int importance;
    private String name;
    private String lastName;
    private String phoneNumber;
    private String nameOfCompany;
    private String address;
    private String email;
    private String fax;
    private String mobileNumber;
    private String comment;
}
