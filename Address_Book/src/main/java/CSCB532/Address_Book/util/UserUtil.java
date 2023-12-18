package CSCB532.Address_Book.util;

import CSCB532.Address_Book.auth.AuthenticationService;
import CSCB532.Address_Book.user.User;
import lombok.experimental.UtilityClass;
import org.modelmapper.ModelMapper;

@UtilityClass
public class UserUtil {

    public User getCurrentlyLoggedUser(AuthenticationService authenticationService){
        return authenticationService.getCurrentlyLoggedUser();
    }
}
