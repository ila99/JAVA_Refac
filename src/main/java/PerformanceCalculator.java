public class PerformanceCalculator {
    protected Performance performance;
    protected Play play;
    PerformanceCalculator(Performance aPerformance, Play play) {
        this.performance = aPerformance;
        this.play = play;
    }

    protected Play getPlay() {
        return play;
    }

    protected long getAmount() {
        throw new Error("[ERROR] 서브클래스에서 처리함");
    }

    protected long getVolumeCredit() {
        return Math.max(performance.getAudience() - 30, 0);
    }
}
