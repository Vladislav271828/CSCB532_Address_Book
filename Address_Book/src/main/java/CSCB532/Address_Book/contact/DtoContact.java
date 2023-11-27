package CSCB532.Address_Book.contact;

import CSCB532.Address_Book.customRow.DtoCustomRow;
import CSCB532.Address_Book.label.Label;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DtoContact {

    private Integer id;
    private String name;
    private String lastName;
    private String phoneNumber;
    private String nameOfCompany;
    private String address;
    private String email;
    private String fax;
    private String mobileNumber;
    private String comment;
    private Label label;
    private List<DtoCustomRow> customRows;


}
