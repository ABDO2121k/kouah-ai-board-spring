FROM tomcat:10.1-jdk17 as runtime

# Set working directory
WORKDIR /app

# The build process (maven service) should produce a jar in `target/*.jar`.
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1024m -Dfile.encoding=UTF-8"

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
