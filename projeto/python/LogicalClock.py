import threading

class LogicalClock:
    def __init__(self):
        self.time = 0
        self.lock = threading.Lock()

    def increment(self):
        with self.lock:
            self.time += 1
            return self.time

    def update(self, received):
        with self.lock:
            self.time = max(self.time, received)

    def get_time(self):
        with self.lock:
            return self.time