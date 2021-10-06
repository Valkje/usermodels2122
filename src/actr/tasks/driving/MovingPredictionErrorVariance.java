package actr.tasks.driving;

import java.util.LinkedList;
import java.lang.Math;

public class MovingPredictionErrorVariance {
    /**
     * Calculates the variance for a moving window
     * of differences. Differences are directly calculated
     * in the update method.
     * 
     * See: https://en.wikipedia.org/wiki/Variance
     * 
     * This provides us with an efficient tool to calculate the
     * (root, if desired) mean squared error between the long-term
     * moving average of the pupil size ("prediction") and the actual
     * samples obtained from the eye-tracker ("observations").
     * 
     * See: https://en.wikipedia.org/wiki/Mean_squared_error
     * 
     * We can use this RMSE as a simple decision threshold:
     * e.g. if short-term pupil size exceeds long-term pupil size + a * RMSE
     * increase the level of automation. 'a' here allows us
     * to control the sensitivity of our AAS.
     * 
     * Monitoring short-term and long-term changes in the pupil
     * size to detect changes in demand was suggested in Minadakis & Lohan (2018)
     * which motivated us to test the simple threshold decision rule
     * mentioned above.
     */

     private int windowSize;
     private LinkedList<Double> buffer;
     private int currentBufferSize = 0;
     private double movingNum = 0.0f;
     private double currentValue = 0.0f;
     
     
     public MovingPredictionErrorVariance(int size) {
         windowSize = size;
         buffer = new LinkedList<>();
     }

     public void update(double y_hat, double y) {
         /**
          * Steps:
          * 1) Calculate power of difference
          * 2) Update moving Numerator
          * 3) Update buffer
          * 4) Update currentValue
          */

          double difference = y_hat - y;
          double squareDiff = Math.pow(difference, 2);

          // Update Numerator
          movingNum += squareDiff;

          // Maintain fixed buffer size and adjust size as well as numerator.
          if (currentBufferSize == windowSize) {
              movingNum -= buffer.removeFirst();
              currentBufferSize -= 1;
          }

          // push new sample to queue
          buffer.addLast(squareDiff);

          // Calculate variance (with sample bias correction)
          // See: https://en.wikipedia.org/wiki/Bessel%27s_correction
          currentValue = movingNum/(currentBufferSize - 1);
     }

     public double getCurrentValue() {
        return(currentValue);
     }
}
