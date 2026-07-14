package de.usd.cstchef.operations.signature;

import burp.api.montoya.core.ByteArray;
import de.usd.cstchef.operations.Operation;
import de.usd.cstchef.operations.OperationCategory;
import de.usd.cstchef.operations.Operation.OperationInfos;
import de.usd.cstchef.view.ui.VariableTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.swing.JComboBox;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@OperationInfos(name = "ECDSA Verify", category = OperationCategory.SIGNATURE, description = "Verify a signature with a PEM encoded EC key")
public class ECDSAVerify extends Operation implements ActionListener, DocumentListener {

    private VariableTextField publicKey;
    private VariableTextField signature;
    private JComboBox<DigestAlgorithmItem> digestAlgorithms;
    private String currentDigest;
    private String currentAlgorithm;
    private boolean hasError= false;

    @Override
    protected ByteArray perform(ByteArray input) throws Exception {
        if(this.hasError) {			
            throw new IllegalArgumentException("Key not valid");
        }
        
        MessageDigest digest = MessageDigest.getInstance(currentDigest);
        byte[] hash = digest.digest(input.getBytes());
        byte[] signatureBytes = Base64.getDecoder().decode(signature.getText());
        Signature ecdsa = Signature.getInstance(currentAlgorithm);
        ecdsa.initVerify(createPublicKey(publicKey.getText()));
        ecdsa.update(hash);
        boolean verifyResult = ecdsa.verify(signatureBytes);
        return factory.createByteArray(String.valueOf(verifyResult));
    }

    public void createUI() {
    	this.publicKey = new VariableTextField();
    	this.publicKey.getDocument().addDocumentListener(this);
    	this.publicKey.setText("");
        this.addUIElement("ECDSA Public Key (PEM)", this.publicKey);
        
        this.signature = new VariableTextField();
    	this.signature.getDocument().addDocumentListener(this);
    	this.signature.setText("");
        this.addUIElement("Signature", this.signature);
        
        this.digestAlgorithms = new JComboBox();
        this.digestAlgorithms.addItem(DigestAlgorithmItem.SHA1);
        this.digestAlgorithms.addItem(DigestAlgorithmItem.SHA256);
        this.digestAlgorithms.addItem(DigestAlgorithmItem.SHA384);
        this.digestAlgorithms.addItem(DigestAlgorithmItem.SHA512);
        this.digestAlgorithms.addActionListener(this);
        this.digestAlgorithms.setSelectedIndex(0);
        this.addUIElement("Message Digest Algorithm", digestAlgorithms);
    }
    
    private void reconfigureAlgorithm() {
    	try {
            this.currentDigest = ((DigestAlgorithmItem) this.digestAlgorithms.getSelectedItem()).getDigestName();
            this.currentAlgorithm = ((DigestAlgorithmItem) this.digestAlgorithms.getSelectedItem()).getAlgorithm();
            this.hasError=false;
        }
        catch(Exception ex) {
            this.hasError=true;
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        this.reconfigureAlgorithm();
        if(publicKey != null || signature != null) {
            this.notifyChange();
        }
    }

    private enum DigestAlgorithmItem {
        SHA1("SHA-1", "SHA1withECDSA"),
        SHA256("SHA-256", "SHA256withECDSA"),
        SHA384("SHA-384", "SHA384withECDSA"),
        SHA512("SHA-512", "SHA512withECDSA");
        
        private String digestName, signatureAlgorithm;
        private DigestAlgorithmItem(String digestName, String signatureAlgorithm) {
            this.digestName = digestName;
            this.signatureAlgorithm = signatureAlgorithm;
        }

        public String getDigestName() {
            return this.digestName;
        }
        
        public String getAlgorithm() {
            return this.signatureAlgorithm;
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        this.reconfigureAlgorithm();		
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        this.reconfigureAlgorithm();		
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        this.reconfigureAlgorithm();
    }
    
    private static PublicKey createPublicKey(String input) throws Exception {
        String publicKeyPem = input.replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace(System.lineSeparator(), "")
            .replaceAll("\\s+","");
        byte [] publicKeyPemBytes = Base64.getDecoder().decode(publicKeyPem);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyPemBytes);
        KeyFactory keyfactory = KeyFactory.getInstance("EC");
        PublicKey pubKey = keyfactory.generatePublic(keySpec);
        return pubKey;
    }
}