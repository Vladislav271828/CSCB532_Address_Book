package CSCB532.Address_Book.customRow;

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
    private String customName;
    private String customField;


}
