public class LogicalClock {

    private int time = 0;

    public synchronized int increment() {
        time++;
        return time;
    }

    public synchronized void update(int received) {
        time = Math.max(time, received);
    }

    public synchronized int getTime() {
        return time;
    }
}