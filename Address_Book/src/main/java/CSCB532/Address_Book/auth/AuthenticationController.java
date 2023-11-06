package CSCB532.Address_Book.auth;

import CSCB532.Address_Book.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:5173")

public class AuthenticationController {

    private final AuthenticationService service;


    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) throws ApiException {

        try {
            return ResponseEntity.ok(service.register(request));
        } catch (Exception e) {
            throw new ApiException("Email is already in use.", e, HttpStatus.NOT_ACCEPTABLE, ZonedDateTime.now());
        }


    }


    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody AuthenticationRequest request
    ) throws ApiException {

        return ResponseEntity.ok(service.authenticate(request));
    }

    @ExceptionHandler(value = ApiException.class)
    public ResponseEntity<Object> handleApiException(ApiException e) {
        // Create a custom error response using your ApiException
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("Gossip", e.getMessage());
        errorResponse.put("Mood", e.getHttpStatus());
        errorResponse.put("Delivered", e.getTimeStamp());

        return new ResponseEntity<>(errorResponse, e.getHttpStatus());
    }
}
