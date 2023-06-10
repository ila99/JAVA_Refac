public class Performance {
    private String playID;
    private Play play;
    private int audience;

    private long amount;

    private long volumeCredits;

    public String getPlayID() {
        return playID;
    }
    public void setPlayID(String playID) {
        this.playID = playID;
    }
    public Play getPlay() {
        return play;
    }

    public void setPlay(Play play) {
        this.play = play;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getVolumeCredits() {
        return volumeCredits;
    }

    public void setVolumeCredits(long volumeCredits) {
        this.volumeCredits = volumeCredits;
    }

    public int getAudience() {
        return audience;
    }
    public void setAudience(int audience) {
        this.audience = audience;
    }
}
