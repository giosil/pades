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

## Build

- `git clone https://github.com/giosil/pades.git`
- `mvn clean install`
- `mvn dependency:copy-dependencies`

## Contributors

* [Giorgio Silvestris](https://github.com/giosil)
