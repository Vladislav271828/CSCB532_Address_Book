package CSCB532.Address_Book.contact;

import CSCB532.Address_Book.auth.AuthenticationService;
import CSCB532.Address_Book.user.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
//@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final AuthenticationService authenticationService;
    public ContactService(ContactRepository contactRepository, AuthenticationService authenticationService) {
        this.contactRepository = contactRepository;
        this.authenticationService = authenticationService;
    }

    public DtoContact createContact(DtoContact dtoContact) {
        Optional<DtoContact> optionalDtoContact = Optional.ofNullable(dtoContact);

        if (optionalDtoContact.isPresent()) {
            User user = authenticationService.getCurrentlyLoggedUser();
            ModelMapper modelMapper = new ModelMapper();

            Contact contact = modelMapper.map(dtoContact, Contact.class);
            contact.setUser(user);

            contactRepository.save(contact);
            Optional<Contact> optionalContact = contactRepository.findTopByUserIdOrderByContactIdDesc(user.getId());

            if (optionalContact.isPresent()) {
                return modelMapper.map(optionalContact.get(), DtoContact.class);
            }
        }
        return null; // or handle as needed if contact creation fails
    }

    public DtoContact updateContact(Integer contactId, DtoContact dtoContact) {
        Optional<Contact> optionalContact = contactRepository.findById(contactId);

        if (optionalContact.isPresent()) {
            Contact contact = optionalContact.get();

            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration().setSkipNullEnabled(true);

            // Map the updates from DtoContactUpdates to Contact
            modelMapper.map(dtoContact, contact);

            contactRepository.save(contact);

            return modelMapper.map(contact, DtoContact.class);
        } else {
            return null; // Or handle as needed if contact is not found
        }
    }

    public List<DtoContact> getAllContactsForLoggedInUser() {
        User user = authenticationService.getCurrentlyLoggedUser();
        List<Contact> userContacts = contactRepository.findAllByUserId(user.getId());

        if (userContacts != null && !userContacts.isEmpty()) {
            ModelMapper modelMapper = new ModelMapper();
            return userContacts.stream()
                    .map(contact -> modelMapper.map(contact, DtoContact.class))
                    .collect(Collectors.toList());
        } else {
            return null; // Or handle as needed if no contacts found
        }
    }

    public String deleteContactById(Integer contactId) {
        Optional<Contact> contact = contactRepository.findById(contactId);

        if (contact.isPresent()) {
            String name = contact.get().getName();
            String lastName = contact.get().getLastName();

            contactRepository.delete(contact.get());
            return "Contact " + name + " " + lastName + " deleted successfully";
        } else {
            return null; // Or handle as needed if contact is not found
        }
    }
}
