package CSCB532.Address_Book.importexport;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.rmi.server.ExportException;

@RestController
@RequestMapping("/api/v1/")
public class ImportExportController {

    private final ImportExportService importExportService;

    public ImportExportController(ImportExportService importExportService) {
        this.importExportService = importExportService;
    }

    @GetMapping("/export/csv")
    public ResponseEntity<String> exportAllContactsToCSV() {
        String csvContent = importExportService.exportAllContactsToCSV();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contacts.csv");
        return new ResponseEntity<>(csvContent, headers, HttpStatus.OK);
    }

    @PostMapping("/import/csv")
    public ResponseEntity<String> importContactsFromCSV(@RequestBody String json) {
        importExportService.importContactsFromCSV(json);
        return ResponseEntity.ok("Contacts imported successfully.");
    }

    @GetMapping("/export/json")
    public ResponseEntity<String> exportContactsToJSON() {
        try {
            String jsonContent = importExportService.exportAllContactsToJSON();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contacts.json");
            return new ResponseEntity<>(jsonContent, headers, HttpStatus.OK);
        } catch (ExportException e) {
            return new ResponseEntity<>("Error exporting contacts to JSON", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/import/json")
    public ResponseEntity<String> importContactsFromJSON(@RequestBody String json) {
        importExportService.importContactsFromJSON(json);
        return ResponseEntity.ok("Contacts imported successfully.");
    }

    @CrossOrigin
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportContactsToExcel() {
        try {
            byte[] excelContent = importExportService.exportAllContactsToExcel();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contacts.xlsx");
            return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @CrossOrigin
    @PostMapping("/import/excel")
    public ResponseEntity<String> importContactsFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            byte[] excelData = file.getBytes();
            importExportService.importContactsFromExcel(excelData);
            return ResponseEntity.ok("Contacts imported successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error importing contacts from Excel: " + e.getMessage());
        }
    }
}
