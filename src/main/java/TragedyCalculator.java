public class TragedyCalculator extends PerformanceCalculator {
    TragedyCalculator(Performance aPerformance, Play play) {
        super(aPerformance, play);
    }

    public long getAmount() {    // 매개변수 삭제
        long result = 0;
        result = 40000;
        if(performance.getAudience() > 30) {
            result += 1000 * (performance.getAudience() - 30);
        }
        return result;
    }
}
