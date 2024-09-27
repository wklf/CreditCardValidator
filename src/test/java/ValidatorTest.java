import Validator.CCValidator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValidatorTest {

    private final String VISA_JSON = "{\"result\":\"VISA\"}";
    private final String AMERICAN_EXPRESS_JSON = "{\"result\":\"AMERICAN_EXPRESS\"}";
    private final String MASTERCARD_JSON = "{\"result\":\"MASTERCARD\"}";

    @Test
    public void validVisa() {
        ArrayList<String> errors = new ArrayList<>();
        assertEquals(VISA_JSON, CCValidator.validateCard("J Dough","4012-8888-8888-1881","342","05/27", errors));
    }

    @Test
    public void validAmericanExpress() {
        ArrayList<String> errors = new ArrayList<>();
        assertEquals(AMERICAN_EXPRESS_JSON, CCValidator.validateCard("J Doe","3714 496353 98431","3422","05/27", errors));
    }

    @Test
    public void validMastercard() {
        ArrayList<String> errors = new ArrayList<>();
        assertEquals(MASTERCARD_JSON, CCValidator.validateCard("J Doe","5506 9004 9000 0436","342","05/27", errors));
    }

    @Test
    public void cardHasExpired() {
        ArrayList<String> errors = new ArrayList<>();
        assertEquals("{\"result\":\"Error, please se provided list.\",\"errors\":[\"Card has expired.\"]}",
                CCValidator.validateCard("J Dough","4012-8888 8888-1881","342","11/18", errors));
    }

    @Test
    public void incorrectCardNumberMastercard() {
        ArrayList<String> errors = new ArrayList<>();
        assertEquals("{\"result\":\"Error, please se provided list.\",\"errors\":[\"Incorrect checksum for card number.\"]}",
                CCValidator.validateCard("J Dough","5506 9304 9000 0436","342","05/27", errors));
    }

    @Test
    public void incorrectCVCMastercard() {
        ArrayList<String> errors = new ArrayList<>();
        assertEquals("{\"result\":\"Error, please se provided list.\",\"errors\":[\"Incorrect CVC length for the card provider.\"]}",
                CCValidator.validateCard("J Dough","5506 9004 9000 0436","3432","05/27", errors));
    }

    @Test
    public void noIdentifiedCardProvider() {
        ArrayList<String> errors = new ArrayList<>();
        assertEquals("{\"result\":\"Error, please se provided list.\",\"errors\":[\"Card provider can not be " +
                        "identified as Visa, Mastercard or American Express.\",\"Can not validate CVC due to missing card provider.\"]}",
                CCValidator.validateCard("J Doe","6011 0009 9013 9424","332","05/27", errors));
    }

    @Test
    public void incorrectDataInName() {
        ArrayList<String> errors = new ArrayList<>();
        assertEquals("{\"result\":\"Error, please se provided list.\",\"errors\":[\"Potential card data found in name field.\"]}",
                CCValidator.validateCard("J 22Doe","5506 9004 9000 0436","332","05/27", errors));
    }

    @Test
    public void invalidChecksumDetected() {
        ArrayList<String> errors = new ArrayList<>();
        CCValidator.validateChecksum("4012888881181881",errors);
        assertEquals("Incorrect checksum for card number.", errors.get(0));
    }

    @Test
    public void validChecksumDetected() {
        ArrayList<String> errors = new ArrayList<>();
        CCValidator.validateChecksum("4012888888881881",errors);
        assert(errors.isEmpty());
    }
}
