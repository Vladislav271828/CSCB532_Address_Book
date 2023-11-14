package admin;

import CSCB532.Address_Book.contact.Contact;
import CSCB532.Address_Book.contact.DtoContact;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    //get all contacts from the database PRIORITY
    @GetMapping("/get-all-contacts-as-admin")
    public ResponseEntity<List<Contact>> getAllContacts() {

        return ResponseEntity.ok(adminService.getAllContactsAsAdmin());

    }

    //change user's role (Not required)


    //delete user profile (Not required)


}
