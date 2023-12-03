package CSCB532.Address_Book.user;

import CSCB532.Address_Book.auth.AuthenticationService;
import CSCB532.Address_Book.exception.BadRequestException;
import CSCB532.Address_Book.exception.DatabaseException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DtoUserResponse getUserProfile() {
        User currentUser = authenticationService.getCurrentlyLoggedUser();
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(currentUser, DtoUserResponse.class);
    }

    public String updateUserPassword(DtoPasswordRequest dtoPasswordRequest) {
        //The incoming data is going to already be checked here because of the DTO validation annotation
        User currentUser = authenticationService.getCurrentlyLoggedUser();
        currentUser.setPassword(passwordEncoder.encode(dtoPasswordRequest.getPassword()));

        userRepository.save(currentUser);

        return "Password changed Successfully";
    }

    public DtoUserResponse updateUserNames(DtoUserNamesRequest dtoUserNamesRequest) {
        // Validate input data
        if (dtoUserNamesRequest.getLastName() == null && dtoUserNamesRequest.getFirstName() == null) {
            throw new BadRequestException("Invalid input, at least one name must be provided.");
        }

        // Get user information
        User currentUser = authenticationService.getCurrentlyLoggedUser();

        boolean isChanged = false;
        // Update first name if provided and different from the current name
        String newFirstName = dtoUserNamesRequest.getFirstName();
        if (newFirstName != null && !newFirstName.trim().isEmpty() &&
                !newFirstName.equals(currentUser.getFirstname())) {
            currentUser.setFirstname(newFirstName.trim());
            isChanged = true;
        }

        // Update last name if provided and different from the current name
        String newLastName = dtoUserNamesRequest.getLastName();
        if (newLastName != null && !newLastName.trim().isEmpty() &&
                !newLastName.equals(currentUser.getLastname())) {
            currentUser.setLastname(newLastName.trim());
            isChanged = true;
        }

        if (!isChanged){
            throw new BadRequestException("Invalid Data provided.");
        }

        // Save changes
        userRepository.save(currentUser);

        // Map to DTO and return the response
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(currentUser, DtoUserResponse.class);
    }


    public void deleteUserProfile() {
        User currentUser = authenticationService.getCurrentlyLoggedUser();

        if (userRepository.existsByEmail(currentUser.getEmail())){
            userRepository.delete(userRepository.findByEmail(currentUser.getEmail()).orElseThrow(
                    () -> new DatabaseException("Problem finding user")
            ));
        }
    }

    public DtoUserResponse updateEmail(DtoEmailRequest dtoEmailRequest) {
        User currentUser = authenticationService.getCurrentlyLoggedUser();

        //check if the email the user is trying to update with is already in db
        boolean isEmailTaken = isEmailTaken(dtoEmailRequest.getEmail());

        if (isEmailTaken){
            throw new BadRequestException("Email already in use.");
        }

        if (currentUser.getEmail().equals(dtoEmailRequest.getEmail())){
            throw new BadRequestException("Invalid input.");
        }

        currentUser.setEmail(dtoEmailRequest.getEmail());

        currentUser = userRepository.save(currentUser);

        // Map to DTO and return the response
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(currentUser, DtoUserResponse.class);
    }


    public boolean isEmailTaken(String email){
        Optional<User> user = userRepository.findByEmail(email);
        return user.isPresent();
    }
}
