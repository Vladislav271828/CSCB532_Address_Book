package CSCB532.Address_Book.contact;

import CSCB532.Address_Book.auth.AuthenticationService;
import CSCB532.Address_Book.exception.BadRequestException;
import CSCB532.Address_Book.exception.DatabaseException;
import CSCB532.Address_Book.exception.MissingContactException;
import CSCB532.Address_Book.user.User;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static CSCB532.Address_Book.util.DtoValidationUtil.areAllContactDtoFieldsNull;

@Service
//@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final AuthenticationService authenticationService;

    @Autowired
    public ContactService(ContactRepository contactRepository, AuthenticationService authenticationService) {
        this.contactRepository = contactRepository;
        this.authenticationService = authenticationService;
    }

    /**
     * Creates a new contact from the provided DTO.
     *
     * @param dtoContact the DTO containing contact information
     * @return the saved contact as a DTO
     * @throws BadRequestException if the provided data is invalid
     * @throws DatabaseException   if there is an issue with saving the contact or mapping the result
     */
    public DtoContact createContact(DtoContact dtoContact) {
        // Validation
        if (dtoContact.getPhoneNumber() == null || dtoContact.getPhoneNumber().trim().isEmpty()) {
            throw new BadRequestException("Invalid data provided (missing or empty phone number)");
        }

        // Retrieve currently logged user
        User user = authenticationService.getCurrentlyLoggedUser();

        // Map DTO to entity
        ModelMapper modelMapper = new ModelMapper();
        Contact contact = modelMapper.map(dtoContact, Contact.class);
        contact.setUser(user);

        // Save the contact
        try {
            contact = contactRepository.save(contact);
        } catch (Exception exc) {
            throw new DatabaseException("Database connectivity issue: " + exc.getMessage(), exc);
        }

        // Map entity back to DTO and return
        try {
            return modelMapper.map(contact, DtoContact.class);
        } catch (Exception exc) {
            throw new DatabaseException("Issue with mapping the contact data: " + exc.getMessage(), exc);
        }
    }

    /**
     * Updates an existing contact with new data from a DTO.
     *
     * @param contactId  the ID of the contact to update
     * @param dtoContact the DTO containing updated contact information
     * @return the updated contact as a DTO
     * @throws BadRequestException     if the provided data is invalid
     * @throws MissingContactException if no contact is found with the given ID
     */
    @Transactional
    public DtoContact updateContact(Integer contactId, DtoContact dtoContact) {
        // Validate input
        if (dtoContact == null || contactId == null) {
            throw new BadRequestException("Contact ID or update information cannot be null.");
        }

        if (areAllContactDtoFieldsNull(dtoContact)) {
            throw new BadRequestException("Missing input.");
        }

        // Find the existing contact
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new MissingContactException("Contact with ID " + contactId + " not found."));

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setSkipNullEnabled(true);

        // Map the updates from DtoContactUpdates to Contact
        modelMapper.map(dtoContact, contact);

        // Save the updated contact
        contact = contactRepository.save(contact);

        // Map entity back to DTO and return
        try {
            return modelMapper.map(contact, DtoContact.class);
        } catch (Exception exc) {
            throw new DatabaseException("Issue with mapping the contact data: " + exc.getMessage(), exc);
        }
    }

    /**
     * Retrieves all contacts for the currently logged-in user.
     * <p>
     * This method fetches the contacts associated with the currently authenticated user
     * from the database and maps them to a list of {@link DtoContact} objects.
     * If the user has no contacts, an empty list is returned.
     *
     * @return A {@link List} of {@link DtoContact} objects representing the user's contacts.
     * If the user has no contacts, the list will be empty, never {@code null}.
     */
    public List<DtoContact> getAllContactsForLoggedInUser() {
        User user = authenticationService.getCurrentlyLoggedUser();
        List<Contact> userContacts = contactRepository.findAllByUserId(user.getId());

        ModelMapper modelMapper = new ModelMapper();

        return userContacts.stream()
                .map(contact -> modelMapper.map(contact, DtoContact.class))
                .collect(Collectors.toList());
    }

    /**
     * Deletes a contact by its ID.
     * <p>
     * This method attempts to delete a contact with the specified ID from the database.
     * If the contact ID is invalid (null or negative), a {@link BadRequestException} is thrown.
     * If no contact with the specified ID is found, a {@link MissingContactException} is thrown.
     * If the contact is found and successfully deleted, a success message is returned.
     *
     * @param contactId The ID of the contact to be deleted.
     * @return A {@link String} message indicating successful deletion.
     * @throws BadRequestException     if the {@code contactId} is {@code null} or negative.
     * @throws MissingContactException if no contact with the specified ID is found.
     * @throws DatabaseException       if there is an issue with the database operation.
     */
    public String deleteContactById(Integer contactId) {
        // Validate input
        if (contactId == null || contactId < 0) {
            throw new BadRequestException("Contact ID must be a positive integer.");
        }

        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new MissingContactException("No contact with id " + contactId + " found."));

        try {
            contactRepository.delete(contact);
        } catch (Exception e) {
            throw new DatabaseException("Couldn't delete contact with id " + contactId, e.getCause());
        }

        return "Contact with id " + contact.getId() + " deleted successfully";
    }
}
