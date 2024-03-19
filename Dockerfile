FROM openjdk:17

VOLUME /tmp

ADD build/libs/admin-client.jar admin-client.jar

RUN bash -c 'touch /admin-client.jar'

ENTRYPOINT ["java", "-jar", "/admin-client.jar"]
