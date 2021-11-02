import queue
import math


class MovingRMSE():

    def __init__(self):
        """
        * Calculates the cumulative variance
        * of differences. Differences are directly calculated
        * in the update method.
        * 
        * Based on: https://en.wikipedia.org/wiki/Variance
        * and see: https://en.wikipedia.org/wiki/Moving_average for definition of cumulative
        * 
        * This provides us with an efficient tool to essentially calculate the
        * root mean squared error between the long-term
        * moving average of the pupil size (our "prediction") and the short-term
        * trend (our "observation"). 
        * 
        * See: https://en.wikipedia.org/wiki/Mean_squared_error
        *
        * Note that we do not use raw observations as described in the wiki article but essentially
        * replace them with predictions from the short-term trend as well.
        * This makes the system less sensitive to outliers in the pupil size.
        * In earlier versions we also experimented with a simple moving variance here
        * (updated for a window like the averages). But the resulting decision boundaries
        * were too sensitive.
        * 
        * Monitoring short-term and long-term changes in the pupil
        * size to detect changes in demand was suggested in Minadakis & Lohan (2018)
        * which motivated us to test the simple threshold decision rule
        * mentioned above.
        """
        self.size = 0
        self.movingNum = 0
        self.currentValue = 0
        self.history = []
    

    def update(self,y_hat,y):
        """
        * 1) Calculate power of difference
        * 2) Update moving Numerator
        * 4) Update currentValue
        """
        difference = y_hat - y
        squareDiff = difference**2

        # First add to current num!
        self.movingNum += squareDiff

        # Adjust size accordingly
        self.size += 1

        # this is the cumulative variance at any point
        """
        Save-guard for division by 0
        """
        if self.size > 1:
            self.currentValue = self.movingNum/(self.size - 1)
        else:
            self.currentValue = 0

        # For plotting and analysis
        self.history.append(self.currentValue)
    
    def getCurrentValue(self):
        return math.sqrt(self.currentValue)
