package CSCB532.Address_Book.contact;

import CSCB532.Address_Book.customRow.DtoCustomRow;
import CSCB532.Address_Book.label.DtoLabel;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @NotEmpty (message = "Phone Number can't be an empty string.")
    @NotNull(message = "Phone Number can't be null.")
    @Pattern(regexp = "[0-9]+", message = "Invalid phone number format")
    private String phoneNumber;
    private String nameOfCompany;
    private String address;
    private String email;
    private String fax;
    private String mobileNumber;
    private String comment;
    private List<DtoLabel> labels;
    private List<DtoCustomRow> customRows;


}
