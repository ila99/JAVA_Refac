public enum PlayType {
    TRAGEDY("tragedy"), COMEDY("comedy");
    private String value;

    PlayType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
