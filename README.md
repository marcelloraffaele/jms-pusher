# jms-pusher Project

This project was made to push messages on JMS Queues and Topics. It uses Quarkus and 'quarkus-qpid-jms library' to connect to the queue and 'javafaker' to generate random messages.

## Set the enrivonment variables
# Configures the Qpid JMS properties.
quarkus.qpid-jms.url=amqp://JMS_BROKER_HOST:5672
quarkus.qpid-jms.username=YOUR_USERNAME
quarkus.qpid-jms.password=YOUR_PASSWORD
pusher.queue=QUEUE
pusher.initial-delay=0
pusher.delay=5000
pusher.messageLenght=100
pusher.producerEnabled=true
pusher.consumerEnabled=true


## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Build docker image
```shell script
docker build -f src/main/docker/Dockerfile.jvm -t jms-pusher:2.0 .
```

if you want to push the image:
```shell script
docker push jms-pusher:2.0
```

## TEST the application locally:
Using Docker, you can create an ArtemisMQ Broker and create a test queue.
```shell script
docker run -it --rm -p 8161:8161 -p 61616:61616 -p 5672:5672 -e ARTEMIS_USERNAME=quarkus -e 
```

ARTEMIS_PASSWORD=quarkus vromero/activemq-artemis:2.11.0-alpine

and you can run:
```shell script
./mvnw compile quarkus:dev
```

## Test the application using Docker images
I already pushed this image on docker hub, you can use it

First of all you need an ArtemisMQ Broker and create a test queue
```shell script
docker run -it --rm -p 8161:8161 -p 61616:61616 -p 5672:5672 -e ARTEMIS_USERNAME=quarkus -e 
```

You can run the producer:
```shell script
docker run -i --rm -p 8080:8080 -e quarkus.qpid-jms.url=amqp://192.168.1.80:5672 -e pusher.queue=test1Address::test1Queue -e pusher.delay=100 -e pusher.messageLenght=1000 -e pusher.producerEnabled=true -e pusher.consumerEnabled=false jms-pusher:2.0
```

And if needed you can run the consumer:
```shell script
docker run -i --rm -e quarkus.qpid-jms.url=amqp://192.168.1.80:5672 -e pusher.queue=test1Address::test1Queue -e pusher.delay=100 -e pusher.messageLenght=1000 -e pusher.producerEnabled=false -e pusher.consumerEnabled=true jms-pusher:2.0
```
