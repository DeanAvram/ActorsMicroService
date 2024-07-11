# Actors Micro Service

This project is a microservice that provides a REST API and RSocket API to manage actors.

## Actor boundary

The movie boundary is a simple example that represents a movie. It has the following attributes:

`
```json
{
  "id": "nm0000235",
  "name": "Uma Thurman",
  "birthdate": "29-04-1970",
  "movies": [
    "tt0110912"
  ]
}
```

## REST API

Swagger page to test the REST API: [http://localhost:9091/swagger-ui.html](http://localhost:9091/swagger-ui.html)

## RSocket API

RSocket is a binary protocol for use on byte stream transports

The RSocket API is available on port 7002.

### Running RSocket API

- delete all actors

We are using fnf (fire and forget) to delete all actors.

```shell
java -jar rsc-0.9.1.jar --fnf --route=delete-all-actors--debug tcp://localhost:7002
```

- get actors by criteria

We are using channel to get actors by criteria. Channel is a bidirectional communication between consumer and server.

```shell
java -jar rsc-0.9.1.jar --channel --route=get-actors-by-criteria-channel --data=- --debug tcp://localhost:7002
```
