package CSCB532.Address_Book.label;

import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "The name of the label must not be null and must contain at least one character.")
    private String name;
    @NotBlank(message = "The color of the label must not be null and must contain a valid rgb patterned color representation.")
    private String colorRGB;

}
