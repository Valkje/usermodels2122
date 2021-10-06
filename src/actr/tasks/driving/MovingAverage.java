package actr.tasks.driving;

import java.util.LinkedList;

public class MovingAverage {
    /**
     * Relatively efficient simple moving average.
     * We will see whether this works for real-time.
     * Otherwise we should probably switch to an
     * exponential moving average - which should be
     * constant time complexity wise.
     * 
     * See: https://en.wikipedia.org/wiki/Moving_average
     */
    private int windowSize;
    private LinkedList<Double> buffer;
    private int currentBufferSize = 0;
    private double movingSum = 0.0f;
    private double currentValue = 0.0f;
    
    public MovingAverage(int size) {
        windowSize = size;
        buffer = new LinkedList<>();
    }

    public void update(double newSample) {
        /**
         * Three steps:
         * 1) update buffer
         * 2) update movingSum
         * 3) update currentValue
         */

         // First add to current sum!
         movingSum += newSample;
        
         // Now optionally remove first item from buffer and adapt sum
         if (currentBufferSize == windowSize) {
             movingSum -= buffer.removeFirst();
             currentBufferSize -= 1;
         }

         // Add new sample to buffer and adjust size accordingly
         buffer.addLast(newSample);
         currentBufferSize += 1;

         // this is the simple moving average
         currentValue = movingSum/currentBufferSize;
    }

    public double getCurrentValue() {
        // Gets latest moving average value
        return(currentValue);
    }
    
}
