package admin;

import CSCB532.Address_Book.contact.Contact;
import CSCB532.Address_Book.contact.ContactService;
import CSCB532.Address_Book.contact.DtoContact;
import CSCB532.Address_Book.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository repository;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final ContactService contactService;


}
