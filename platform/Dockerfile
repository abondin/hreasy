FROM azul/zulu-openjdk-alpine:11-jre
VOLUME /tmp

RUN apk --update add fontconfig ttf-dejavu

ARG JAR_FILE
ADD ${JAR_FILE} app.jar

EXPOSE 8080

HEALTHCHECK --interval=5s --timeout=2s --retries=12 --start-period=320s \
  CMD wget --quiet --tries=1 --spider http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
