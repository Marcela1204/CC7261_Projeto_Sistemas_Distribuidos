FROM python:3.9-slim

WORKDIR /app

COPY . .

RUN pip install pyzmq

CMD ["python", "Broker.py"]