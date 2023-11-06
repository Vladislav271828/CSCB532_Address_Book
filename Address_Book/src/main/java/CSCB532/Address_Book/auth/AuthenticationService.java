package CSCB532.Address_Book.auth;


import CSCB532.Address_Book.config.JwtService;
import CSCB532.Address_Book.exception.ApiException;
import CSCB532.Address_Book.token.Token;
import CSCB532.Address_Book.token.TokenRepository;
import CSCB532.Address_Book.token.TokenType;
import CSCB532.Address_Book.user.Role;
import CSCB532.Address_Book.user.User;
import CSCB532.Address_Book.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;


    public AuthenticationResponse register(RegisterRequest request) throws ApiException {

        if (request.getEmail().isEmpty() || request.getPassword().isEmpty() || request.getFirstName().isEmpty()) {
            throw new ApiException("Invalid input bruh..", new Throwable(), HttpStatus.BAD_REQUEST, ZonedDateTime.now());
        }

        var user = User.builder()
                .firstname(request.getFirstName())
                .lastname(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();


        var savedUser = repository.save(user); //here we save the user in the db
        var jwtToken = jwtService.generateToken(user); //we generate a JWT Token

        saveUserToken(savedUser, jwtToken);

        return AuthenticationResponse.builder()
                .token(jwtToken)//we return a jwt token
                .build()
                ;
    }


    public AuthenticationResponse authenticate(AuthenticationRequest request) throws ApiException {

        if (request.getEmail().isEmpty() || request.getPassword().isEmpty()) {
            throw new ApiException("Invalid input bruh..", new Throwable(), HttpStatus.BAD_REQUEST, ZonedDateTime.now());
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );


        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();

        var jwtToken = jwtService.generateToken(user);

        revokeAllUserTokens(user);//ensures jwt tokens are set to false before creating anew one to avoid addition of more than one valid jwt tokens

        saveUserToken(user, jwtToken);//saves user token

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build()
                ;
    }


    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if (validUserTokens.isEmpty()) {
            return;
        }
        validUserTokens.forEach(t -> {
            t.setExpired(true);
            t.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }


    private void saveUserToken(User savedUser, String jwtToken) {
        var token = Token.builder()
                .user(savedUser)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .revoked(false)
                .expired(false)
                .build();//here we create a Token object

        tokenRepository.save(token);//we save the token object in our database
    }


    //ultra cool method, I use it every time (Not sure if this is good practice LOL)
    public User getCurrentlyLoggedUser() throws ApiException { //throws the cringe
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // This returns the username/email of the authenticated user

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("Big problem", new Throwable(), HttpStatus.NOT_FOUND, ZonedDateTime.now()));
    }

}