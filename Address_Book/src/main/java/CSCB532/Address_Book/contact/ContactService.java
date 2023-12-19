package CSCB532.Address_Book.contact;

import CSCB532.Address_Book.auth.AuthenticationService;
import CSCB532.Address_Book.customRow.DtoCustomRow;
import CSCB532.Address_Book.exception.BadRequestException;
import CSCB532.Address_Book.exception.ContactNotFoundException;
import CSCB532.Address_Book.exception.DatabaseException;
import CSCB532.Address_Book.exception.LabelNotFoundException;
import CSCB532.Address_Book.label.Label;
import CSCB532.Address_Book.label.LabelRepository;
import CSCB532.Address_Book.user.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.server.ExportException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static CSCB532.Address_Book.util.DtoValidationUtil.areAllContactDtoFieldsNull;

@Service
//@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final AuthenticationService authenticationService;
    private final LabelRepository labelRepository;

    @Autowired
    public ContactService(ContactRepository contactRepository, AuthenticationService authenticationService, LabelRepository labelRepository) {
        this.contactRepository = contactRepository;
        this.authenticationService = authenticationService;
        this.labelRepository = labelRepository;
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

        // Retrieve currently logged user
        User user = authenticationService.getCurrentlyLoggedUser();

        // Map DTO to entity
        ModelMapper modelMapper = new ModelMapper();
        Contact contact = modelMapper.map(dtoContact, Contact.class);
        contact.setUser(user);

        // Save the contact
        try {
            contact = contactRepository.save(contact);
        } catch (DataAccessException exc) {
            throw new DatabaseException("Database connectivity issue: " + exc.getMessage(), exc);
        }

        // Map entity back to DTO and return
        try {
            return modelMapper.map(contact, DtoContact.class);
        } catch (DataAccessException exc) {
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
     * @throws ContactNotFoundException if no contact is found with the given ID
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

        //checks if the currently logged user is attempting to update a contact that's not theirs
        validateUserPermission(contactId);


        // Find the existing contact
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact with ID " + contactId + " not found."));

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setSkipNullEnabled(true);

        // Map the updates from DtoContactUpdates to Contact
        modelMapper.map(dtoContact, contact);

        // Save the updated contact
        contact = contactRepository.save(contact);

        // Map entity back to DTO and return
        try {
            return modelMapper.map(contact, DtoContact.class);
        } catch (DataAccessException exc) {
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
     * If no contact with the specified ID is found, a {@link ContactNotFoundException} is thrown.
     * If the contact is found and successfully deleted, a success message is returned.
     *
     * @param contactId The ID of the contact to be deleted.
     * @throws BadRequestException     if the {@code contactId} is {@code null} or negative.
     * @throws ContactNotFoundException if no contact with the specified ID is found.
     * @throws DatabaseException       if there is an issue with the database operation.
     */
    public void deleteContactById(Integer contactId) {
        // Validate input
        if (contactId == null || contactId < 0) {
            throw new BadRequestException("Contact ID must be a positive integer.");
        }

        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("No contact with id " + contactId + " found."));

        //checks if the currently logged user is attempting to update a contact that's not theirs
        validateUserPermission(contactId);

        try {
            contactRepository.delete(contact);
        } catch (DataAccessException e) {
            throw new DatabaseException("Couldn't delete contact with id " + contactId, e.getCause());
        }
    }

    private void validateUserPermission(Integer contactId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact with ID " + contactId + " not found."));
        User currentUser = authenticationService.getCurrentlyLoggedUser();
        if (!Objects.equals(currentUser.getId(), contact.getUser().getId())) {
            throw new BadRequestException("User doesn't have permissions to perform this action.");
        }
    }

    public List<DtoContact> searchContacts(DtoContact dtoContact) {
        User user = authenticationService.getCurrentlyLoggedUser();

        if ((dtoContact.getName() == null && dtoContact.getLastName() == null)) {
            throw new BadRequestException("Missing input.");
        }
        List<Contact> userContacts = contactRepository.findAllByUserIdAndNameAndOrLastName(user.getId(), dtoContact.getName(), dtoContact.getLastName());
        ModelMapper modelMapper = new ModelMapper();
        return userContacts.stream()
                .map(contact -> modelMapper.map(contact, DtoContact.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public DtoContact addLabelToContact(Integer contactId, Integer labelId) {
        //checks if the currently logged user is attempting to update a contact that's not theirs
        validateUserPermission(contactId);

        // Find the existing contact
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact with ID " + contactId + " not found."));
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new LabelNotFoundException("Label with ID " + labelId + " not found."));

        // checks if the currently logged user is attempting to use a label that's not theirs
        if (!authenticationService.getCurrentlyLoggedUser().getId().equals(label.getUser().getId())) {
            throw new BadRequestException("User doesn't have permissions to perform this action.");
        }

        // Save the updated contact
        contact.setLabel(label);
        label.getContacts().add(contact);
        contactRepository.save(contact);
        labelRepository.save(label);

        // Map entity back to DTO and return
        try {
            ModelMapper modelMapper = new ModelMapper();
            return modelMapper.map(contact, DtoContact.class);
        } catch (DataAccessException exc) {
            throw new DatabaseException("Issue with mapping the contact data: " + exc.getMessage(), exc);
        }
    }

    @Transactional
    public void RemoveLabelFromContact(Integer contactId) {
        //checks if the currently logged user is attempting to update a contact that's not theirs
        validateUserPermission(contactId);

        // Find the existing contact
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact with ID " + contactId + " not found."));
        if (contact.getLabel() == null) throw new LabelNotFoundException("Contact doesn't have a label to remove");

        Label label = contact.getLabel();

        // Save the updated contact
        contact.setLabel(null);
        label.getContacts().remove(contact);
        contactRepository.save(contact);
        labelRepository.save(label);
    }

    public String exportAllContactsToCSV() {
        List<DtoContact> allContacts = getAllContactsForLoggedInUser();

        // Convert contacts to CSV format
        StringBuilder csvContent = new StringBuilder();
            // Write the header
            csvContent.append("Name,Last Name,Phone Number,Name of Company,Address,Email,Fax,Mobile Number,Comment,Label,Custom Rows\n");

            // Write each contact to the CSV file
            for (DtoContact contact : allContacts) {
                csvContent.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        contact.getName(),
                        contact.getLastName(),
                        contact.getPhoneNumber(),
                        contact.getNameOfCompany(),
                        contact.getAddress(),
                        contact.getEmail(),
                        contact.getFax(),
                        contact.getMobileNumber(),
                        contact.getComment(),
                        contact.getLabel() != null ? contact.getLabel().getName() : "null",
                        getCustomRowsAsString(contact.getCustomRows())));
            }

            return csvContent.toString();

    }

    public String getCustomRowsAsString(List<DtoCustomRow> customRows) {
        StringBuilder customRowsString = new StringBuilder();

        if (customRows != null && !customRows.isEmpty()) {
            for (DtoCustomRow customRow : customRows) {
                customRowsString.append(customRow.getCustomName()).append(": ").append(customRow.getCustomField()).append(", ");
            }
            // Remove the trailing comma and space
            customRowsString.setLength(customRowsString.length() - 2);
        }

        return customRowsString.toString();
    }

    public String exportAllContactsToJSON() throws ExportException {
        List<DtoContact> allContacts = getAllContactsForLoggedInUser();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsString(allContacts);
        } catch (JsonProcessingException e) {
            throw new ExportException("Error exporting contacts to JSON: " + e.getMessage(), e);
        }
    }

    public byte[] exportAllContactsToExcel() throws IOException {
        List<DtoContact> allContacts = getAllContactsForLoggedInUser();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Contacts");

            // Write the header
            Row headerRow = sheet.createRow(0);
            String[] headerData = {"Name", "Last Name", "Phone Number", "Name of Company", "Address", "Email", "Fax",
                    "Mobile Number", "Comment", "Label", "Custom Rows"};
            for (int i = 0; i < headerData.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headerData[i]);
            }

            // Write each contact to the Excel file
            int rowNum = 1;
            for (DtoContact contact : allContacts) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(contact.getName());
                row.createCell(1).setCellValue(contact.getLastName());
                row.createCell(2).setCellValue(contact.getPhoneNumber());
                row.createCell(3).setCellValue(contact.getNameOfCompany());
                row.createCell(4).setCellValue(contact.getAddress());
                row.createCell(5).setCellValue(contact.getEmail());
                row.createCell(6).setCellValue(contact.getFax());
                row.createCell(7).setCellValue(contact.getMobileNumber());
                row.createCell(8).setCellValue(contact.getComment());

                Cell labelCell = row.createCell(9);
                labelCell.setCellValue(contact.getLabel() != null ? contact.getLabel().getName() : "null");

                Cell customRowsCell = row.createCell(10);
                customRowsCell.setCellValue(getCustomRowsAsString(contact.getCustomRows()));
            }

            // Save the workbook content to a ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public List<DtoContact> getContactsWithMostCommonLabelByUserId(){
        User user = authenticationService.getCurrentlyLoggedUser();
        List<Contact> userContacts = contactRepository.findAllWithMostCommonLabelByUserId(user.getId());

        ModelMapper modelMapper = new ModelMapper();

        return userContacts.stream()
                .map(contact -> modelMapper.map(contact, DtoContact.class))
                .collect(Collectors.toList());
    }

}
