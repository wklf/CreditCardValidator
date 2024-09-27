package Validator;

public enum CardProvider {
    VISA(16,3),
    MASTERCARD(16,3),
    AMERICAN_EXPRESS(15,4);

    private int cvcLength;
    private int numberLength;

    CardProvider (int numberLength, int cvcLength) {
        this.numberLength = numberLength;
        this.cvcLength = cvcLength;
    }

    public int getCvcLength() {
        return cvcLength;
    }

    public int getNumberLength() {
        return numberLength;
    }
}
