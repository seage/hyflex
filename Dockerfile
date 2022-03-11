FROM openjdk:17-jdk-slim

WORKDIR /hyflex

COPY . .

RUN ./scripts/build.sh

CMD tail -f /dev/null

