# micro-multi-rpc

This project uses Quarkus, the Supersonic Subatomic Java Framework.

The project was created with the following command:

`quarkus create app org.dew:micro-multi-rpc --no-code`

`cd micro-multi-rpc`

`quarkus ext add io.quarkus:quarkus-undertow`

## Dependencies

**multi-rpc 3.0.0**

- `git clone https://github.com/giosil/multi-rpc-3.git` 
- `mvn clean install` - this will publish `multi-rpc-3.0.0.jar` in Maven local repository

## Build with Quarkus

- `git clone https://github.com/giosil/micro-json-rpc.git`
- `quarkus build`

## Run in development mode

- `quarkus dev`

To enable debug :

- `quarkus dev -Dsuspend -Ddebug` 

Then, attach your debugger to localhost:5005.

## Test

POST `http://localhost:8080/rpc`

```json
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "DEMO.hello",
  "params": ["world"]
}
```

```json
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "DEMO.helloObj",
  "params": ["world"]
}
```