import queue
import math


class MovingRMSE():

    def __init__(self):
        """
        * Calculates the cumulative root mean square error.
        * 
        * Based on: https://en.wikipedia.org/wiki/Mean_squared_error
        * and see: https://en.wikipedia.org/wiki/Root-mean-square_deviation for RMSE definition
        * and see: https://en.wikipedia.org/wiki/Moving_average for definition of cumulative
        * note, that the MSE is essentially a variance: https://en.wikipedia.org/wiki/Variance
        * 
        * As discussed in the MSE article and our wiki, the RMSE/MSE is usually calculated
        * between a prediction and some observation. Note that we replace raw observations with
        * the short-term trend as described in our wiki.
        * This makes the system less sensitive to outliers in the pupil size.
        *
        * In earlier versions we also experimented with a simple moving variance here
        * (updated for a window like the averages). But the resulting decision boundaries
        * were too sensitive.
        * 
        * Monitoring short-term and long-term changes in the pupil
        * size to detect changes in demand was suggested in Minadakis & Lohan (2018)
        * which motivated us to test the simple threshold decision rule
        * mentioned in the wiki.
        """
        self.size = 0
        self.movingNum = 0
        self.currentValue = 0
        self.history = []
    

    def update(self,y_hat,y):
        """
        Three main steps that need to be completed include:

        * 1) Calculate power of difference
        * 2) Update moving Numerator
        * 4) Update currentValue

        Of course we also need to keep track of the current "sample size".
        """
        difference = y_hat - y
        squareDiff = difference**2

        # First add to current num!
        self.movingNum += squareDiff

        # Adjust size accordingly
        self.size += 1

        # this is the cumulative mse at any point
        # see: https://en.wikipedia.org/wiki/Mean_squared_error for formula
        """
        Save-guard for division by 0
        """
        if self.size > 1:
            self.currentValue = self.movingNum/(self.size - 1)
        else:
            self.currentValue = 0

        # For plotting and analysis
        # self.history.append(self.currentValue) #[Deprecated] we handled plotting in main
    
    def getCurrentValue(self):
        # For our decision we want the root MSE
        # so we take the root here.
        return math.sqrt(self.currentValue)
