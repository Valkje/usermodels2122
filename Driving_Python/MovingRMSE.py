import queue
import math


class MovingRMSE():

    def __init__(self):
        """
        * Calculates the cumulative variance
        * of differences. Differences are directly calculated
        * in the update method.
        * 
        * See: https://en.wikipedia.org/wiki/Variance
        * and see: https://en.wikipedia.org/wiki/Moving_average for definition of cumulative
        * 
        * This provides us with an efficient tool to calculate the
        * (root, if desired) mean squared error between the long-term
        * moving average of the pupil size ("observation") and the short-term
        * trend: ("prediction").
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
