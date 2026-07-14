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
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import javax.swing.JComboBox;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@OperationInfos(name = "ECDSA Sign", category = OperationCategory.SIGNATURE, description = "Sign a plaintext message with a PEM encoded EC key")
public class ECDSASign extends Operation implements ActionListener, DocumentListener {

    private VariableTextField privateKey;
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
        Signature signature = Signature.getInstance(currentAlgorithm);
        signature.initSign(createPrivateKey(privateKey.getText()));
        signature.update(hash);
        byte[] signatureBytes = signature.sign();
        String encodedSignature = Base64.getEncoder().encodeToString(signatureBytes);
        return factory.createByteArray(encodedSignature);
    }

    public void createUI() {
    	this.privateKey = new VariableTextField();
    	this.privateKey.getDocument().addDocumentListener(this);
    	this.privateKey.setText("");
        this.addUIElement("ECDSA Private Key (PEM)", this.privateKey);
        
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
        if(privateKey != null) {
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
    
    private static PrivateKey createPrivateKey(String input) throws Exception {
        String privateKeyPem = input.replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace(System.lineSeparator(), "")
            .replaceAll("\\s+","");
        byte [] privateKeyPemBytes = Base64.getDecoder().decode(privateKeyPem);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyPemBytes);
        KeyFactory keyfactory = KeyFactory.getInstance("EC");
        PrivateKey privateKey = keyfactory.generatePrivate(keySpec);
        return privateKey;
    }
}