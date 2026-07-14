package de.usd.cstchef.operations.signature;

import burp.api.montoya.core.ByteArray;
import de.usd.cstchef.operations.Operation;
import de.usd.cstchef.operations.OperationCategory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;
import javax.swing.JComboBox;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;

@Operation.OperationInfos(name = "ECDSA Generate Key Pair", category = OperationCategory.SIGNATURE, description = "Generate an ECDSA key pair in PEM encoded format.")
public class ECDSAKeyGen extends Operation implements ActionListener, DocumentListener {

    private JComboBox<EllipticCurveItem> ellipticCurve;
    private String currentCurve;
    private boolean hasError= false;

    @Override
    protected ByteArray perform(ByteArray input) throws Exception {
        if(this.hasError) {			
            throw new IllegalArgumentException("Key not valid");
        }
        
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(currentCurve);
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
        keyGen.initialize(ecSpec, new SecureRandom());
        KeyPair pair = keyGen.generateKeyPair();
        ECPrivateKey privateKey = (ECPrivateKey) pair.getPrivate();
        ECPublicKey publicKey = (ECPublicKey) pair.getPublic();
        String result =
            "-----BEGIN PRIVATE KEY-----\n" + Base64.getEncoder().encodeToString(privateKey.getEncoded()) + "\n-----END PRIVATE KEY-----\n\n" + 
            "-----BEGIN PUBLIC KEY-----\n" + Base64.getEncoder().encodeToString(publicKey.getEncoded()) + "\n-----END PUBLIC KEY-----";
        return factory.createByteArray(result);
    }

    @Override
    public void createUI() {
        this.ellipticCurve = new JComboBox();
        this.ellipticCurve.addItem(EllipticCurveItem.P224);
        this.ellipticCurve.addItem(EllipticCurveItem.P256);
        this.ellipticCurve.addItem(EllipticCurveItem.P384);
        this.ellipticCurve.addItem(EllipticCurveItem.P521);
        this.ellipticCurve.addActionListener(this);
        this.ellipticCurve.setSelectedIndex(0);
        this.addUIElement("Elliptic Curve ", ellipticCurve);
    }
    
    private void reconfigureCurve() {
    	try {
            this.currentCurve = ((EllipticCurveItem) this.ellipticCurve.getSelectedItem()).toString();
            this.hasError=false;
        }
        catch(Exception ex) {
            this.hasError=true;
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        this.reconfigureCurve();
    }

    private enum EllipticCurveItem {	
        P224("P-224"), P256("P-256"), P384("P-384"), P521("P-521");
        private String name;
        private EllipticCurveItem(String name) {
            this.name = name;
        }

        @Override
        public String toString(){
            return name;
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        this.reconfigureCurve();		
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        this.reconfigureCurve();		
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        this.reconfigureCurve();
    }
}