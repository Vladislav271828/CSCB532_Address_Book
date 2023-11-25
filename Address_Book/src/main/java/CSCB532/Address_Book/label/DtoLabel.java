package CSCB532.Address_Book.label;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DtoLabel {
    private Integer id;
    private String name;
    private Integer color;

}
