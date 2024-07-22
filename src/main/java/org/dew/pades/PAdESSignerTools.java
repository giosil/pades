package org.dew.pades;

import java.io.File;

public class PAdESSignerTools {
  public static final int FILE_TO_SIGN  = 0;
  public static final int KEYSTORE      = 1;
  public static final int KEYSTORE_PASS = 2;
  public static final int ALIAS_KEY     = 3;
  public static final int KEY_PASS      = 4;
  public static final int CREATOR       = 5;
  public static final int CONTACT       = 6;
  public static final int REASON        = 7;
  public static final int LOCATION      = 8;
  public static final int FILE_SIGNED   = 9;
  public static final int LIBRARY       = 10;
  
  public static void main(String[] args) {
    
    System.out.println("PAdESSignerTools v. 1.0.0");
    System.out.println("-------------------------");
    if(args == null || args.length < LOCATION + 1) {
      System.out.println("Usage:");
      System.out.println("------\n");
      //                                   0            1        2             3         4        5       6       7      8        9             10
      System.out.println("PAdESSignerTools file_to_sign keystore keystore_pass alias_key key_pass creator contact reason location [file_signed] [lib=itext]");
      System.exit(1);
      return;
    }
    
    File file = new File(args[0]);
    if(!file.exists()) {
      System.out.println("File " + args[FILE_TO_SIGN] + " does not exist.");
      System.exit(1);
      return;
    }
    if(!file.isFile()) {
      System.out.println("Argument " + args[FILE_TO_SIGN] + " is not a file.");
      System.exit(1);
      return;
    }
    File keystore = new File(args[KEYSTORE]);
    if(!keystore.exists()) {
      System.out.println("Keystore " + args[KEYSTORE] + " does not exist.");
      System.exit(1);
      return;
    }
    String signedFilePath = null;
    if(args.length >= FILE_SIGNED + 1) {
      signedFilePath = args[FILE_SIGNED];
    }
    else {
      signedFilePath = args[FILE_TO_SIGN] + ".signed";
    }
    
    String creator  = args[CREATOR];
    String contact  = args[CONTACT];
    String reason   = args[REASON];
    String location = args[LOCATION];
    
    if(creator  == null || creator.equals("_"))  creator  = "";
    if(contact  == null || contact.equals("_"))  contact  = "";
    if(reason   == null || reason.equals("_"))   reason   = "";
    if(location == null || location.equals("_")) location = "";
    
    String lib = null;
    if(args != null && args.length >= 6) {
      lib = args[LIBRARY];
    }
    if(lib == null || lib.length() == 0) {
      lib = "itext";
    }
    char c0 = lib.charAt(0);
    if(c0 == 'i' || c0 == 'I') {
      System.out.println("Sign " + args[FILE_TO_SIGN] + " with iText...");
      // iText
      try {
        PAdESSignerIText pades = new PAdESSignerIText(args[KEYSTORE], args[KEYSTORE_PASS], args[ALIAS_KEY], args[KEY_PASS]);
        
        pades.sign(args[FILE_TO_SIGN], signedFilePath, creator, contact, reason, location);
        
        System.out.println("end.");
      }
      catch(Exception ex) {
        ex.printStackTrace();
        System.exit(1);
        return;
      }
    }
    else {
      System.out.println("Sign " + args[FILE_TO_SIGN] + " with Apache PDFBox...");
      // PDFBox
      try {
        PAdESSignerBox pades = new PAdESSignerBox(args[KEYSTORE], args[KEYSTORE_PASS], args[ALIAS_KEY], args[KEY_PASS]);
        
        pades.sign(args[FILE_TO_SIGN], signedFilePath, creator, contact, reason, location);
        
        System.out.println("end.");
      }
      catch(Exception ex) {
        ex.printStackTrace();
        System.exit(1);
        return;
      }
    }
  }
}
