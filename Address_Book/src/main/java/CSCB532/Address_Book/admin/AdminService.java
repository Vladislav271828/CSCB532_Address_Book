package CSCB532.Address_Book.admin;

import CSCB532.Address_Book.auth.AuthenticationService;
import CSCB532.Address_Book.contact.Contact;
import CSCB532.Address_Book.contact.ContactRepository;
import CSCB532.Address_Book.contact.ContactService;
import CSCB532.Address_Book.contact.DtoContact;
import CSCB532.Address_Book.exception.BadRequestException;
import CSCB532.Address_Book.user.Role;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.server.ExportException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AuthenticationService authenticationService;
    private final ContactService contactService;
    private final ContactRepository contactRepository;

    private void checkIfUserIsAdmin() {
        if (!authenticationService.getCurrentlyLoggedUser().getRole().equals(Role.ADMIN)) {
            throw new BadRequestException("User doesn't have permissions to perform this action.");
        }
    }


    public List<DtoContact> getAllContactsAsAdmin() {
        checkIfUserIsAdmin();
        List<Contact> userContacts = contactRepository.findAll();

        ModelMapper modelMapper = new ModelMapper();

        return userContacts.stream()
                .map(contact -> modelMapper.map(contact, DtoContact.class))
                .collect(Collectors.toList());

    }

    public List<DtoContact> searchContacts(DtoContact dtoContact) {
        checkIfUserIsAdmin();

        if ((dtoContact.getName() == null && dtoContact.getLastName() == null))
            throw new BadRequestException("Missing input.");

        List<Contact> userContacts = contactRepository.findAllByNameAndOrLastName(dtoContact.getName(), dtoContact.getLastName());
        ModelMapper modelMapper = new ModelMapper();
        return userContacts.stream()
                .map(contact -> modelMapper.map(contact, DtoContact.class))
                .collect(Collectors.toList());
    }

    public String exportAllContactsToCSV() {
        checkIfUserIsAdmin();
        List<Contact> ogContacts = contactRepository.findAll();

        ModelMapper modelMapper = new ModelMapper();

        List<DtoContact> allContacts = ogContacts.stream()
                .map(contact -> modelMapper.map(contact, DtoContact.class))
                .toList();

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
                    contact.getLabel() != null ? contact.getLabel().getName() : "null",
                    contactService.getCustomRowsAsString(contact.getCustomRows())));
        }

        return csvContent.toString();

    }

    public String exportAllContactsToJSON() throws ExportException {
        checkIfUserIsAdmin();
        List<Contact> ogContacts = contactRepository.findAll();

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

    public byte[] exportAllContactsToExcel() throws IOException {
        checkIfUserIsAdmin();
        List<Contact> ogContacts = contactRepository.findAll();

        ModelMapper modelMapper = new ModelMapper();

        List<DtoContact> allContacts = ogContacts.stream()
                .map(contact -> modelMapper.map(contact, DtoContact.class))
                .toList();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Contacts");

            // Write the header
            Row headerRow = sheet.createRow(0);
            String[] headerData = {"Name", "Last Name", "Phone Number", "Name of Company", "Address", "Email", "Fax",
                    "Mobile Number", "Comment", "Label", "Custom Rows"};
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
                labelCell.setCellValue(contact.getLabel() != null ? contact.getLabel().getName() : "null");

                Cell customRowsCell = row.createCell(10);
                customRowsCell.setCellValue(contactService.getCustomRowsAsString(contact.getCustomRows()));
            }

            // Save the workbook content to a ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public List<DtoContact> getContactsWithMostCommonLabel(){
        checkIfUserIsAdmin();
        List<Contact> userContacts = contactRepository.findAllWithMostCommonLabel();

        ModelMapper modelMapper = new ModelMapper();

        return userContacts.stream()
                .map(contact -> modelMapper.map(contact, DtoContact.class))
                .collect(Collectors.toList());
    }
}
