package CSCB532.Address_Book.admin;

import CSCB532.Address_Book.contact.DtoContact;
import CSCB532.Address_Book.label.DtoLabel;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.rmi.server.ExportException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    //get all contacts from the database
    @GetMapping("/get-all-contacts-as-admin")
    public ResponseEntity<List<DtoContact>> getAllContacts() {

        return ResponseEntity.ok(adminService.getAllContactsAsAdmin());
    }

    @PostMapping("/search-contact-as-admin")
    public ResponseEntity<List<DtoContact>> searchContact(
            @RequestBody DtoContact dtoContact) {

        return ResponseEntity.ok(adminService.searchContacts(dtoContact));

    }

//    @GetMapping("/export-as-admin/csv")
//    public ResponseEntity<String> exportAllContactsToCSV() {
//        String csvContent = adminService.exportAllContactsToCSV();
//        HttpHeaders headers = new HttpHeaders();
//        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");
//        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contacts.csv");
//        return new ResponseEntity<>(csvContent, headers, HttpStatus.OK);
//    }

    @GetMapping("/export-as-admin/json")
    public ResponseEntity<String> exportContactsToJSON() {
        try {
            String jsonContent = adminService.exportAllContactsToJSON();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contacts.json");
            return new ResponseEntity<>(jsonContent, headers, HttpStatus.OK);
        } catch (ExportException e) {
            return new ResponseEntity<>("Error exporting contacts to JSON", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    @PostMapping("/import-as-admin/json")
//    public ResponseEntity<String> importContacts(@RequestBody String json) throws JsonProcessingException {
//        adminService.importContactsFromJSON(json);
//        return ResponseEntity.ok("Contacts imported successfully.");
//    }

//    @CrossOrigin
//    @GetMapping("/export-as-admin/excel")
//    public ResponseEntity<byte[]> exportContactsToExcel() {
//        try {
//            byte[] excelContent = adminService.exportAllContactsToExcel();
//            HttpHeaders headers = new HttpHeaders();
//            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contacts.xlsx");
//            return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);
//        } catch (IOException e) {
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

//    @GetMapping("/get-contacts-with-most-common-label-as-admin")
//    public ResponseEntity<List<DtoContact>> getContactsWithMostCommonLabel() {
//
//        return ResponseEntity.ok(adminService.getContactsWithMostCommonLabel());
//    }

    @GetMapping("/get-contacts-with-label-as-admin/{labelId}")
    public ResponseEntity<List<DtoContact>> getContactsWithLabel(
            @PathVariable Integer labelId) {
        return ResponseEntity.ok(adminService.getContactsWithLabel(labelId));
    }

    @GetMapping("/get-all-labels-as-admin")
    public ResponseEntity<List<DtoLabel>> getAllLabels() {

        return ResponseEntity.ok(adminService.getAllLabels());
    }
}
