FROM maven:3.5.3-jdk-8
MAINTAINER Bartosz Wróblewski <wroblewskib92@gmail.com>

ENV workdir /opt/cargame

# defines game duration
ENV GAME_DURATION 30
ENV DISABLE_CORS false
ENV BACK_IN_HISTORY_DELAY 1000

WORKDIR /opt/cargame
COPY . .
RUN mvn clean install
EXPOSE 8888

CMD java -jar ./backend/target/cargame.jar
