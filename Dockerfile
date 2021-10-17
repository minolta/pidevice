FROM openjdk:11
ARG JAR_FILE=target/pidevice.jar
COPY ${JAR_FILE} app.jar
#RUN apk add --no-cache tzdata
ENV TZ Asia/bangkok
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS  -jar app.jar" ]
