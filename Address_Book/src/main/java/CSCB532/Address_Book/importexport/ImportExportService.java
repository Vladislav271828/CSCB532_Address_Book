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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        csvContent.append("Name,Last Name,Phone Number,Name of Company,Address,Email,Fax,Mobile Number,Comment,Labels,Custom Rows\n");

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

                        String labelsAsString = record[9];
                        List<DtoLabel> labels = parseLabels(labelsAsString);
                        contact.setLabels(labels);

                        String customRowsAsString = record[10];
                        List<DtoCustomRow> customRows = parseCustomRows(customRowsAsString);
                        contact.setCustomRows(customRows);


                        return contact;
                    })
                    .toList();

            importContactList(contactsToImport);
        } catch (IOException | CsvException e) {
            throw new ImportException("Error importing contacts from CSV: " + e.getMessage(), e);
        }
    }

    @Transactional
    public String getCustomRowsAsString(List<DtoCustomRow> customRows) {
        StringBuilder customRowsString = new StringBuilder();

        if (customRows != null && !customRows.isEmpty()) {
            customRows.forEach(customRow -> customRowsString.append(customRow.getCustomName()).append(": ").append(customRow.getCustomField()).append("; "));
            // Remove the trailing comma and space
            customRowsString.setLength(customRowsString.length() - 2);
        }

        return customRowsString.toString();
    }

    @Transactional
    public String convertLabelsToString(List<DtoLabel> labels) {
        StringBuilder labelsString = new StringBuilder();

        if (labels != null && !labels.isEmpty()) {
            labels.forEach(customRow -> labelsString.append(customRow.getName()).append(": ").append(customRow.getColorRGB()).append("; "));
            // Remove the trailing comma and space
            labelsString.setLength(labelsString.length() - 2);
        }
        return labelsString.toString();
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

    private List<DtoLabel> parseLabels(String labelsAsString) {
        List<DtoLabel> labels = new ArrayList<>();

        if (labelsAsString != null && !labelsAsString.isEmpty()) {
            String[] customRowEntries = labelsAsString.split("; ");

            for (String entry : customRowEntries) {
                String[] parts = entry.split(": ");
                if (parts.length == 2) {
                    String customName = parts[0].trim();
                    String color = parts[1].trim();

                    DtoLabel dtoLabel = new DtoLabel();
                    dtoLabel.setName(customName);
                    dtoLabel.setColorRGB(color);

                    labels.add(dtoLabel);
                }
            }
        }

        return labels;
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
        importContactList(importedContacts);
    }

    private void importContactList(List<DtoContact> importedContacts){
        importedContacts.forEach(dtoContact -> {
            DtoContact contact = contactService.createContact(dtoContact);
            dtoContact.getCustomRows().forEach(dtoCustomRow -> {
                dtoCustomRow.setContactId(contact.getId());
                customRowService.createCustomRow(dtoCustomRow);
            });
            dtoContact.getLabels().forEach(dtoLabel -> {
                if (labelService.getAllLabelsForLoggedInUser().contains(dtoLabel)){
                    //dtoLabel.setName(dtoLabel.getName() + "-2");
                    Integer labelId = labelService.getAllLabelsForLoggedInUser().stream()
                            .filter(label -> dtoLabel.getName().equals(label.getName()))
                            .toList().get(0).getId();
                    contactService.addLabelToContact(contact.getId(), labelId);
                }
                else {
                    DtoLabel label = labelService.createLabel(dtoLabel);
                    contactService.addLabelToContact(contact.getId(), label.getId());
                }
            });
        });
    }


    @Transactional
    public byte[] exportAllContactsToExcel() throws IOException {
        List<DtoContact> allContacts = contactService.getAllContactsForLoggedInUser();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Contacts");

            // Write the header
            Row headerRow = sheet.createRow(0);
            String[] headerData = {"Name", "Last Name", "Phone Number", "Name of Company", "Address", "Email", "Fax",
                    "Mobile Number", "Comment", "Labels", "Custom Rows"};
            for (int i = 0; i < headerData.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headerData[i]);
            }

            // Write each contact to the Excel file
            int rowNum = 1;
            for (DtoContact contact : allContacts) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(contact.getName());
                row.createCell(1).setCellValue(contact.getLastName());
                row.createCell(2).setCellValue(contact.getPhoneNumber());
                row.createCell(3).setCellValue(contact.getNameOfCompany());
                row.createCell(4).setCellValue(contact.getAddress());
                row.createCell(5).setCellValue(contact.getEmail());
                row.createCell(6).setCellValue(contact.getFax());
                row.createCell(7).setCellValue(contact.getMobileNumber());
                row.createCell(8).setCellValue(contact.getComment());

                Cell labelCell = row.createCell(9);
                labelCell.setCellValue(convertLabelsToString(contact.getLabels()));

                Cell customRowsCell = row.createCell(10);
                customRowsCell.setCellValue(getCustomRowsAsString(contact.getCustomRows()));
            }

            // Save the workbook content to a ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    @Transactional
    public void importContactsFromExcel(byte[] excelData) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            Sheet sheet = workbook.getSheetAt(0);

            Iterator<Row> rowIterator = sheet.iterator();

            // Skip the header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }
            List<DtoContact> importedContacts = new ArrayList<>();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                DtoContact dtoContact = new DtoContact();
                dtoContact.setName(getStringCellValue(row.getCell(0)));
                dtoContact.setLastName(getStringCellValue(row.getCell(1)));
                dtoContact.setPhoneNumber(getStringCellValue(row.getCell(2)));
                dtoContact.setNameOfCompany(getStringCellValue(row.getCell(3)));
                dtoContact.setAddress(getStringCellValue(row.getCell(4)));
                dtoContact.setEmail(getStringCellValue(row.getCell(5)));
                dtoContact.setFax(getStringCellValue(row.getCell(6)));
                dtoContact.setMobileNumber(getStringCellValue(row.getCell(7)));
                dtoContact.setComment(getStringCellValue(row.getCell(8)));

                // Process labels and custom rows accordingly
                 dtoContact.setLabels(parseLabels(getStringCellValue(row.getCell(9))));
                 dtoContact.setCustomRows(parseCustomRows(getStringCellValue(row.getCell(10))));

                importedContacts.add(dtoContact);
            }
            importContactList(importedContacts);
        }
    }

    private String getStringCellValue(Cell cell) {
        if (cell != null) {
            return cell.getStringCellValue();
        }
        return null;
    }

}
