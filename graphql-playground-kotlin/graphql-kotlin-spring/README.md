## Contextual Query and Fetch

If you use graphql-kotlin in Webflux+Coroutine, you may need to communicate with 3 kinds of 'context'.

- Reactor Context: Passing data between Mono and Flux
e.g. Record some data for one operation, like request id, user data, jwt, similar with ThreadLocal in Webmvc

- Coroutine Context: Passing data between coroutines

- Graphql Context: Passing data between queries

Pls see `com.github.anddd7.graphql.plugins.*`, customized a graphql context for graphql + kotlin coroutine + reactor
