package expectj;

/**
 * This interface represents the events triggered by Timer class
 */

public interface TimerEventListener {

    /**
     * This method represents the time-out event triggered by Timer.
     */
    public void timerTimedOut();

    /**
     * This method is invoked by the Timer, when the timer thread
     * receives an interrupted exception.
     * @param reason Why we were interrupted.
     */
    public void timerInterrupted(InterruptedException reason);

}
