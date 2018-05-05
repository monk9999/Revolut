# Highly efficient account transfer Vert.x web app (Revolut recruitment test)

## Features
This is standalone server application with embedded in-memory database. 
Application is highly concurrent and can support multiple simultaneous request.
Can easily be transformed into clustered solution by simple refactoring. Just need to deploy verticles in clustered mode with for example Hazelcast instance as backbone. Thanks to implemented distributed locking mechanism DB also can be distributed using the same Hazelcast instance or for that matter any other DB solution.
### Sample request
```
POST /transfer
Headers: Content-Type: application/json
Body:
{ 
"fromAccount":"4",
"toAccount":"04",
"amount":1
}
```
### Configuration
Base configuration file is application.properties located in resource folder.
### Build
```
mvn clean package
```
### Verify with PMD, Checkstyle, FindBug
```
mvn verify
```
## Lunch
```
java -jar /target/revolut-1.0-SNAPSHOT-fat.jar
```
#### Additional notes
Application autoscales server verticles up to the number of available processors. To use specified number of processors change 'http.server.instances' from 0 (autoscale) to desired number.
Default http port is 8080. You can change it by modifying 'http.port' value in application.properties file.
#### Benchmark results
There is appropriate JMH benchmark test (PerformanceTest.java) that is not run on test stage.
Here are results:
```
Machine: Intel i7-7700K OC 5GHz 16GB RAM 4200MHz
```
```
N = 212637
  mean =   1175,056 ±(99.9%) 2,583 us/op

  Histogram, us/op:
    [   0,000,  250,000) = 5 
    [ 250,000,  500,000) = 2 
    [ 500,000,  750,000) = 49 
    [ 750,000, 1000,000) = 78553 
    [1000,000, 1250,000) = 92541 
    [1250,000, 1500,000) = 4621 
    [1500,000, 1750,000) = 5451 
    [1750,000, 2000,000) = 15120 
    [2000,000, 2250,000) = 16237 
    [2250,000, 2500,000) = 22 
    [2500,000, 2750,000) = 14 
    [2750,000, 3000,000) = 13 
    [3000,000, 3250,000) = 6 
    [3250,000, 3500,000) = 2 
    [3500,000, 3750,000) = 1 

  Percentiles, us/op:
      p(0,0000) =    160,768 us/op
     p(50,0000) =   1009,664 us/op
     p(90,0000) =   1990,656 us/op
     p(95,0000) =   2011,136 us/op
     p(99,0000) =   2043,904 us/op
     p(99,9000) =   2084,864 us/op
     p(99,9900) =   2761,558 us/op
     p(99,9990) =   3457,225 us/op
     p(99,9999) =   3555,328 us/op
    p(100,0000) =   3555,328 us/op
```