
# certificates

Mutual TLS authentication (mTLS) is a process in which both the server and the client authenticate each other using certificates. This ensures that both the server and the clients are who they claim to be, which is particularly useful for securing communication in sensitive environments.

First, it is ideal to generate a Certificate Authority (CA), which will sign both the server's and the clients' certificates. This is useful because any client or server certified by your CA will be recognized as valid.
     
```bash
**Generate CA private key** 
openssl genpkey -algorithm RSA -out ca.key -aes256
```

   - Private key for signing certificates 
   - PEM pass phrase: certificadora
  
```bash
**Generate CA certificate**
openssl req -key ca.key -new -x509 -out ca.crt -days 3650
```
   - Here the X.509 self signed certificate is created 
   - Pass phrase: certificadora
   - Complete the requested information with fictional data
      
```bash
**Generate private key**
openssl genpkey -algorithm RSA -out server.key
```
  
   - CHALLENGE PASS: firmame
   - Complete the requested information with fictional data
           
```bash
**Generate sign request (CSR)**
openssl req -new -key server.key -out server.csr -nodes
```     
    
```bash 
**Sign certificate with CA**
openssl x509 -req -in server.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out server.crt -days 3650 -sha256 -extfile <(printf "subjectAltName=DNS:localhost, DNS:the-grpc-autho, DNS:the-grpc-opinator, DNS:the-grpc-user-pack, IP:127.0.0.1")
```

```bash 
**Sign**
openssl x509 -in server.crt -text -noout
```
