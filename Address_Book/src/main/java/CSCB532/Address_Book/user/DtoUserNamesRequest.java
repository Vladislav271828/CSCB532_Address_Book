package CSCB532.Address_Book.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DtoUserNamesRequest {
    private String firstName;
    private String lastName;
}
