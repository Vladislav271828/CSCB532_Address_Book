package CSCB532.Address_Book.admin;

import CSCB532.Address_Book.contact.Contact;
import CSCB532.Address_Book.contact.ContactService;
import CSCB532.Address_Book.contact.DtoContact;
import CSCB532.Address_Book.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository repository;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final ContactService contactService;


    public List<DtoContact> getAllContactsAsAdmin() {

        List<Contact> userContacts = contactService.getAllContacts();

        ModelMapper modelMapper = new ModelMapper();

        return userContacts.stream()
                .map(contact -> modelMapper.map(contact, DtoContact.class))
                .collect(Collectors.toList());

    }
}
