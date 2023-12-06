package CSCB532.Address_Book.user;

import CSCB532.Address_Book.auth.AuthenticationService;
import CSCB532.Address_Book.auth.emailVerification.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/user-profile")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;
    private final EmailService emailService;
    private final AuthenticationService authenticationService;

    //get information about user profile
    @GetMapping("/get-profile")
    public ResponseEntity<DtoUserResponse> getUserProfile() {

        return ResponseEntity.ok(userService.getUserProfile());

    }


    //change user password
    @PatchMapping("/change-password")
    public ResponseEntity<String> updateUserPassword(
            @Valid @RequestBody DtoPasswordRequest dtoPasswordRequest) {

        return ResponseEntity.ok(userService.updateUserPassword(dtoPasswordRequest));

    }

    //update first and last name
    @PatchMapping("/update-user-names")
    public ResponseEntity<DtoUserResponse> updateUserProfile(
            @RequestBody DtoUserNamesRequest dtoUserNamesRequest) {

        return ResponseEntity.ok(userService.updateUserNames(dtoUserNamesRequest));

    }

    @PostMapping("/update-email-request")
    public ResponseEntity<String> updateEmailRequest(@Valid @RequestBody DtoEmailRequest dtoEmailRequest) throws IOException {

        emailService.sendVerificationEmailChange(dtoEmailRequest.getEmail());
        return new ResponseEntity<>("Registration successful, please check your email for verification", HttpStatus.CREATED);
    }

    @GetMapping("/verifyEmailChange")
    public ResponseEntity<String> verifyAccount(@RequestParam String code, @RequestParam String email) {
        boolean isVerified = authenticationService.verifyUserAndChangeEmail(code, email);
        if (isVerified) {
            return new ResponseEntity<>("Email successfully changed.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Invalid verification code.", HttpStatus.BAD_REQUEST);
        }
    }

    //update-email
    @PatchMapping("/update-email")
    public ResponseEntity<DtoUserResponse> updateEmail(
            @Valid @RequestBody DtoEmailRequest dtoEmailRequest) {

        return ResponseEntity.ok(userService.updateEmail(dtoEmailRequest));

    }

    //delete profile
    @DeleteMapping("/delete-user-profile")
    public ResponseEntity<String> deleteUserProfile() {
        userService.deleteUserProfile();
        return ResponseEntity.noContent().build();
    }

}
