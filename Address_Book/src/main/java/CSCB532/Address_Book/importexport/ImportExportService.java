package CSCB532.Address_Book.importexport;

import CSCB532.Address_Book.contact.ContactService;
import CSCB532.Address_Book.contact.DtoContact;
import CSCB532.Address_Book.customRow.CustomRowService;
import CSCB532.Address_Book.customRow.DtoCustomRow;
import CSCB532.Address_Book.exception.ImportException;
import CSCB532.Address_Book.label.DtoLabel;
import CSCB532.Address_Book.label.LabelService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImportExportService {

    private final ContactService contactService;
    private final CustomRowService customRowService;
    private final LabelService labelService;


    @Transactional
    public String exportAllContactsToCSV() {
        List<DtoContact> allContacts = contactService.getAllContactsForLoggedInUser();

        // Convert contacts to CSV format
        StringBuilder csvContent = new StringBuilder();
        // Write the header
        csvContent.append("Name,Last Name,Phone Number,Name of Company,Address,Email,Fax,Mobile Number,Comment,Label,Custom Rows\n");

        // Write each contact to the CSV file
        for (DtoContact contact : allContacts) {
            csvContent.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    contact.getName(),
                    contact.getLastName(),
                    contact.getPhoneNumber(),
                    contact.getNameOfCompany(),
                    contact.getAddress(),
                    contact.getEmail(),
                    contact.getFax(),
                    contact.getMobileNumber(),
                    contact.getComment(),
                    convertLabelsToString(contact.getLabels()),
                    getCustomRowsAsString(contact.getCustomRows())));
        }

        return csvContent.toString();

    }
    @Transactional
    public void importContactsFromCSV(String csvData) {

        try (CSVReader csvReader = new CSVReader(new StringReader(csvData))) {
            List<String[]> csvRecords = csvReader.readAll();

            // first row is the header, skip it
            csvRecords = csvRecords.subList(1, csvRecords.size());

            List<DtoContact> contactsToImport = csvRecords.stream()
                    .map(record -> {
                        DtoContact contact = new DtoContact();
                        contact.setName(record[0]);
                        contact.setLastName(record[1]);
                        contact.setPhoneNumber(record[2]);
                        contact.setNameOfCompany(record[3]);
                        contact.setAddress(record[4]);
                        contact.setEmail(record[5]);
                        contact.setFax(record[6]);
                        contact.setMobileNumber(record[7]);
                        contact.setComment(record[8]);


                        // Set custom rows (assuming they are in the 10th column)
                        String customRowsAsString = record[10];
                        List<DtoCustomRow> customRows = parseCustomRows(customRowsAsString);
                        contact.setCustomRows(customRows);


                        return contact;
                    })
                    .toList();

            contactsToImport.forEach(contactService::createContact);
            System.out.println(contactsToImport);
        } catch (IOException | CsvException e) {
            throw new ImportException("Error importing contacts from CSV: " + e.getMessage(), e);
        }
    }

    @Transactional
    public String getCustomRowsAsString(List<DtoCustomRow> customRows) {
        StringBuilder customRowsString = new StringBuilder();

        if (customRows != null && !customRows.isEmpty()) {
            for (DtoCustomRow customRow : customRows) {
                customRowsString.append(customRow.getCustomName()).append(": ").append(customRow.getCustomField()).append("; ");
            }
            // Remove the trailing comma and space
            customRowsString.setLength(customRowsString.length() - 2);
        }

        return customRowsString.toString();
    }

    @Transactional
    public String convertLabelsToString(List<DtoLabel> labels) {
        return labels.stream()
                .map(DtoLabel::getName)
                .collect(Collectors.joining(", "));
    }

    private List<DtoCustomRow> parseCustomRows(String customRowsAsString) {
        List<DtoCustomRow> customRows = new ArrayList<>();

        if (customRowsAsString != null && !customRowsAsString.isEmpty()) {
            String[] customRowEntries = customRowsAsString.split("; ");

            for (String entry : customRowEntries) {
                String[] parts = entry.split(": ");
                if (parts.length == 2) {
                    String customName = parts[0].trim();
                    String customField = parts[1].trim();

                    DtoCustomRow dtoCustomRow = new DtoCustomRow();
                    dtoCustomRow.setCustomName(customName);
                    dtoCustomRow.setCustomField(customField);

                    customRows.add(dtoCustomRow);
                }
            }
        }

        return customRows;
    }

    @Transactional
    public String exportAllContactsToJSON() throws ExportException {
        List<DtoContact> ogContacts = contactService.getAllContactsForLoggedInUser();

        ModelMapper modelMapper = new ModelMapper();

        List<DtoContact> allContacts = ogContacts.stream()
                .map(contact -> modelMapper.map(contact, DtoContact.class))
                .toList();

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsString(allContacts);
        } catch (JsonProcessingException e) {
            throw new ExportException("Error exporting contacts to JSON: " + e.getMessage(), e);
        }
    }
    @Transactional
    public void importContactsFromJSON(String json) {

        ObjectMapper objectMapper = new ObjectMapper();
        List<DtoContact> importedContacts;

        try {
            importedContacts = objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new ImportException("Error importing contacts from JSON: " + e.getMessage(), e);
        }

        importedContacts.forEach(dtoContact -> {
            DtoContact contact = contactService.createContact(dtoContact);
            dtoContact.getCustomRows().forEach(customRowService::createCustomRow);
            dtoContact.getLabels().forEach(dtoLabel -> {
                if (labelService.getAllLabelsForLoggedInUser().contains(dtoLabel)){
                    dtoLabel.setName(dtoLabel.getName() + "-2");
                }
                DtoLabel label = labelService.createLabel(dtoLabel);
                contactService.addLabelToContact(contact.getId(), label.getId());
            });

        });
    }


