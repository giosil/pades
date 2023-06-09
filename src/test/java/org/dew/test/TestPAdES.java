package org.dew.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.dew.pades.PAdESSignerIText;
import org.dew.pades.PAdESSignerBox;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestPAdES extends TestCase {
  
  public TestPAdES(String testName) {
    super(testName);
  }
  
  public static Test suite() {
    return new TestSuite(TestPAdES.class);
  }
  
  public 
  void testApp() 
    throws Exception 
  {
    check_PAdESSignerIText();
    
    check_PAdESSignerBox();
  }
  
  public 
  void check_PAdESSignerIText() 
    throws Exception 
  {
    String pdfFilePath = getDesktopPath("test_itext.pdf");
    
    System.out.println("Check " + pdfFilePath + "...");
    File file = new File(pdfFilePath);
    if(!file.exists()) {
      System.out.println("Generate test pdf...");
      byte[] content = generatePdf("itext");
        
      System.out.println("Save " + pdfFilePath + "...");
      writeFile(pdfFilePath, content);
    }
    
    String signedFilePath = getDesktopPath("test_itext_signed.pdf");
    
    System.out.println("Check " + signedFilePath + "...");
    File signedFile = new File(signedFilePath);
    if(!signedFile.exists()) {
      signedFile.delete();
    }
    
    System.out.println("new PAdESSignerIText...");
    PAdESSignerIText pades = new PAdESSignerIText("keystore.jks", "password", "selfsigned");
    
    System.out.println("sign...");
    pades.sign(pdfFilePath, signedFilePath, "creator", "contact@dew.org", "reason", "location");
    
    System.out.println("OK");
  }
  
  public 
  void check_PAdESSignerBox() 
    throws Exception 
  {
    String pdfFilePath = getDesktopPath("test_box.pdf");
    
    System.out.println("Check " + pdfFilePath + "...");
    File file = new File(pdfFilePath);
    if(!file.exists()) {
      System.out.println("Generate test pdf...");
      byte[] content = generatePdf("PDFBox");
        
      System.out.println("Save " + pdfFilePath + "...");
      writeFile(pdfFilePath, content);
    }
    
    String signedFilePath = getDesktopPath("test_box_signed.pdf");
    
    System.out.println("Check " + signedFilePath + "...");
    File signedFile = new File(signedFilePath);
    if(!signedFile.exists()) {
      signedFile.delete();
    }
    
    System.out.println("new PAdESSignerBox...");
    PAdESSignerBox pades = new PAdESSignerBox("keystore.jks", "password", "selfsigned");
    
    System.out.println("sign...");
    pades.sign(pdfFilePath, signedFilePath, "creator", "contact@dew.org", "reason", "location");
    
    System.out.println("OK");
  }
  
  public static
  String getDesktop()
  {
    String sUserHome = System.getProperty("user.home");
    return sUserHome + File.separator + "Desktop";
  }
  
  public static
  String getDesktopPath(String sFileName)
  {
    String sUserHome = System.getProperty("user.home");
    return sUserHome + File.separator + "Desktop" + File.separator + sFileName;
  }
  
  public static
  byte[] generatePdf(String text)
    throws Exception
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    
    PdfWriter pdfWriter = new PdfWriter(baos);
    
    PdfDocument pdfDocument = new PdfDocument(pdfWriter);
    
    Document document = new Document(pdfDocument);
    
    document.add(new Paragraph(text));
    
    document.close();
    
    return baos.toByteArray();
  }
  
  public static
  byte[] readFile(String filePath)
    throws Exception
  {
    InputStream is = null;
    try {
      is = new FileInputStream(filePath);
      int n;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buff = new byte[1024];
      while((n = is.read(buff)) > 0) baos.write(buff, 0, n);
      return baos.toByteArray();
    }
    finally {
      if(is != null) try{ is.close(); } catch(Exception ex) {}
    }
  }
  
  public static
  void writeFile(String filePath, byte[] content)
    throws Exception
  {
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(filePath);
      fos.write(content);
    }
    finally {
      if(fos != null) try{ fos.close(); } catch(Exception ex) { ex.printStackTrace(); }
    }
  }
}
