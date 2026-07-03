package de.usd.cstchef.operations.extractors;

import javax.swing.JTextField;

import burp.api.montoya.core.ByteArray;
import de.usd.cstchef.Utils.MessageType;
import de.usd.cstchef.operations.Operation;
import de.usd.cstchef.operations.Operation.OperationInfos;
import de.usd.cstchef.operations.OperationCategory;

@OperationInfos(
    name = "Get HTTP JSON",
    category = OperationCategory.EXTRACTORS,
    description = """
            This operation uses JsonPath expressions. More information here: https://github.com/json-path/JsonPath

            ## Operators
            
            $               The root element to query. This starts all path expressions.
            @               The current node being processed by a filter predicate.
            *               Wildcard. Available anywhere a name or numeric are required.
            ..              Deep scan. Available anywhere a name is required.
            .<name>             Dot-notated child
            ['<name>' (, '<name>')]             Bracket-notated child or children
            [<number> (, <number>)]             Array index or indexes
            [start:end]             Array slice operator
            [?(<expression>)]               Filter expression. Expression must evaluate to a boolean value.

            ## Functions

            min()                          Provides the min value of an array of numbers	Double
            max()                          Provides the max value of an array of numbers	Double
            avg()                          Provides the average value of an array of numbers	Double
            stddev()                           Provides the standard deviation value of an array of numbers	Double
            length()                           Provides the length of an array	Integer
            sum()                          Provides the sum value of an array of numbers	Double
            keys()                         Provides the property keys (An alternative for terminal tilde ~)	Set<E>
            concat(X)                          Provides a concatinated version of the path output with a new item	like input
            append(X)                          add an item to the json path output array	like input
            first()                                Provides the first item of an array	Depends on the array
            last()                         Provides the last item of an array	Depends on the array
            index(X)                           Provides the item of an array of index: X, if the X is negative, take from backwards	Depends on the array

            ## Filter Operators

            ==	            left is equal to right (note that 1 is not equal to '1')
            !=	            left is not equal to right
            <	            left is less than right
            <=	            left is less or equal to right
            >	            left is greater than right
            >=	            left is greater than or equal to right
            =~	            left matches regular expression [?(@.name =~ /foo.*?/i)]
            in	            left exists in right [?(@.size in ['S', 'M'])]
            nin	            left does not exists in right
            subsetof	            left is a subset of right [?(@.sizes subsetof ['S', 'M', 'L'])]
            anyof	            left has an intersection with right [?(@.sizes anyof ['M', 'L'])]
            noneof	            left has no intersection with right [?(@.sizes noneof ['M', 'L'])]
            size	            size of left (array or string) should match right
            empty	            left (array or string) should be empty
            """
)
public class HttpJsonExtractor extends Operation {

    protected JTextField fieldTxt;

    @Override
    protected ByteArray perform(ByteArray input) throws Exception {

        MessageType messageType = parseMessageType(input);

        String keyName = fieldTxt.getText();
        if( keyName.equals("") )
            return input;

        JsonExtractor extractor = new JsonExtractor(keyName);
        if(messageType == MessageType.REQUEST){
            return checkNull(extractor.perform(factory.createHttpRequest(input).body()));
        }
        else if(messageType == MessageType.RESPONSE){
            return checkNull(extractor.perform(factory.createHttpResponse(input).body()));
        }
        else{
            return parseRawMessage(input);
        }
    }

    @Override
    public void createUI() {
        this.fieldTxt = new JTextField();
        this.addUIElement("Field", this.fieldTxt);
    }
}
