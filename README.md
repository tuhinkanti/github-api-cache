# github-api-cache
This project builds a reactive cache to cache get requests made to Github APIs.

Instructions to run the program are as follows.

Configure the environment variable GITHUB_API_TOKEN
```$xslt
export GITHUB_API_TOKEN=<YOUR GITHUB TOKEN>
```

This project is built using maven. This project requires Java 8 to be installed and available in path to execute. You may execute the program using the following command inside the root directory. (Directory with the pom.xml):
```
mvn spring-boot:run
```
You may verify functionality by executing tests by running the script using the following command:
```
./api-suite.sh 8080 8080
```

8080 is the default port configured which you may change by getting into `src/main/java/resources/application.properties` and setting the variable `server.port` to the port of your choice.
