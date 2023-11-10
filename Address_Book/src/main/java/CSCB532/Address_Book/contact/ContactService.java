package CSCB532.Address_Book.contact;

import CSCB532.Address_Book.auth.AuthenticationService;
import CSCB532.Address_Book.user.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
//@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final AuthenticationService authenticationService;
    public ContactService(ContactRepository contactRepository, AuthenticationService authenticationService) {
        this.contactRepository = contactRepository;
        this.authenticationService = authenticationService;
    }

    public DtoContact createContact(DtoContact dtoContact)  {
        if (dtoContact.toString().isEmpty()) return null;
        User user = authenticationService.getCurrentlyLoggedUser();

        ModelMapper modelMapper = new ModelMapper();
        Contact contact = modelMapper.map(dtoContact, Contact.class);
        contact.setUser(user);
//        contact.setImportance(dtoContact.getImportance());
//        contact.setName(dtoContact.getName());
//        contact.setLastName(dtoContact.getLastName());
//        contact.setPhoneNumber(dtoContact.getPhoneNumber());
//        contact.setNameOfCompany(dtoContact.getNameOfCompany());
//        contact.setAddress(dtoContact.getAddress());
//        contact.setEmail(dtoContact.getEmail());
//        contact.setFax(dtoContact.getFax());
//        contact.setMobileNumber(dtoContact.g  etMobileNumber());
//        contact.setComment(dtoContact.getComment());
        contactRepository.save(contact);
        contact = contactRepository.findTopByUserIdOrderByContactIdDesc(user.getId()).get();

        dtoContact = modelMapper.map(contact, DtoContact.class);
//        dtoContact.setImportance(contact.getImportance());
//        dtoContact.setAddress(contact.getAddress());
//        dtoContact.setName(contact.getName());
//        dtoContact.setLastName(contact.getLastName());
//        dtoContact.setPhoneNumber(contact.getPhoneNumber());
//        dtoContact.setNameOfCompany(contact.getNameOfCompany());
//        dtoContact.setAddress(contact.getAddress());
//        dtoContact.setEmail(contact.getEmail());
//        dtoContact.setFax(contact.getFax());
//        dtoContact.setMobileNumber(contact.getMobileNumber());
//        dtoContact.setComment(contact.getComment());

        return dtoContact;

    }
}
