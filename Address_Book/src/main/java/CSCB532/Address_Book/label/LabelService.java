package CSCB532.Address_Book.label;

import CSCB532.Address_Book.auth.AuthenticationService;
import CSCB532.Address_Book.exception.BadRequestException;
import CSCB532.Address_Book.exception.DatabaseException;
import CSCB532.Address_Book.exception.LabelNotFoundException;
import CSCB532.Address_Book.user.User;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
public class LabelService {
    private final AuthenticationService authenticationService;
    private final LabelRepository labelRepository;

    public LabelService(AuthenticationService authenticationService, LabelRepository labelRepository) {
        this.authenticationService = authenticationService;
        this.labelRepository = labelRepository;
    }

    public DtoLabel createLabel(DtoLabel dtoLabel) {
        // Validation
        if (dtoLabel.getName() == null || dtoLabel.getColor() == null) {
            throw new BadRequestException("Missing input.");
        }

        // Retrieve currently logged user
        User user = authenticationService.getCurrentlyLoggedUser();

        // Map DTO to entity
        ModelMapper modelMapper = new ModelMapper();
        Label label = modelMapper.map(dtoLabel, Label.class);
        label.setUser(user);

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
        // Validate input
        if (dtoLabel == null || labelId == null) {
            throw new BadRequestException("Label ID or update information cannot be null.");
        }

        if (dtoLabel.getName() == null && dtoLabel.getColor() == null) {
            throw new BadRequestException("Missing input.");
        }

        //checks if the currently logged user is attempting to update a label that's not theirs
        validateUserPermission(labelId);


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

    public String deleteLabelById(Integer labelId) {
        // Validate input
        if (labelId == null || labelId < 0) {
            throw new BadRequestException("Label ID must be a positive integer.");
        }

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new LabelNotFoundException("No label with id " + labelId + " found."));

        //checks if the currently logged user is attempting to update a label that's not theirs
        validateUserPermission(labelId);

        try {
            labelRepository.delete(label);
        } catch (DataAccessException e) {
            throw new DatabaseException("Couldn't delete label with id " + labelId, e.getCause());
        }

        return "Label with id " + label.getId() + " deleted successfully";
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
