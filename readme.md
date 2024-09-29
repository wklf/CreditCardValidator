# How to run
Requires Java 17 and maven to run (tested on maven 3.6.3).

# Description
A simple Jetty web server is deployed locally using the Javalin framework to provide an endpoint when executing the code in the repository. As the program has dependencies it can be executed from the fat jar using
java -jar target/CCValidator-1.0-SNAPSHOT-jar-with-dependencies.jar 

# Tests
The test suite is executed automatically when building the service using mvn install.

# API specification
A post request is made to localhost:4001/validate with a JSON payload formatted according to the example below:

{"name":"Jane Doe",
 "number":"4444-4444-4444-4444",
 "date":"11/24",
 "cvc":"134"}

The number field can be delimited by either a blank space, nothing, or a hyphen while the date field is separated using a "/".

The server will return the credit card provider if successful accordingly: 

{"result":"VISA"}

The validation is performed in two steps, 1) check that the required fields are present, and 2) check the validity of the data.

In case of validation error the result will say so and contain an array with the errors like this:

{"result":"Error, please se provided errors",
 "errors:["Card has expired."]}

