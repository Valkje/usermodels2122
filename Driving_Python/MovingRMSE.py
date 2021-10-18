import queue
import math


class MovingRMSE():

    def __init__(self,size):
        """
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
        """
        self.windowSize = size
        self.buffer = queue.Queue(maxsize= size + 1)
        self.currentBufferSize = 0
        self.movingNum = 0
        self.currentValue = 0
        self.history = []
    

    def update(self,y_hat,y):
        """
        * 1) Calculate power of difference
        * 2) Update moving Numerator
        * 3) Update buffer
        * 4) Update currentValue
        """
        difference = y_hat - y
        squareDiff = difference**2

        # First add to current sum!
        self.movingNum += squareDiff

        # Now optionally remove first item from buffer and adapt sum
        if (self.currentBufferSize == self.windowSize):
            self.movingNum -= self.buffer.get()
            self.currentBufferSize -= 1
        

        # Add new sample to buffer and adjust size accordingly
        self.buffer.put(squareDiff)
        self.currentBufferSize += 1

        # this is the simple moving variance
        """
        Save-guard for division by 0
        """
        if self.currentBufferSize > 1:
            self.currentValue = self.movingNum/(self.currentBufferSize - 1)
        else:
            self.currentValue = 0

        # For plotting and analysis
        self.history.append(self.currentValue)
    
    def getCurrentValue(self):
        return math.sqrt(self.currentValue)
