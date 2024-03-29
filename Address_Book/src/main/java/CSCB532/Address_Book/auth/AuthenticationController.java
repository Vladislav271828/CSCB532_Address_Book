package CSCB532.Address_Book.auth;

import CSCB532.Address_Book.auth.emailVerification.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor

public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) throws IOException {
        authenticationService.register(request);//TODO comment this out to remove email verification checks
        emailService.sendVerificationEmail(request.getEmail());//TODO comment this out to remove email verification checks
        return new ResponseEntity<>("Registration successful, please check your email for verification", HttpStatus.CREATED);//TODO comment this out after merge
//         return ResponseEntity.ok(authenticationService.register(request)); //TODO uncomment this to remove email verification checks
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam String code) {
        boolean isVerified = authenticationService.verifyUser(code);//TODO comment this out to remove email verification checks
        if (isVerified) {
            return new ResponseEntity<>("Account successfully verified.", HttpStatus.OK);//TODO comment this out to remove email verification checks
        } else {
            return new ResponseEntity<>("Invalid verification code.", HttpStatus.BAD_REQUEST);//TODO comment this out to remove email verification checks
        }
//        return ResponseEntity.ok(authenticationService.authenticate(request)); //TODO uncomment this out to remove email verification checks
    }


    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid
            @RequestBody AuthenticationRequest request
    ) {

        return ResponseEntity.ok(authenticationService.authenticate(request));
    }


}
