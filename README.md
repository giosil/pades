# PAdES

Utility class to sign pdf files width PAdES (PDF Advanced Electronic Signature) based on iText (https://itextpdf.com).

Before user see [iText License](https://itextpdf.com/how-buy/AGPLv3-license).

The library provides the following classes:

- `org.dew.pades.PAdESSigner` - PAdES Signer

## Example

```java
PAdESSigner pades = new PAdESSigner("keystore.jks", "password", "selfsigned");

pades.sign(pdfFilePath, signedFilePath, "creator", "contact@dew.org", "reason", "location");

byte[] out = pades.sign((byte[]) in, "creator", "contact@dew.org", "reason", "location");
```

## Build

- `git clone https://github.com/giosil/pades.git`
- `mvn clean install`
- `mvn dependency:copy-dependencies`

## Contributors

* [Giorgio Silvestris](https://github.com/giosil)
