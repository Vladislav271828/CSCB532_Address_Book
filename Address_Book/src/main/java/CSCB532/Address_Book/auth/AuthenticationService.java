package CSCB532.Address_Book.auth;


import CSCB532.Address_Book.auth.emailVerification.Verification;
import CSCB532.Address_Book.auth.emailVerification.VerificationRepository;
import CSCB532.Address_Book.config.JwtService;
import CSCB532.Address_Book.exception.BadRequestException;
import CSCB532.Address_Book.token.Token;
import CSCB532.Address_Book.token.TokenRepository;
import CSCB532.Address_Book.token.TokenType;
import CSCB532.Address_Book.user.Role;
import CSCB532.Address_Book.user.User;
import CSCB532.Address_Book.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static CSCB532.Address_Book.util.DtoValidationUtil.checkUserDto;
import static CSCB532.Address_Book.util.DtoValidationUtil.errorMessage;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final VerificationRepository verificationRepository;


    public AuthenticationResponse register(RegisterRequest request) {//TODO switch back to return AuthenticationResponse instead of void

        //Check if incoming data is valid
        if (checkUserDto(request)){//checks if fields are invalid
            throw new BadRequestException("Invalid Request: ".concat(errorMessage(request)));//the errorMessage method will get the names of the invalid fields. I.e. email, password. And their values - though for the moment the values are going to be empty strings
        }


        //Check if new user is unique
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("User with email ".concat(request.getEmail()).concat(" already exists."));
        }

        //create user with request data
        var user = User.builder()
                .firstname(request.getFirstName())
                .lastname(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .verified(false)//sets the initial status of the user as not verified basically
                .build();


        var savedUser = repository.save(user); //save user in the db
        //TODO uncomment this after merge
        var jwtToken = jwtService.generateToken(user); //generate a JWT Token for the user's session (If the user logs out the token will be marked as invalid, if the user authenticates again a new token will be created and the old one will be updated to be invalid)

        saveUserToken(savedUser, jwtToken);//save the token

        return AuthenticationResponse.builder()
                .token(jwtToken)//we return a jwt token that can be used by the client to authenticate other requests
                .build()
                ;
    }


    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        if (checkUserDto(request)){//checks if fields are invalid
            throw new BadRequestException("Invalid Request: ".concat(errorMessage(request)));//the errorMessage method will get the names of the invalid fields. I.e. email, password. And their values - though for the moment the values are going to be empty strings
        }

        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();//we do this and need it to there after revoke all previous jwt tokens


//        boolean verified = userRepository.isUserVerified(user.getId());//TODO comment this out after merge

//        if (!verified){
//            throw new BadRequestException("User is not verified. A verification email has been sent to " + user.getEmail() + ".");//TODO comment this out after merge
//        }
        authenticationManager.authenticate(//authenticates the user
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );



        revokeAllUserTokens(user);//ensures jwt tokens are set to false before creating a new one to avoid addition of more than one valid jwt tokens

        var jwtToken = jwtService.generateToken(user);//generates a new token
        saveUserToken(user, jwtToken);//saves user token - saves session in a way

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
    public User getCurrentlyLoggedUser() { //throws the cringe
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // This returns the username/email of the authenticated user

        return userRepository.findByEmail(email)
                .orElseThrow();
    }

    public boolean verifyUser(String code) {
        Optional<Verification> verificationOpt = verificationRepository.findByVerificationCode(code);

        if (verificationOpt.isPresent()) {
            Verification verification = verificationOpt.get();

            if (!verification.isExpired()) {
                User user = verification.getUser();
                user.setVerified(true);
                userRepository.save(user);

                verification.setExpired(true);
                verificationRepository.save(verification);

                return true;
            }
        }
        return false;
    }

    public boolean verifyUserAndChangeEmail(String code, String email) {
        Optional<Verification> verificationOpt = verificationRepository.findByVerificationCode(code);

        if (verificationOpt.isPresent()) {
            Verification verification = verificationOpt.get();

            if (!verification.isExpired()) {
                User user = verification.getUser();
                user.setVerified(true);
                user.setEmail(email);
                userRepository.save(user);

                verification.setExpired(true);
                verificationRepository.save(verification);

                return true;
            }
        }
        return false;
    }
}
