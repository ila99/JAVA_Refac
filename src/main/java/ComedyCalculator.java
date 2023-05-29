public class ComedyCalculator extends PerformanceCalculator {

    ComedyCalculator(Performance aPerformance, Play play) {
        super(aPerformance, play);
    }
    public long getAmount() {    // 매개변수 삭제
        long result = 0;
        result = 30000;
        if(performance.getAudience() > 20) {
            result += 10000 + 500 * (performance.getAudience() - 20);
        }
        result += 300 * performance.getAudience();

        return result;
    }

    public long getVolumeCredit() {
        return (long)(super.getVolumeCredit()+Math.floor(performance.getAudience() / 5));
    }
}
