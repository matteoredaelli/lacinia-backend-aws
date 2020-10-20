FROM openjdk:11
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "lacinia-backend-qliksense-with-deps.jar"]
