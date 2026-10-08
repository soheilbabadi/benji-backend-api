# Read Me First
The following was discovered as part of building this project:

* The original package name 'social.benji.benji-backend-api' is invalid and this project uses 'social.benji.benji_backend_api' instead.

# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/4.1.1/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.1.1/maven-plugin/build-image.html)
* [Distributed Tracing Reference Guide](https://docs.micrometer.io/tracing/reference/index.html)
* [Getting Started with Distributed Tracing](https://docs.spring.io/spring-boot/4.1.1/reference/actuator/tracing.html)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/4.1.1/reference/using/devtools.html)
* [Spring Web](https://docs.spring.io/spring-boot/4.1.1/reference/web/servlet.html)
* [HTTP Client](https://docs.spring.io/spring-boot/4.1.1/reference/io/rest-client.html#io.rest-client.restclient)
* [SpringDoc OpenAPI](https://springdoc.org/)
* [Spring Security](https://docs.spring.io/spring-boot/4.1.1/reference/web/spring-security.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/4.1.1/reference/data/sql.html#data.sql.jpa-and-spring-data)
* [Spring Data JDBC](https://docs.spring.io/spring-boot/4.1.1/reference/data/sql.html#data.sql.jdbc)
* [Liquibase Migration](https://docs.spring.io/spring-boot/4.1.1/how-to/data-initialization.html#howto.data-initialization.migration-tool.liquibase)
* [Spring Data Redis (Access+Driver)](https://docs.spring.io/spring-boot/4.1.1/reference/data/nosql.html#data.nosql.redis)
* [Spring Data MongoDB](https://docs.spring.io/spring-boot/4.1.1/reference/data/nosql.html#data.nosql.mongodb)
* [Spring Data Elasticsearch](https://docs.spring.io/spring-boot/4.1.1/reference/data/nosql.html#data.nosql.elasticsearch)
* [Validation](https://docs.spring.io/spring-boot/4.1.1/reference/io/validation.html)
* [Java Mail Sender](https://docs.spring.io/spring-boot/4.1.1/reference/io/email.html)
* [Quartz Scheduler](https://docs.spring.io/spring-boot/4.1.1/reference/io/quartz.html)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/4.1.1/reference/actuator/index.html)
* [Prometheus](https://docs.spring.io/spring-boot/4.1.1/reference/actuator/metrics.html#actuator.metrics.export.prometheus)
* [OpenTelemetry](https://docs.spring.io/spring-boot/4.1.1/reference/actuator/observability.html#actuator.observability.opentelemetry)
* [Spring REST Docs](https://docs.spring.io/spring-restdocs/docs/current/reference/htmlsingle/)
* [Ollama](https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [SpringDoc OpenAPI](https://github.com/springdoc/springdoc-openapi-demos/)
* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Using Spring Data JDBC](https://github.com/spring-projects/spring-data-examples/tree/main/jdbc/basics)
* [Messaging with Redis](https://spring.io/guides/gs/messaging-redis/)
* [Accessing Data with MongoDB](https://spring.io/guides/gs/accessing-data-mongodb/)
* [Validation](https://spring.io/guides/gs/validating-form-input/)
* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

