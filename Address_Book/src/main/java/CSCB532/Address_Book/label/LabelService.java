package CSCB532.Address_Book.label;

import CSCB532.Address_Book.auth.AuthenticationService;
import CSCB532.Address_Book.contact.Contact;
import CSCB532.Address_Book.contact.ContactRepository;
import CSCB532.Address_Book.contact.ContactService;
import CSCB532.Address_Book.exception.BadRequestException;
import CSCB532.Address_Book.exception.CustomRowNotFoundException;
import CSCB532.Address_Book.exception.DatabaseException;
import CSCB532.Address_Book.exception.LabelNotFoundException;
import CSCB532.Address_Book.user.User;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
@Lazy
public class LabelService {
    private final AuthenticationService authenticationService;
    private final LabelRepository labelRepository;
    private final ContactRepository contactRepository;
    private final ContactService contactService;

    public LabelService(AuthenticationService authenticationService, LabelRepository labelRepository, ContactRepository contactRepository, ContactService contactService) {
        this.authenticationService = authenticationService;
        this.labelRepository = labelRepository;
        this.contactRepository = contactRepository;
        this.contactService = contactService;
    }

    public DtoLabel createLabel(DtoLabel dtoLabel) {
        // Validate the RGB color format
        String color = dtoLabel.getColorRGB();
        String rgbPattern = "^(\\d{1,3})(?:,\\s*|\\s+)(\\d{1,3})(?:,\\s*|\\s+)(\\d{1,3})$";
        Pattern pattern = Pattern.compile(rgbPattern);
        Matcher matcher = pattern.matcher(color);

        if (!matcher.matches()) {
            throw new BadRequestException("Invalid RGB format");
        }

        // Ensure each RGB component is within the range 0-255
        for (int i = 1; i <= matcher.groupCount(); i++) {
            int value;
            try {
                value = Integer.parseInt(matcher.group(i));
            } catch (NumberFormatException e) {
                throw new BadRequestException("RGB component is not a valid integer");
            }
            if (value < 0 || value > 255) {
                throw new BadRequestException("RGB value out of range: " + value);
            }
        }


        // Retrieve currently logged user
        User user = authenticationService.getCurrentlyLoggedUser();

        boolean isNameAlreadyInUseByLoggedUser = labelRepository.existsByNameAndUserId(dtoLabel.getName(), user.getId());

        if (isNameAlreadyInUseByLoggedUser){
            throw new BadRequestException("The label name " + dtoLabel.getName() + " is already in use.");
        }

        // Map DTO to entity
        ModelMapper modelMapper = new ModelMapper();
        Label label = modelMapper.map(dtoLabel, Label.class);
        label.setUser(user);
        label.setId(null);
        label.setContacts(new ArrayList<>());

        // Save the label
        try {
            label = labelRepository.save(label);
        } catch (DataAccessException exc) {
            throw new DatabaseException("Database connectivity issue: " + exc.getMessage(), exc);
        }
        // return the saved label as dtoLabel
        try {
            return modelMapper.map(label, DtoLabel.class);
        } catch (DataAccessException exc) {
            throw new DatabaseException("Issue with mapping the label data: " + exc.getMessage(), exc);
        }
    }

    public DtoLabel updateLabel(Integer labelId, DtoLabel dtoLabel) {
        if (dtoLabel.getId()!=null){
            dtoLabel.setId(null);
        }

        if (dtoLabel.getName() == null && dtoLabel.getColorRGB() == null){
            throw new BadRequestException("Incorrect request body");
        }

        // Validate input
        Label existingLabel = labelRepository.findById(labelId)
                .orElseThrow(() -> new CustomRowNotFoundException("Label not found for ID: " + labelId));

        boolean isNameBlank = false;
        if (dtoLabel.getName() != null){
            if (dtoLabel.getName().isBlank()){
                isNameBlank = true;
//                throw new BadRequestException("Field Name can't be blank.");
            }
            if (existingLabel.getName().equals(dtoLabel.getName())){
                dtoLabel.setName(null);
//                throw new BadRequestException("Field Name can't be the same.");
            }else if(isNameBlank){
                dtoLabel.setName(null);
            }
        }

        boolean isColorRgbBlank = false;
        if ( dtoLabel.getColorRGB() != null){
            if (dtoLabel.getColorRGB().isBlank()){
                isColorRgbBlank = true;
//                throw new BadRequestException("Custom Name can't be empty.");
            }
            if (existingLabel.getColorRGB().equals(dtoLabel.getColorRGB())){
                dtoLabel.setColorRGB(null);
//                    throw new BadRequestException("Custom Name can't be the same.");
            }else if(isColorRgbBlank){
                dtoLabel.setColorRGB(null);
            }
        }


        //checks if the currently logged user is attempting to update a label that's not theirs
        validateUserPermission(labelId);
        User user = authenticationService.getCurrentlyLoggedUser();
        boolean isNameAlreadyInUseByLoggedUser = labelRepository.existsByNameAndUserId(dtoLabel.getName(), user.getId());

        if (isNameAlreadyInUseByLoggedUser){
            throw new BadRequestException("The label name " + dtoLabel.getName() + " is already in use.");
        }

        // Find the existing label
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new LabelNotFoundException("Label with ID " + labelId + " not found."));

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setSkipNullEnabled(true);

        // Map the updates from DtoLabelUpdates to Label
        modelMapper.map(dtoLabel, label);

        // Save the updated label
        label = labelRepository.save(label);

        // Map entity back to DTO and return
        try {
            return modelMapper.map(label, DtoLabel.class);
        } catch (DataAccessException exc) {
            throw new DatabaseException("Issue with mapping the label data: " + exc.getMessage(), exc);
        }
    }

    public List<DtoLabel> getAllLabelsForLoggedInUser() {
        User user = authenticationService.getCurrentlyLoggedUser();
        List<Label> userLabels = labelRepository.findAllByUserId(user.getId());

        ModelMapper modelMapper = new ModelMapper();

        return userLabels.stream()
                .map(label -> modelMapper.map(label, DtoLabel.class))
                .collect(Collectors.toList());
    }

    public void deleteLabelById(Integer labelId) {
        // Validate input
        if (labelId == null || labelId < 0) {
            throw new BadRequestException("Label ID must be a positive integer.");
        }

        //remove the label from existing contacts
        List<Contact> contacts = contactRepository.findAllWithLabelId(labelId);
        contacts.forEach(contact -> contactService.removeLabelFromContact(contact.getId(), labelId));


        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new LabelNotFoundException("No label with id " + labelId + " found."));

        //checks if the currently logged user is attempting to update a label that's not theirs
        validateUserPermission(labelId);

        try {
            labelRepository.delete(label);
        } catch (DataAccessException e) {
            throw new DatabaseException("Couldn't delete label with id " + labelId, e.getCause());
        }

    }

    private void validateUserPermission(Integer labelId) {
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new LabelNotFoundException("Label with ID " + labelId + " not found."));
        User currentUser = authenticationService.getCurrentlyLoggedUser();
        if (!Objects.equals(currentUser.getId(), label.getUser().getId())) {
            throw new BadRequestException("User doesn't have permissions to perform this action.");
        }
    }


}