//    @Transactional
//    public byte[] exportAllContactsToExcel() throws IOException {
//        List<DtoContact> allContacts = getAllContactsForLoggedInUser();
//
//        try (Workbook workbook = new XSSFWorkbook()) {
//            Sheet sheet = workbook.createSheet("Contacts");
//
//            // Write the header
//            Row headerRow = sheet.createRow(0);
//            String[] headerData = {"Name", "Last Name", "Phone Number", "Name of Company", "Address", "Email", "Fax",
//                    "Mobile Number", "Comment", "Label", "Custom Rows"};
//            for (int i = 0; i < headerData.length; i++) {
//                Cell cell = headerRow.createCell(i);
//                cell.setCellValue(headerData[i]);
//            }
//
//            // Write each contact to the Excel file
//            int rowNum = 1;
//            for (DtoContact contact : allContacts) {
//                Row row = sheet.createRow(rowNum++);
//
//                row.createCell(0).setCellValue(contact.getName());
//                row.createCell(1).setCellValue(contact.getLastName());
//                row.createCell(2).setCellValue(contact.getPhoneNumber());
//                row.createCell(3).setCellValue(contact.getNameOfCompany());
//                row.createCell(4).setCellValue(contact.getAddress());
//                row.createCell(5).setCellValue(contact.getEmail());
//                row.createCell(6).setCellValue(contact.getFax());
//                row.createCell(7).setCellValue(contact.getMobileNumber());
//                row.createCell(8).setCellValue(contact.getComment());
//
//                Cell labelCell = row.createCell(9);
//                labelCell.setCellValue(contact.getLabel() != null ? contact.getLabel().getName() : "null");
//
//                Cell customRowsCell = row.createCell(10);
//                customRowsCell.setCellValue(getCustomRowsAsString(contact.getCustomRows()));
//            }
//
//            // Save the workbook content to a ByteArrayOutputStream
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            workbook.write(outputStream);
//            return outputStream.toByteArray();
//        }
//    }

//    @Transactional
//    public List<DtoContact> getContactsWithMostCommonLabelByUserId(){
//        User user = authenticationService.getCurrentlyLoggedUser();
//        List<Contact> userContacts = contactRepository.findAllWithMostCommonLabelByUserId(user.getId());
//
//        ModelMapper modelMapper = new ModelMapper();
//
//        return userContacts.stream()
//                .map(contact -> modelMapper.map(contact, DtoContact.class))
//                .toList();
//    }
}
