# PAdES

Utility classes to sign pdf files width PAdES (PDF Advanced Electronic Signature) using [iText](https://itextpdf.com) or [Apache PDFBox](https://pdfbox.apache.org/) libraries.

Before use carefully read the licenses of third-party libraries.

The library provides the following classes:

- `org.dew.pades.PAdESSignerIText` - PAdES Signer based on iText
- `org.dew.pades.PAdESSignerBox` - PAdES Signer based on Apache PDFBox

## Examples

```java
PAdESSignerIText pades = new PAdESSignerIText("keystore.jks", "password", "selfsigned");

pades.sign(pdfFilePath, signedFilePath, "creator", "contact@dew.org", "reason", "location");

byte[] out = pades.sign((byte[]) in, "creator", "contact@dew.org", "reason", "location");
```

```java
PAdESSignerBox pades = new PAdESSignerBox("keystore.jks", "password", "selfsigned");

pades.sign(pdfFilePath, signedFilePath, "creator", "contact@dew.org", "reason", "location");

byte[] out = pades.sign((byte[]) in, "creator", "contact@dew.org", "reason", "location");
```

## Start tool as external java process

```java
public static byte[] pades(byte[] docContent) throws Exception {
  String signerFolder = "C:\\prj\\dew\\pades";
  String signerTarget = "C:\\prj\\dew\\pades\\target\\";
  String signerLibs   = "C:\\prj\\dew\\pades\\target\\dependency";
  
  if(docContent == null) {
    System.out.println("pades(null) -> null");
    return null;
  }
  if(docContent.length == 0) {
    System.out.println("pades([]) -> []");
    return docContent;
  }
  System.out.println("pades([" + docContent.length + "])...");
  
  String osName   = System.getProperty("os.name");
  if(osName == null || osName.length() == 0) osName = "Linux";
  boolean windows = osName.charAt(0) == 'W' || osName.charAt(0) == 'w';
  String javaHome = System.getProperty("java.home");
  if(javaHome == null || javaHome.length() == 0) {
    System.out.println("  Undefined java.home in properties.");
    System.out.println("pades([" + docContent.length + "]) -> [" + docContent.length + "] (no signature)");
    return docContent;
  }
  File folder = new File(signerFolder);
  if(!folder.exists()) {
    System.out.println("  Folder " + folder.getAbsolutePath() + " does not exist.");
    System.out.println("pades([" + docContent.length + "]) -> [" + docContent.length + "] (no signature)");
    return docContent;
  }
  File libFolder = new File(signerLibs);
  if(!libFolder.exists()) {
    System.out.println("  Folder " + libFolder.getAbsolutePath() + " does not exist.");
    System.out.println("pades([" + docContent.length + "]) -> [" + docContent.length + "] (no signature)");
    return docContent;
  }
  File keystore = new File(signerFolder + File.separator + "keystore.jks");
  if(!keystore.exists()) {
    System.out.println("  Keystore " + keystore.getAbsolutePath() + " does not exist.");
    System.out.println("pades([" + docContent.length + "]) -> [" + docContent.length + "] (no signature)");
    return docContent;
  }
  
  String fileName   = "tmp_" + WUtil.formatDateTime(Calendar.getInstance(), "#", true);
  String fileToSign = signerFolder + File.separator + fileName + ".pdf";
  String fileSigned = signerFolder + File.separator + fileName + "_signed.pdf";
  
  saveContent(docContent, fileToSign);
  
  List<String> libraries = new ArrayList<String>();
  libraries.add("commons-7.2.5.jar");
  libraries.add("commons-logging-1.2.jar");
  libraries.add("slf4j-api-1.7.36.jar");
  libraries.add("slf4j-simple-1.7.36.jar");
  libraries.add("bcpkix-jdk15on-1.70.jar");
  libraries.add("bcprov-jdk15on-1.70.jar");
  libraries.add("bcutil-jdk15on-1.70.jar");
  libraries.add("fontbox-2.0.28.jar");
  libraries.add("forms-7.2.5.jar");
  libraries.add("io-7.2.5.jar");
  libraries.add("kernel-7.2.5.jar");
  libraries.add("layout-7.2.5.jar");
  libraries.add("pdfa-7.2.5.jar");
  libraries.add("pdfbox-2.0.28.jar");
  libraries.add("sign-7.2.5.jar");
  
  String appJar = signerTarget + File.separator + "pades-1.0.0.jar";
  
  String classPath = "";
  for(int i = 0; i < libraries.size(); i++) {
    classPath += signerFolder + File.separator + "lib" + File.separator + libraries.get(i);
    if(i < libraries.size() - 1) classPath += File.pathSeparator;
  }
  if(classPath.length() > 0) classPath += File.pathSeparator;
  classPath += appJar;
  
  List<String> command = new ArrayList<String>();
  command.add(javaHome + File.separator + "bin" + File.separator + "java" + (windows ? ".exe" : ""));
  command.add("-cp");
  command.add(classPath);
  command.add("org.dew.pades.PAdESSignerTools");
  
  List<String> arguments = new ArrayList<String>();
  arguments.add(fileToSign);
  arguments.add(signerFolder + File.separator + "keystore.jks");
  arguments.add("password");
  arguments.add("key");
  arguments.add("password");
  arguments.add("creator");
  arguments.add("test@test.com");
  arguments.add("reason");
  arguments.add("location");
  arguments.add(fileSigned);
  
  int p = 0;
  String[] cmdarray = new String[command.size() + arguments.size()];
  for(int i = 0; i < command.size(); i++) {
    cmdarray[p++] = command.get(i);
  }
  for(int i = 0; i < arguments.size(); i++) {
    cmdarray[p++] = arguments.get(i);
  }
  
  BufferedReader brI = null;
  BufferedReader brE = null;
  Process process    = null;
  try {
    Runtime runtime = Runtime.getRuntime();
    process = runtime.exec(cmdarray, null, folder);
    brI = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String sLine = null;
    while((sLine = brI.readLine()) != null) {
      System.out.println("    " + sLine);
    }
    brE = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    while((sLine = brE.readLine()) != null) {
      System.out.println("    " + sLine);
    }
  }
  catch(Exception ex) {
    ex.printStackTrace();
    System.out.println("pades([" + docContent.length + "]) -> [" + docContent.length + "] (no signature)");
    return docContent;
  }
  finally {
    if(brE != null) try{ brE.close(); } catch(Exception ex) {}
    if(brI != null) try{ brI.close(); } catch(Exception ex) {}
    if(process != null) {
      try{
        process.destroy();
      }
      catch(Exception ex) {
      }
    }
  }
  
  File inpFile = new File(fileToSign);
  if(inpFile.exists()) {
    System.out.println("  Delete " + inpFile.getAbsolutePath() + "...");
    inpFile.delete();
  }
  
  File outFile = new File(fileSigned);
  if(!outFile.exists()) {
    System.out.println("  File " + fileSigned + " does not exist.");
    System.out.println("pades([" + docContent.length + "]) -> [" + docContent.length + "] (no signature)");
    return docContent;
  }
  
  byte[] result = readFile(outFile);
  
  System.out.println("  Delete " + outFile.getAbsolutePath() + "...");
  outFile.delete();
  
  if(result == null || result.length == 0) {
    System.out.println("  readFile(" + outFile.getAbsolutePath() + ") -> empty");
    System.out.println("pades([" + docContent.length + "]) -> [" + docContent.length + "] (no signature)");
    return docContent;
  }
  
  System.out.println("pades([" + docContent.length + "]) -> [" + result.length + "]");
  return result;
}
```

## Build

- `git clone https://github.com/giosil/pades.git`
- `mvn clean install`
- `mvn dependency:copy-dependencies`

## Contributors

* [Giorgio Silvestris](https://github.com/giosil)
