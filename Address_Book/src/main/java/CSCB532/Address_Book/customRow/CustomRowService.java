package CSCB532.Address_Book.customRow;

import CSCB532.Address_Book.auth.AuthenticationService;
import CSCB532.Address_Book.contact.Contact;
import CSCB532.Address_Book.contact.ContactRepository;
import CSCB532.Address_Book.exception.BadRequestException;
import CSCB532.Address_Book.exception.ContactNotFoundException;
import CSCB532.Address_Book.exception.CustomRowNotFoundException;
import CSCB532.Address_Book.exception.DatabaseException;
import CSCB532.Address_Book.user.User;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CustomRowService {
    private final CustomRowRepository customRowRepository;
    private final ContactRepository contactRepository;
    private final AuthenticationService authenticationService;

    public CustomRowService(CustomRowRepository customRowRepository, ContactRepository contactRepository, AuthenticationService authenticationService) {
        this.customRowRepository = customRowRepository;
        this.contactRepository = contactRepository;
        this.authenticationService = authenticationService;
    }

    public DtoCustomRow createCustomRow(DtoCustomRow dtoCustomRow) {
        validateContactExists(dtoCustomRow.getContactId());
        validateUserPermission(dtoCustomRow.getContactId());

        Contact contact = contactRepository.findById(dtoCustomRow.getContactId())
                .orElseThrow(() -> new ContactNotFoundException("Contact with ID " + dtoCustomRow.getContactId() + " not found."));

        ModelMapper modelMapper = new ModelMapper();


        CustomRow customRow = modelMapper.map(dtoCustomRow, CustomRow.class);
        customRow.setContact(contact);

        try {
            CustomRow createdCustomRow = customRowRepository.save(customRow);
            return modelMapper.map(createdCustomRow, DtoCustomRow.class);
        } catch (Exception exc) {
            throw new DatabaseException(exc.getMessage());
        }
    }


    public DtoCustomRow updateCustomRow(Integer id, DtoCustomRow dtoCustomRow) {

        CustomRow existingCustomRow = customRowRepository.findById(id)
                .orElseThrow(() -> new CustomRowNotFoundException("Custom row not found for ID: " + id));

        boolean isBlank = false;
        if (dtoCustomRow.getCustomField() != null) {
            if (dtoCustomRow.getCustomField().isBlank()) {
                isBlank = true;
//                throw new BadRequestException("Field Name can't be blank.");
            }
            if (existingCustomRow.getCustomField().equals(dtoCustomRow.getCustomField())) {
                dtoCustomRow.setCustomField(null);
//                throw new BadRequestException("Field Name can't be the same.");
            } else if (isBlank) {
                dtoCustomRow.setCustomField(null);
            }
        }

        boolean isNameBlank = false;
        if (dtoCustomRow.getCustomName() != null) {
            if (dtoCustomRow.getCustomName().isBlank()) {
                isNameBlank = true;
//                throw new BadRequestException("Custom Name can't be empty.");
            }
            if (existingCustomRow.getCustomName().equals(dtoCustomRow.getCustomName())) {
                dtoCustomRow.setCustomName(null);
//                    throw new BadRequestException("Custom Name can't be the same.");
            } else if (isNameBlank) {
                dtoCustomRow.setCustomName(null);
            }
        }

        if (dtoCustomRow.getCustomName() == null && dtoCustomRow.getCustomField() == null) {
            throw new BadRequestException("Incorrect request body");
        }


        validateUserPermission(existingCustomRow.getContact().getId());

        ModelMapper modelMapper = configureModelMapperForUpdate();
        modelMapper.map(dtoCustomRow, existingCustomRow);

        try {
            CustomRow updatedCustomRow = customRowRepository.save(existingCustomRow);
            return modelMapper.map(updatedCustomRow, DtoCustomRow.class);
        } catch (Exception exc) {
            throw new DatabaseException(exc.getMessage());
        }
    }

    public List<DtoCustomRow> getCustomRowsByContactId(Integer contactId) {
        if (contactId == null) {
            throw new BadRequestException("Contact ID is required.");
        }

        validateContactExists(contactId);
        validateUserPermission(contactId);

        List<CustomRow> customRows = customRowRepository.findByContactId(contactId);
        ModelMapper modelMapper = new ModelMapper();

        return customRows.stream()
                .map(customRow -> modelMapper.map(customRow, DtoCustomRow.class))
                .collect(Collectors.toList());
    }

    public void deleteCustomRowById(Integer customRowId) {
        if (customRowId == null) {
            throw new BadRequestException("Custom row ID is required.");
        }

        CustomRow customRow = customRowRepository.findById(customRowId)
                .orElseThrow(() -> new CustomRowNotFoundException("Custom row not found for ID: " + customRowId));

        validateUserPermission(customRow.getContact().getId());

        try {
            customRowRepository.delete(customRow);
        } catch (DataAccessException exc) {
            throw new DatabaseException(exc.getMessage());
        }
    }

    public void deleteCustomRowsByContactId(Integer contactId) {
        if (contactId == null) {
            throw new BadRequestException("Contact ID is required.");
        }

        validateContactExists(contactId);
        validateUserPermission(contactId);

        List<CustomRow> customRows = customRowRepository.findByContactId(contactId);
        if (customRows.isEmpty()) {
            throw new CustomRowNotFoundException("No Custom Rows found for contact with id " + contactId);
        }
        try {
            customRowRepository.deleteAll(customRows);
        } catch (DataAccessException exc) {
            throw new DatabaseException("Failed to delete custom rows due to database issue.");
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


    private void validateContactExists(Integer contactId) {
        if (contactRepository.findById(contactId).isEmpty()) {
            throw new ContactNotFoundException("Contact with ID " + contactId + " not found.");
        }
    }

    private ModelMapper configureModelMapperForUpdate() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.addMappings(new PropertyMap<DtoCustomRow, CustomRow>() {
            @Override
            protected void configure() {
                skip(destination.getId());
                skip(destination.getContact());
            }
        });
        return modelMapper;
    }
}
