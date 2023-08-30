package de.usd.cstchef.operations.setter;

import java.util.Arrays;

import burp.BurpUtils;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.requests.HttpRequest;
import de.usd.cstchef.operations.Operation;
import de.usd.cstchef.operations.OperationCategory;
import de.usd.cstchef.operations.Operation.OperationInfos;
import de.usd.cstchef.view.ui.FormatTextField;

@OperationInfos(name = "HTTP Body", category = OperationCategory.SETTER, description = "Set the HTTP body to the specified value.")
public class HttpSetBody extends Operation {

    private FormatTextField replacementTxt;

    @Override
    protected ByteArray perform(ByteArray input) throws Exception {
        MontoyaApi api = BurpUtils.getInstance().getApi();
        int bodyOffset = HttpRequest.httpRequest(input).bodyOffset();

        ByteArray noBody = input.subArray(0, bodyOffset);
        ByteArray newBody = replacementTxt.getText();
        ByteArray newRequest = ByteArray.byteArray(noBody.length() + newBody.length());
        System.arraycopy(noBody, 0, newRequest, 0, noBody.length());
        System.arraycopy(newBody, 0, newRequest, noBody.length(), newBody.length());

        return newRequest;
    }

    @Override
    public void createUI() {
        this.replacementTxt = new FormatTextField();
        this.addUIElement("Body", this.replacementTxt);
    }

}
