import queue


class MovingAverage():

    def __init__(self,size):
        """
        Relatively efficient simple moving average.

        Based on: https://en.wikipedia.org/wiki/Moving_average
        """
        self.windowSize = size
        self.buffer = queue.Queue(maxsize= size + 1)
        self.currentBufferSize = 0
        self.movingSum = 0
        self.currentValue = 0
        self.history = []
    

    def update(self,newSample):
        """
        * Three steps:
        * 1) update buffer
        * 2) update movingSum
        * 3) update currentValue
        """

        # First add to current sum!
        self.movingSum += newSample

        # Now optionally remove first item from buffer and adapt sum
        if (self.currentBufferSize == self.windowSize):
            self.movingSum -= self.buffer.get()
            self.currentBufferSize -= 1
        

        # Add new sample to buffer and adjust size accordingly
        self.buffer.put(newSample)
        self.currentBufferSize += 1

        # this is the simple moving average
        self.currentValue = self.movingSum/self.currentBufferSize

        # For plotting and analysis
        self.history.append(self.currentValue)
    
    def getCurrentValue(self):
        return self.currentValue
