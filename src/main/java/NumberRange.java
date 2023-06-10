public class NumberRange {
    public static final int MIN = 0;
    public static final int MAX = 1;
    int[] range = new int[2];

    NumberRange(int min, int max) {
        range[MIN] = min;
        range[MAX] = max;
    }

    public int getMin() {
        return range[MIN];
    }

    public int getMax() {
        return range[MAX];
    }

}
