package CSCB532.Address_Book.customRow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DtoCustomRow {
    private Integer id;
    @Positive
    private Integer contactId;
    @NotBlank
    private String customName;
    @NotBlank
    private String customField;


}
