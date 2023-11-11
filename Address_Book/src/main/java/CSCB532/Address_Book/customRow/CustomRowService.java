package CSCB532.Address_Book.customRow;

import CSCB532.Address_Book.auth.AuthenticationService;
import CSCB532.Address_Book.contact.Contact;
import CSCB532.Address_Book.contact.ContactRepository;
import CSCB532.Address_Book.exception.BadRequestException;
import CSCB532.Address_Book.exception.MissingContactException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static CSCB532.Address_Book.util.DtoValidationUtil.areAllFieldsNull;

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

    public DtoCustomRow createCustomRow(Integer contactId, DtoCustomRow dtoCustomRow) {
        // Validate contactId
        if (contactId == null || contactRepository.findById(contactId).isEmpty()) {
            throw new MissingContactException("Contact with ID " + contactId + " not found.");
        }
        if (areAllFieldsNull(dtoCustomRow)){
            throw new BadRequestException("Missing input.");
        }
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new MissingContactException("Contact with ID " + contactId + " not found."));
            ModelMapper modelMapper = new ModelMapper();
            CustomRow customRow = modelMapper.map(dtoCustomRow, CustomRow.class);
            customRow.setContact(contact);
            CustomRow createdCustomRow = customRowRepository.save(customRow);
            return modelMapper.map(createdCustomRow, DtoCustomRow.class);
    }

    public DtoCustomRow updateCustomRow(Integer id, DtoCustomRow updatedDtoCustomRow) {
        if (areAllFieldsNull(updatedDtoCustomRow)){
            throw new BadRequestException("Missing input.");
        }
        CustomRow existingCustomRow = customRowRepository.findById(id).orElse(null);
        if (existingCustomRow == null) throw new MissingContactException("Custom row with ID " + id + " not found.");
        ModelMapper modelMapper = new ModelMapper();
        updatedDtoCustomRow.setId(id);

        modelMapper.map(updatedDtoCustomRow, existingCustomRow);
        CustomRow updatedCustomRow = customRowRepository.save(existingCustomRow);
        return modelMapper.map(updatedCustomRow, DtoCustomRow.class);

    }

    public CustomRow getCustomRowById(Integer id) {
        return customRowRepository.findById(id).orElse(null);
    }

    public List<DtoCustomRow> getCustomRowsByContactId(Integer contactId) {
        if (contactId == null || contactRepository.findById(contactId).isEmpty()) {
            throw new MissingContactException("Contact with ID " + contactId + " not found.");
        }
        List<CustomRow> customRows = customRowRepository.findByContactId(contactId);
        ModelMapper modelMapper = new ModelMapper();

        return customRows.stream()
                .map(customRow -> modelMapper.map(customRow, DtoCustomRow.class))
                .collect(Collectors.toList());
    }

    public List<DtoCustomRow> deleteCustomRowsByContactId(Integer contactId) {
        List<CustomRow> customRows = customRowRepository.findByContactId(contactId);
        ModelMapper modelMapper = new ModelMapper();

        return customRows.stream()
                .map(customRow -> {
                    DtoCustomRow dtoCustomRow = modelMapper.map(customRow, DtoCustomRow.class);
                    customRowRepository.delete(customRow);
                    return dtoCustomRow;
                })
                .collect(Collectors.toList());
    }

    public DtoCustomRow deleteCustomRowById(Integer id) {
        CustomRow existingCustomRow = customRowRepository.findById(id).orElse(null);
        if (existingCustomRow == null) throw new MissingContactException("Custom row with ID " + id + " not found.");
        ModelMapper modelMapper = new ModelMapper();

        DtoCustomRow deletedDtoCustomRow = modelMapper.map(existingCustomRow, DtoCustomRow.class);
        customRowRepository.delete(existingCustomRow);
        return deletedDtoCustomRow;

    }
}
