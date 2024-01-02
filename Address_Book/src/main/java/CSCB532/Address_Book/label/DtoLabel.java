package CSCB532.Address_Book.label;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DtoLabel {
    private Integer id;
    private Integer priority;
    @NotBlank(message = "The name of the label must not be null and must contain at least one character.")
    private String name;
    @NotBlank(message = "The color of the label must not be null and must contain a valid rgb patterned color representation.")
    private String colorRGB;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DtoLabel label = (DtoLabel) obj;
        return Objects.equals(name, label.getName());
    }

}
