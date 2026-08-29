package com.kreview.kreview.service;

import com.kreview.kreview.exception.InvalidRequestException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentTextExtractorService {

  public String extractText(MultipartFile file) {
    String filename = file.getOriginalFilename();
    if (filename == null || filename.isBlank()) {
      throw new InvalidRequestException("Uploaded file has no filename");
    }
    String lower = filename.toLowerCase(Locale.ROOT);

    try {
      if (lower.endsWith(".pdf")) {
        return extractPdf(file);
      } else if (lower.endsWith(".docx")) {
        return extractDocx(file);
      } else if (lower.endsWith(".txt")) {
        return new String(file.getBytes(), StandardCharsets.UTF_8);
      } else {
        throw new InvalidRequestException(
            "Unsupported file type: " + filename + ". Supported types: .pdf, .docx, .txt");
      }
    } catch (IOException e) {
      throw new InvalidRequestException("Could not read uploaded file: " + e.getMessage());
    }
  }

  private String extractPdf(MultipartFile file) throws IOException {
    try (PDDocument document = Loader.loadPDF(file.getBytes())) {
      return new PDFTextStripper().getText(document);
    } catch (IOException e) {
      throw new InvalidRequestException("Could not parse PDF file: " + e.getMessage());
    }
  }

  private String extractDocx(MultipartFile file) throws IOException {
    try (XWPFDocument document = new XWPFDocument(file.getInputStream());
         XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
      return extractor.getText();
    } catch (IOException e) {
      throw new InvalidRequestException("Could not parse Word document: " + e.getMessage());
    }
  }
}
