# Spring Quartz Retry
[![Build](https://github.com/thomasrepnik/spring-quartz-retry/actions/workflows/build.yml/badge.svg)](https://github.com/thomasrepnik/spring-quartz-retry/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=thomasrepnik_spring-quartz-retry&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=thomasrepnik_spring-quartz-retry)

Spring Quartz Retry offers a simple, lightweight retry mechanism using the quartz scheduler.

## Prerequisites 
For using this library your project needs
* to be a spring-boot project
* to have quartz scheduler configured with persistent storage (database)

## Getting started

Add the following Maven Dependency to your project
```xml
<dependency>
    <groupId>ch.repnik.quartz-retry</groupId>
    <artifactId>spring-quartz-retry</artifactId>
    <version>version is available soon</version>
</dependency>
```

Create a new spring component by extending `QuartzRetry`
```java
@Service
public class SampleService extends QuartzRetry<Payload, String> {
//This Class takes a request type of Payload and returns a String if the execution succeeds
    
    @Autowired
    private MailService mailService; //Just a dummy service for demonstration
    
    @Override
    protected String process(Payload payload, RetryContext ctx) {
        //If this method throws any kind of RuntimeException, the execution will be retried
        return mailService.send(payload);
    }

    @Override
    protected RetryTimeout[] getRetryTimeouts() {
        //In case of a failing process-method three retries will be scheduled with the defined timeouts
        return new RetryTimeout[] {
                timeout(3, SECOND),
                timeout(10, SECOND),
                timeout(5, SECOND)
        };
    }

    @Override
    protected void onError(Payload entity, Exception e, RetryContext ctx) {
        //This method will be executed when a retry attempt has failed
    }

    @Override
    protected void onSuccess(Payload entity, String s, RetryContext ctx) {
        //This method will be executed when the process method was successful
        //The second Parameter (String s) will be the return value of the process-Method
    }

    @Override
    protected void onFailure(Payload payload, Exception e, RetryContext ctx) {
        //This method will be executed when all retries have failed.
    }
}
```

After providing your implementation, you can just call the `execute` method

```java
@RestController
public class SampleController {

    @Autowired
    private SampleService sampleService;

    @GetMapping("/start")
    private ResponseEntity call(){
        sampleService.execute(new Payload());
        return ResponseEntity.ok().build();
    }
}
```

That's it!

For a full and running sample please check the sample Directory
