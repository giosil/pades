package org.dew.pades;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;

import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

public 
class PAdESSignerBox implements SignatureInterface
{
  protected Certificate   certificate;
  protected PrivateKey    privateKey;
  protected Certificate[] certChain;
  
  protected boolean initCompleted;
  
  public PAdESSignerBox(String keystoreFile, String password, String alias)
      throws Exception
  {
    this(keystoreFile, password, alias, password);
  }
  
  public PAdESSignerBox(String keystoreFile, String password, String alias, String passwordKey)
    throws Exception
  {
    if(keystoreFile == null || keystoreFile.length() == 0) {
      return;
    }
    if(password == null || password.length() == 0) {
      return;
    }
    if(alias == null || alias.length() == 0) {
      return;
    }
    if(passwordKey == null || passwordKey.length() == 0) {
      passwordKey = password;
    }
    
    int iFileSep = keystoreFile.indexOf('/');
    if(iFileSep < 0) iFileSep = keystoreFile.indexOf('\\');
    InputStream is = null;
    if(iFileSep < 0) {
      URL url = Thread.currentThread().getContextClassLoader().getResource(keystoreFile);
      if(url == null) return;
      is = url.openStream();
    }
    else {
      is = new FileInputStream(keystoreFile);
    }    
    if(is == null) return;
    
    Security.addProvider(new BouncyCastleProvider());
    
    KeyStore keyStore = null;
    if(keystoreFile.endsWith(".p12")) {
      keyStore = KeyStore.getInstance("PKCS12", "BC");
    }
    else {
      keyStore = KeyStore.getInstance("JKS");
    }
    keyStore.load(is, password.toCharArray());
    
    certificate = keyStore.getCertificate(alias);
    Key key     = keyStore.getKey(alias, passwordKey.toCharArray());
    if(key instanceof PrivateKey) {
      privateKey = (PrivateKey) key;
    }
    else {
      System.err.println(alias + " key is not a PrivateKey");
    }
    
    certChain = keyStore.getCertificateChain(alias);
    
    initCompleted = certificate != null && privateKey != null;
  }
  
  public PAdESSignerBox(Certificate certificate, PrivateKey privateKey, Certificate[] certChain)
    throws Exception
  {
    this.certificate = certificate;
    this.privateKey  = privateKey;
    this.certChain   = certChain;
    
    Security.addProvider(new BouncyCastleProvider());
    
    initCompleted = certificate != null && privateKey != null;
  }
  
  public
  PrivateKey getPrivateKey()
    throws Exception
  {
    if(privateKey instanceof PrivateKey) {
      return (PrivateKey) privateKey;
    }
    return null;
  }
  
  public
  X509Certificate getX509Certificate()
    throws Exception
  {
    if(certificate instanceof X509Certificate) {
      return (X509Certificate) certificate;
    }
    return null;
  }
  
  public
  String sign(String sourceFile, String outputFile, String creator, String contact, String reason, String location)
    throws Exception
  {
    if(!initCompleted) throw new Exception("Init not completed. Check keystore.");
    
    PDDocument pdDocument = PDDocument.load(new File(sourceFile));
    
    PDSignature signature = new PDSignature();
    signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
    signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
    signature.setName(creator);
    signature.setContactInfo(contact);
    signature.setLocation(location);
    signature.setReason(reason);
    
    pdDocument.addSignature(signature, this, new SignatureOptions());
    
    pdDocument.saveIncremental(new FileOutputStream(outputFile));
    pdDocument.close();
    
    return outputFile;
  }
  
  public
  OutputStream sign(InputStream sourceFile, OutputStream outputFile, String creator, String contact, String reason, String location)
    throws Exception
  {
    if(!initCompleted) throw new Exception("Init not completed. Check keystore.");
    
    PDDocument pdDocument = PDDocument.load(sourceFile);
    
    PDSignature signature = new PDSignature();
    signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
    signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
    signature.setName(creator);
    signature.setContactInfo(contact);
    signature.setLocation(location);
    signature.setReason(reason);
    
    pdDocument.addSignature(signature, this, new SignatureOptions());
    
    pdDocument.saveIncremental(outputFile);
    pdDocument.close();
    
    return outputFile;
  }
  
  public
  byte[] sign(byte[] sourceFile, String creator, String contact, String reason, String location)
    throws Exception
  {
    if(!initCompleted) throw new Exception("Init not completed. Check keystore.");
    
    ByteArrayInputStream bais = new ByteArrayInputStream(sourceFile);
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    
    sign(bais, baos, creator, contact, reason, location);
    
    return baos.toByteArray();
  }
  
  // org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface
  @Override
  public 
  byte[] sign(InputStream is) 
    throws IOException
  {
    if(is == null) return null;
    byte[] content = null;
    try {
      int n;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buff = new byte[1024];
      while((n = is.read(buff)) > 0) baos.write(buff, 0, n);
      content = baos.toByteArray();
    }
    finally {
      if(is != null) try{ is.close(); } catch(Exception ex) {}
    }
    if(content == null) content = new byte[0];
    
    try {
      ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(privateKey);
      CMSTypedData msg = new CMSProcessableByteArray(content);
      CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
      gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider("BC").build()).build(signer, (X509Certificate) certificate));
      gen.addCertificates(new JcaCertStore(Arrays.asList(certificate)));
      CMSSignedData sigData = gen.generate(msg, false);
      return sigData.getEncoded();
    }
    catch(Exception ex) {
      throw new IOException(ex);
    }
  }
}
