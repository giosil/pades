package org.dew.pades;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collection;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;

import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.ICrlClient;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.IOcspClient;
import com.itextpdf.signatures.ITSAClient;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PrivateKeySignature;

public 
class PAdESSigner
{
  protected Certificate   certificate;
  protected PrivateKey    privateKey;
  protected Certificate[] certChain;
  
  protected boolean initCompleted;
  
  public PAdESSigner(String keystoreFile, String password, String alias)
      throws Exception
  {
    this(keystoreFile, password, alias, password);
  }
  
  public PAdESSigner(String keystoreFile, String password, String alias, String passwordKey)
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
  
  public PAdESSigner(Certificate certificate, PrivateKey privateKey, Certificate[] certChain)
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
    
    PdfReader reader = new PdfReader(sourceFile);
    
    StampingProperties sp = new StampingProperties();
    sp.useAppendMode();
    
    PdfSigner signer = new PdfSigner(reader, new FileOutputStream(outputFile), sp);
    signer.setCertificationLevel(PdfSigner.CERTIFIED_NO_CHANGES_ALLOWED);
    
    IExternalSignature pks = new PrivateKeySignature(privateKey, DigestAlgorithms.SHA256, "BC");
    
    IExternalDigest digest = new BouncyCastleDigest();
    
    PdfSignatureAppearance appearance = signer.getSignatureAppearance();
    appearance.setReason(reason);
    appearance.setLocation(location);
    appearance.setContact(contact);
    appearance.setSignatureCreator(creator);
    
    Collection<ICrlClient> crlList = null;
    IOcspClient ocspClient = null;
    ITSAClient  tsaClient  = null;
    
    signer.setFieldName("Signature");
    signer.signDetached(digest, pks, certChain, crlList, ocspClient, tsaClient, 0, PdfSigner.CryptoStandard.CMS);
    
    return outputFile;
  }
  
  public
  OutputStream sign(InputStream sourceFile, OutputStream outputFile, String creator, String contact, String reason, String location)
    throws Exception
  {
    if(!initCompleted) throw new Exception("Init not completed. Check keystore.");
    
    PdfReader reader = new PdfReader(sourceFile);
    
    StampingProperties sp = new StampingProperties();
    sp.useAppendMode();
    
    PdfSigner signer = new PdfSigner(reader, outputFile, sp);
    signer.setCertificationLevel(PdfSigner.CERTIFIED_NO_CHANGES_ALLOWED);
    
    IExternalSignature pks = new PrivateKeySignature(privateKey, DigestAlgorithms.SHA256, "BC");
    
    IExternalDigest digest = new BouncyCastleDigest();
    
    PdfSignatureAppearance appearance = signer.getSignatureAppearance();
    appearance.setReason(reason);
    appearance.setLocation(location);
    appearance.setContact(contact);
    appearance.setSignatureCreator(creator);
    
    Collection<ICrlClient> crlList = null;
    IOcspClient ocspClient = null;
    ITSAClient  tsaClient  = null;
    
    signer.setFieldName("Signature");
    signer.signDetached(digest, pks, certChain, crlList, ocspClient, tsaClient, 0, PdfSigner.CryptoStandard.CMS);
    
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
}
