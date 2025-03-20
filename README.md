
# auth-and-user

When gRPC met Spring... but also Hexagonal Arch, Authorization Server and JWT. The system allows users to register, create categories, products, and leave opinions, reviews, and votes.

**Components** :rotating_light::no_entry: **IN CONSTRUCCTION**
- [Kafka](https://kafka.apache.org/) [9092] + [9093] 
- Eureka server as service registry and discovery service [8761]
- Authorization Server (the-grpc-autho) [9000] + [9001]
- gRPC Server the-grpc-user-pack [0]
- gRPC Server the-grpc-opinator [0]


## Table of contents

- [Installation](#installation)
- [Usage](#usage)
- [It's not a bug, it's a feature](#features)
- [Maintainers](#maintainers)
- [License](#license)


## Installation

#### First steps

1. As usual, please clone or download the project.

1. Inside the main folder, you could find a docker-compose yaml file.

1. From there use the command line to start the project

```    
    **Developer mode**  
    docker-compose -f docker-compose-dev.yml up -d

```
      
The dev environment is ready for using with your IDE. The microservice attempts to communicate with Kafka using the local host. 
   
4. You could stop the project and free resources with any of this orders

```
    **Developer mode**
    docker-compose -f docker-compose-dev.yml down --rmi local -v
    
```
5. It is important to have [grpcurl](https://github.com/fullstorydev/grpcurl) to call gRPC services in your system. 
  
#### About certificates

The communication between the gRPC server and clients is secure thanks to SSL certificates. The document  located at the root project, certificates.md, and the explanation on [grpc-ecosystem](https://grpc-ecosystem.github.io/grpc-spring/en/server/security.html) could clarify you how it works. 
     
     
## Usage

#### Initial considerations

- In your IDE, you should always follow this order to run microservices. First Discovery service, then the-grpc-autho, the-grpc-user-pack, and finally the-grpc-opinator.
- In the project, when using [grpcurl](https://github.com/fullstorydev/grpcurl), **it is mandatory to send a certificate with the command.** For convenience the same certificate is used for all services and is located in the folder `certificates` within each module, except for the discovery server. 
- Additionally, some gRPC methods require including a JWT token with the request. If this constraint is configured, you could revise it in the `GrpcConfig` class, under the `dev.ime.common.config` package in that module

| Module | Service | Method | Token JWT Required|
|-----|-----|-----|-----|
| the-grpc-opinator | ReviewGrpcService | CreateReview | X |
| the-grpc-opinator | ReviewGrpcService | UpdateReview | X | 
| the-grpc-opinator | ReviewGrpcService | DeleteReview | X | 
| the-grpc-opinator | VoteGrpcService | CreateVote | X | 
| the-grpc-opinator | VoteGrpcService | UpdateVote | X | 
| the-grpc-opinator | VoteGrpcService | DeleteVote | X | 

- To learn more about each gRPC service methods simply revise its .proto file under `src.main.proto` folder. An alternative approach is to run against the microservice the following commands, adjusting the port as needed:

```
grpcurl -cacert ./ca.crt localhost:9001 list
grpcurl -cacert ./ca.crt localhost:9001 describe
```

#### Register

- In the first place you should register an user using the suitable gRPC method. Here is an example of the command you need to send:

```
grpcurl -cacert ./ca.crt -d '{"name":"Momo","lastname":"Ayase","email":"momo@ayase.tk","password":"12345"}' localhost:9001 net.proto.AuthGrpcService.CreateUser
```
- Next, since form-based login authorization is active, we need to use a browser. Navigate to `http://localhost:9000/login` and use the credentials you have just created.
- If the authentication is successful, you will be redirected to the success page where you can request an **authorization code**.

#### Request Token

A good way for knowing more about the JWT generation and validation flow, is reading [this article](https://adictosaltrabajo.com/2023/06/29/securizacion-aplicacion-oauth-2-spring-authorization-server-spring-resource-server/). Otherwise you can follow these steps:

- From the success page, push the button `Request Auth Code`. The authorization server will respond with a code, which the client can exchange for tokens over a secure channel.
- We use the POST method to request our JWT token sending the authorization code and the rest of parameters

<img src="./src/main/resources/static/images/request_token_conf.png" style="width: 800px; max-width: 1024px; flex-grow: 1;" />

&nbsp;

- Now we can use the retrieved JWT token with the tool [grpcurl](https://github.com/fullstorydev/grpcurl) when necessary

You can find a [SoapUI](https://www.soapui.org/) profile with the request token order configured at the root folder.
  
#### Main operations

You can use [grpcurl](https://github.com/fullstorydev/grpcurl) to call gRPC services by selecting the appropriate port. **Since the port is random, you will need to adjust the examples accordingly**. The main operations in the project are managed by `the-grpc-opinator` microservice, so the following commands are for it. Remember to request and update the token too.

```   
- Category endpoint examples

grpcurl -cacert ./ca.crt -d '{"categoryName":"Grocery"}' localhost:50051 net.proto.CategoryGrpcService.CreateCategory
grpcurl -cacert ./ca.crt -d '{"categoryId":"1", "categoryName":"Vegetables"}' localhost:50051 net.proto.CategoryGrpcService.UpdateCategory
grpcurl -cacert ./ca.crt -d '{"categoryId":"1"}' localhost:50051 net.proto.CategoryGrpcService.DeleteCategory
grpcurl -cacert ./ca.crt localhost:50051 net.proto.CategoryGrpcService.ListCategories
grpcurl -cacert ./ca.crt -d '{"page":0, "size":2, "sortBy":"categoryName", "sortDir":"DESC"}' localhost:50051 net.proto.CategoryGrpcService.ListCategoriesPaginated
grpcurl -cacert ./ca.crt -d '{"categoryId":"1"}' localhost:50051 net.proto.CategoryGrpcService.GetCategory

- Product endpoint examples

grpcurl -cacert ./ca.crt localhost:50051 describe net.proto.ProductGrpcService
grpcurl -cacert ./ca.crt -d '{"productName":"Lettuce", "productDescription":"green", "categoryId":"1"}' localhost:50051 net.proto.ProductGrpcService.CreateProduct
grpcurl -cacert ./ca.crt -d '{"productId":"1", "productName":"Tomatoes", "productDescription":"full of red", "categoryId":"1"}' localhost:50051 net.proto.ProductGrpcService.UpdateProduct
grpcurl -cacert ./ca.crt -d '{"productId":"1"}' localhost:50051 net.proto.ProductGrpcService.DeleteProduct
grpcurl -cacert ./ca.crt localhost:50051 net.proto.ProductGrpcService.ListProducts
grpcurl -cacert ./ca.crt -d '{"page":0, "size":2, "sortBy":"productName", "sortDir":"DESC"}' localhost:50051 net.proto.ProductGrpcService.ListProductsPaginated
grpcurl -cacert ./ca.crt -d '{"productId":"1"}' localhost:50051 net.proto.ProductGrpcService.GetProduct

- Review endpoint examples

grpcurl -cacert ./ca.crt -H "Authorization: Bearer eyJraWQiOiIwYTU0ZTg2Ny1hMjViLTQ4Y2QtOTdjZC0wMGFhZWY0NjRiMTYiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJsaXphQGxpemEuanAiLCJhdWQiOiJjbGllbnQiLCJuYmYiOjE3NDAzODMwNTAsInNjb3BlIjpbIm9wZW5pZCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwMDAiLCJleHAiOjE3NDAzODQ4NTAsImlhdCI6MTc0MDM4MzA1MCwianRpIjoiMGQxYjZiMmYtYmU2Yy00Mzk1LWE3ZTQtNzU5NTFhNzAxMDJhIn0.e8fRyXi_W1ek3thgKuZoJxmdjFYQkNnH2p-IbqRdyDzWw9I1FNnq5Q85K-uUGB75Y2xJrBy9zB4m6dEn74_z3ijsaxa_1XNWwT-zVH0PnJJwuow2p-ZKFdnoDbV2A0hRCrk87YfYKEZqNqQlkfxo7iJzqpZX-6MOG-b4b-Cik1uaqkTx4kFYcssu3b7bqu6OcSKknPz1RNgitG2s-N3SrIC17rWkDMBtqvhToe0mwzQwD2bn-V52HbIzYyMAzTK0PlwwserH_Wj0Wg7osqRGvo2IHspLNTZYyBwpAgOOwN-KMvvx77mBZZ-M7HUTQDHK7RZQrzkOuOE96Gg882sTiA" -d '{"productId":"1", "reviewText":"Medium", "rating":"3"}' localhost:50051 net.proto.ReviewGrpcService.CreateReview
grpcurl -cacert ./ca.crt -H "Authorization: Bearer eyJraWQiOiIwYTU0ZTg2Ny1hMjViLTQ4Y2QtOTdjZC0wMGFhZWY0NjRiMTYiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJsaXphQGxpemEuanAiLCJhdWQiOiJjbGllbnQiLCJuYmYiOjE3NDAzODMwNTAsInNjb3BlIjpbIm9wZW5pZCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwMDAiLCJleHAiOjE3NDAzODQ4NTAsImlhdCI6MTc0MDM4MzA1MCwianRpIjoiMGQxYjZiMmYtYmU2Yy00Mzk1LWE3ZTQtNzU5NTFhNzAxMDJhIn0.e8fRyXi_W1ek3thgKuZoJxmdjFYQkNnH2p-IbqRdyDzWw9I1FNnq5Q85K-uUGB75Y2xJrBy9zB4m6dEn74_z3ijsaxa_1XNWwT-zVH0PnJJwuow2p-ZKFdnoDbV2A0hRCrk87YfYKEZqNqQlkfxo7iJzqpZX-6MOG-b4b-Cik1uaqkTx4kFYcssu3b7bqu6OcSKknPz1RNgitG2s-N3SrIC17rWkDMBtqvhToe0mwzQwD2bn-V52HbIzYyMAzTK0PlwwserH_Wj0Wg7osqRGvo2IHspLNTZYyBwpAgOOwN-KMvvx77mBZZ-M7HUTQDHK7RZQrzkOuOE96Gg882sTiA" -d '{"reviewId":"1", "reviewText":"Awesome", "rating":"4"}' localhost:50051 net.proto.ReviewGrpcService.UpdateReview
grpcurl -cacert ./ca.crt -H "Authorization: Bearer eyJraWQiOiIwYTU0ZTg2Ny1hMjViLTQ4Y2QtOTdjZC0wMGFhZWY0NjRiMTYiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJsaXphQGxpemEuanAiLCJhdWQiOiJjbGllbnQiLCJuYmYiOjE3NDAzODMwNTAsInNjb3BlIjpbIm9wZW5pZCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwMDAiLCJleHAiOjE3NDAzODQ4NTAsImlhdCI6MTc0MDM4MzA1MCwianRpIjoiMGQxYjZiMmYtYmU2Yy00Mzk1LWE3ZTQtNzU5NTFhNzAxMDJhIn0.e8fRyXi_W1ek3thgKuZoJxmdjFYQkNnH2p-IbqRdyDzWw9I1FNnq5Q85K-uUGB75Y2xJrBy9zB4m6dEn74_z3ijsaxa_1XNWwT-zVH0PnJJwuow2p-ZKFdnoDbV2A0hRCrk87YfYKEZqNqQlkfxo7iJzqpZX-6MOG-b4b-Cik1uaqkTx4kFYcssu3b7bqu6OcSKknPz1RNgitG2s-N3SrIC17rWkDMBtqvhToe0mwzQwD2bn-V52HbIzYyMAzTK0PlwwserH_Wj0Wg7osqRGvo2IHspLNTZYyBwpAgOOwN-KMvvx77mBZZ-M7HUTQDHK7RZQrzkOuOE96Gg882sTiA" -d '{"reviewId":"1"}' localhost:50051 net.proto.ReviewGrpcService.DeleteReview
grpcurl -cacert ./ca.crt localhost:50051 net.proto.ReviewGrpcService.ListReviews
grpcurl -cacert ./ca.crt -d '{"page":0, "size":2, "sortBy":"reviewId", "sortDir":"DESC"}' localhost:50051 net.proto.ReviewGrpcService.ListReviewsPaginated
grpcurl -cacert ./ca.crt -d '{"reviewId":"1"}' localhost:50051 net.proto.ReviewGrpcService.GetReview

- Vote endpoint examples

grpcurl -cacert ./ca.crt -H "Authorization: Bearer eyJraWQiOiIwYTU0ZTg2Ny1hMjViLTQ4Y2QtOTdjZC0wMGFhZWY0NjRiMTYiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJsaXphQGxpemEuanAiLCJhdWQiOiJjbGllbnQiLCJuYmYiOjE3NDAzODMwNTAsInNjb3BlIjpbIm9wZW5pZCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwMDAiLCJleHAiOjE3NDAzODQ4NTAsImlhdCI6MTc0MDM4MzA1MCwianRpIjoiMGQxYjZiMmYtYmU2Yy00Mzk1LWE3ZTQtNzU5NTFhNzAxMDJhIn0.e8fRyXi_W1ek3thgKuZoJxmdjFYQkNnH2p-IbqRdyDzWw9I1FNnq5Q85K-uUGB75Y2xJrBy9zB4m6dEn74_z3ijsaxa_1XNWwT-zVH0PnJJwuow2p-ZKFdnoDbV2A0hRCrk87YfYKEZqNqQlkfxo7iJzqpZX-6MOG-b4b-Cik1uaqkTx4kFYcssu3b7bqu6OcSKknPz1RNgitG2s-N3SrIC17rWkDMBtqvhToe0mwzQwD2bn-V52HbIzYyMAzTK0PlwwserH_Wj0Wg7osqRGvo2IHspLNTZYyBwpAgOOwN-KMvvx77mBZZ-M7HUTQDHK7RZQrzkOuOE96Gg882sTiA" -d '{"reviewId":"1", "useful":"true"}' localhost:50051 net.proto.VoteGrpcService.CreateVote
grpcurl -cacert ./ca.crt -H "Authorization: Bearer eyJraWQiOiIwYTU0ZTg2Ny1hMjViLTQ4Y2QtOTdjZC0wMGFhZWY0NjRiMTYiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJsaXphQGxpemEuanAiLCJhdWQiOiJjbGllbnQiLCJuYmYiOjE3NDAzODMwNTAsInNjb3BlIjpbIm9wZW5pZCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwMDAiLCJleHAiOjE3NDAzODQ4NTAsImlhdCI6MTc0MDM4MzA1MCwianRpIjoiMGQxYjZiMmYtYmU2Yy00Mzk1LWE3ZTQtNzU5NTFhNzAxMDJhIn0.e8fRyXi_W1ek3thgKuZoJxmdjFYQkNnH2p-IbqRdyDzWw9I1FNnq5Q85K-uUGB75Y2xJrBy9zB4m6dEn74_z3ijsaxa_1XNWwT-zVH0PnJJwuow2p-ZKFdnoDbV2A0hRCrk87YfYKEZqNqQlkfxo7iJzqpZX-6MOG-b4b-Cik1uaqkTx4kFYcssu3b7bqu6OcSKknPz1RNgitG2s-N3SrIC17rWkDMBtqvhToe0mwzQwD2bn-V52HbIzYyMAzTK0PlwwserH_Wj0Wg7osqRGvo2IHspLNTZYyBwpAgOOwN-KMvvx77mBZZ-M7HUTQDHK7RZQrzkOuOE96Gg882sTiA" -d '{"voteId":"1", "useful":"false"}' localhost:50051 net.proto.VoteGrpcService.UpdateVote
grpcurl -cacert ./ca.crt -H "Authorization: Bearer eyJraWQiOiIwYTU0ZTg2Ny1hMjViLTQ4Y2QtOTdjZC0wMGFhZWY0NjRiMTYiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJsaXphQGxpemEuanAiLCJhdWQiOiJjbGllbnQiLCJuYmYiOjE3NDAzODMwNTAsInNjb3BlIjpbIm9wZW5pZCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwMDAiLCJleHAiOjE3NDAzODQ4NTAsImlhdCI6MTc0MDM4MzA1MCwianRpIjoiMGQxYjZiMmYtYmU2Yy00Mzk1LWE3ZTQtNzU5NTFhNzAxMDJhIn0.e8fRyXi_W1ek3thgKuZoJxmdjFYQkNnH2p-IbqRdyDzWw9I1FNnq5Q85K-uUGB75Y2xJrBy9zB4m6dEn74_z3ijsaxa_1XNWwT-zVH0PnJJwuow2p-ZKFdnoDbV2A0hRCrk87YfYKEZqNqQlkfxo7iJzqpZX-6MOG-b4b-Cik1uaqkTx4kFYcssu3b7bqu6OcSKknPz1RNgitG2s-N3SrIC17rWkDMBtqvhToe0mwzQwD2bn-V52HbIzYyMAzTK0PlwwserH_Wj0Wg7osqRGvo2IHspLNTZYyBwpAgOOwN-KMvvx77mBZZ-M7HUTQDHK7RZQrzkOuOE96Gg882sTiA" -d '{"voteId":"1"}' localhost:50051 net.proto.VoteGrpcService.DeleteVote
grpcurl -cacert ./ca.crt localhost:50051 net.proto.VoteGrpcService.ListVotes
grpcurl -cacert ./ca.crt -d '{"page":0, "size":2, "sortBy":"voteId", "sortDir":"DESC"}' localhost:50051 net.proto.VoteGrpcService.ListVotesPaginated
grpcurl -cacert ./ca.crt -d '{"voteId":"1"}' localhost:50051 net.proto.VoteGrpcService.GetVote
      
```
 

## Features

#### :arrow_forward: Secure gRPC communications with SSL encryption

#### :arrow_forward: JWT token-based authorization for secure access

#### :arrow_forward: Comprehensive unit testing for business logic classes



## Maintainers

Just me, [Iván](https://github.com/Ivan-Montes) :sweat_smile:


## License

[GPLv3 license](https://choosealicense.com/licenses/gpl-3.0/)


---


[![Java](https://badgen.net/static/JavaSE/21/orange)](https://www.java.com/es/)
[![Maven](https://badgen.net/badge/icon/maven?icon=maven&label&color=red)](https://https://maven.apache.org/)
[![Spring](https://img.shields.io/badge/spring-blue?logo=Spring&logoColor=white)](https://spring.io)
[![GitHub](https://badgen.net/badge/icon/github?icon=github&label)](https://github.com)
[![Eclipse](https://badgen.net/badge/icon/eclipse?icon=eclipse&label)](https://https://eclipse.org/)
[![SonarQube](https://badgen.net/badge/icon/sonarqube?icon=sonarqube&label&color=purple)](https://www.sonarsource.com/products/sonarqube/downloads/)
[![Docker](https://badgen.net/badge/icon/docker?icon=docker&label)](https://www.docker.com/)
[![Kafka](https://badgen.net/static/Apache/Kafka/cyan)](https://kafka.apache.org/)
[![GPLv3 license](https://badgen.net/static/License/GPLv3/blue)](https://choosealicense.com/licenses/gpl-3.0/)
