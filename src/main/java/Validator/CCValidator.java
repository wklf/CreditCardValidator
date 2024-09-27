package Validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.javalin.Javalin;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.YearMonth;
import java.util.ArrayList;

public class CCValidator {

    public static void main(String[] args) {
        var app = Javalin.create();
        app.post("/validate", ctx -> {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(ctx.body());

            ArrayList<String> validationErrors = new ArrayList<>(validateInputFields(jsonNode));
            String result = null;

            if (validationErrors.isEmpty()) { // Only proceed with the validation if all fields are present
                result = validateCard(
                        jsonNode.get("name").asText(),
                        jsonNode.get("number").asText(),
                        jsonNode.get("cvc").asText(),
                        jsonNode.get("date").asText(),
                        validationErrors
                );
            } else {
                result = "{\"result\":\"Error, please se provided list.\",\"errors\":" +
                        objectMapper.writeValueAsString(validationErrors + "}");
            }

            ctx.json(result);
        });
        app.start(4001);
    }

    public static String validateCard(String name, String number, String cvc, String date, ArrayList<String> validationErrors) {
        StringBuilder jsonResult = new StringBuilder("{\"result\":");

        String noSeparatorNumber = number.replace("-", "");
        noSeparatorNumber = noSeparatorNumber.replace(" ", "");

        if (validationErrors.isEmpty()) {
            CardProvider provider = determineProvider(noSeparatorNumber, validationErrors);
            if (provider == null) validationErrors.add("Card provider can not be identified as Visa, Mastercard or American Express.");

            validateName(name, validationErrors);
            validateCardExpiration(date, validationErrors);
            validateChecksum(noSeparatorNumber, validationErrors);
            validateCVC(cvc, provider, validationErrors);

            if (validationErrors.isEmpty()) {
                jsonResult.append("\"").append(provider.name()).append("\"}");
                return jsonResult.toString();
            }
        }

            ObjectMapper objectMapper = new ObjectMapper();

            try {
                jsonResult.append("\"Error, please se provided list.\",")
                        .append("\"errors\":")
                        .append(objectMapper.writeValueAsString(validationErrors))
                        .append("}");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

        return jsonResult.toString();
    }

    private static ArrayList<String> validateInputFields(JsonNode body) {
        ArrayList<String> validationErrors = new ArrayList<>();

        if (body.get("name") == null || body.get("name").asText().isBlank()) {
            validationErrors.add("Name field is empty or missing.");
        }

        if (body.get("number") == null || body.get("number").asText().isBlank()) {
            validationErrors.add("Number field is empty or missing.");
        }

        if (body.get("cvc") == null || body.get("cvc").asText().isBlank()) {
            validationErrors.add("CVC field is empty or missing.");
        }

        if (body.get("date") == null || body.get("date").asText().isBlank()) {
            validationErrors.add("Date field is empty or missing.");
        }

        return validationErrors;
    }

    private static void validateName(String name, ArrayList<String> validationErrors) {
        for (char c: name.toCharArray()) {
            if (Character.isDigit(c)) {
                validationErrors.add("Potential card data found in name field.");
                break;
            };
        }
    }

    private static void validateCVC(String cvc, CardProvider provider, ArrayList<String> validationErrors) {
        if (provider == null) {
            validationErrors.add("Can not validate CVC due to missing card provider.");
            return;
        }

        for (char c: cvc.toCharArray()) {
            if (!Character.isDigit(c)) {
                validationErrors.add("Non-digit found in CVC field");
                return;
            }
        }

        if (cvc.length() != provider.getCvcLength()) validationErrors.add("Incorrect CVC length for the card provider.");
    }

    private static void validateCardExpiration(String date, ArrayList<String> validationErrors) {
        if (date.length() != 5 || date.charAt(2) != '/') {
            validationErrors.add("Incorrect format in date field, can not determine if expired.");
            return;
        }

        if (!Character.isDigit(date.charAt(0)) ||
                !Character.isDigit(date.charAt(1)) ||
                !Character.isDigit(date.charAt(3)) ||
                !Character.isDigit(date.charAt(4))) {
            validationErrors.add("Non-digit found in month or year date field, can not determine if expired.");
            return;
        }

        int month = Integer.parseInt(date.substring(0,2));
        int year = Integer.parseInt("20" + date.substring(3));
        YearMonth expirationDate = YearMonth.of(year, month);;
        YearMonth current = YearMonth.now();

        if (expirationDate.compareTo(current) < 0) validationErrors.add("Card has expired.");
    }

    private static CardProvider determineProvider(String number, ArrayList<String> validationErrors) {
        CardProvider provider = null;

        for (char c: number.toCharArray()) {
            if (!Character.isDigit(c)) {
                validationErrors.add("Non-digit found in credit card number, can not determine provider.");
                break;
            }
        }

        if (number.length() < 15) {
            validationErrors.add("Credit card length is too short, can not determine provider.");
        } else {
            String firstTwo = number.substring(0, 2);
            String firstFour = number.substring(0, 4);

            if (number.charAt(0) == '4' && number.length() == CardProvider.VISA.getNumberLength()) {
                provider = CardProvider.VISA;
            } else if (number.length() == CardProvider.AMERICAN_EXPRESS.getNumberLength() &&
                    (firstTwo.equals("34") || firstTwo.equals("37"))) {
                provider = CardProvider.AMERICAN_EXPRESS;
            } else if (number.length() == CardProvider.MASTERCARD.getNumberLength() &&
                    (((Integer.parseInt(firstTwo) >= 51) && (Integer.parseInt(firstTwo) <= 55)) ||
                            ((Integer.parseInt(firstFour) >= 2221) && Integer.parseInt(firstFour) <= 2720))) {
                provider = CardProvider.MASTERCARD;
            }
        }

        return provider;
    }

    /*
    Credit card number validated using the Luhn's Algorithm.
     */
    public static void validateChecksum(String number, ArrayList<String> validationErrors) {
        int checkDigit = Integer.parseInt(number.substring(number.length() - 1));
        int nDigits =  number.length();
        int sum = 0;
        boolean isSecond = true;

        for (int i = nDigits - 2; i >= 0; i--) {
            int d = number.charAt(i) - '0';

            if (isSecond) d = d * 2;

            sum += d / 10;
            sum += d % 10;

            isSecond = !isSecond;
        }

        if ((sum + checkDigit) % 10 != 0) {
            validationErrors.add("Incorrect checksum for card number.");
        }
    }

}

