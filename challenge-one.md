# Day one challenge: Music app

In this challenge you're asked to create a Java microservice for a music school. You should deliver an MVP, that should be able to:

* List bands and its genre used on it;
* Search by a specific band name (or part of the name);

Technically, these are the pre-requirements of this application:

1. It should be Java-based and use Payara micro as the runtime; 
2. It should be named **music-service**;
3. During the start-up, a list of 100 songs should be **eagerly** loaded in a **Singleton bean**.
4. There should be two REST APIs:
   1. HTTP GET /bands/ - return a list of bands
   2. HTTP GET /bands/{bandName}/albums - return a list of albums from a specific band 
      1. In case the user tries to search for the albums of a band that does not exist, the exception `BandNotFoundException` should be raised and the endpoint should return `HTTP 404`. 