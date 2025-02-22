# Simple REST API for Secure User Data Handling with Spring Boot

A minimalistic REST API implementation using Java and Spring Boot that focuses on secure data transmission and storage.

## Key Features
- 🔒 **AES Encryption** for secure data transmission over HTTP/HTTPS
- 🔑 **SCrypt** algorithm for one-way password hashing
- 📨 Basic user registration and authentication endpoints

## API Endpoints

### User Registration (POST)
Endpoint: `/api/post`  
Parameters:
- `user` (Encrypted username)
- `password` (Encrypted password)
- `email` (Encrypted email)

**Responses:**
- `200 OK`: Registration successful
- `409 Conflict`: Email already exists
- `409 Conflict`: Username already taken

### Password Verification (GET)
Endpoint: `/api/get`  
Parameters:
- `user` (Encrypted username)
- `password` (Encrypted password)

**Responses:**
- `200 OK`: Password matches
- `401 Unauthorized`: Invalid credentials

# API Usage Examples with Encryption

Example implementation for sending encrypted requests to the API:

## Java Client Example

```java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClientExample {
    private static final AESCrypt crypt = AESCrypt.getCrypt();
    private static final String BASE_URL = "http://127.0.0.1:8080/api";
    private static final String AES_KEY = "b2b1d40fca6a1746ab14db81f50d0c4c24ec58c3adbb16d315cbe8b2cf56b0bc";

    static {
        crypt.setKey(AES_KEY);
    }

    public static void main(String[] args) throws Exception {
        // Example POST request
        String postResponse = sendPostRequest("testUser", "strongPassword", "user@example.com");
        System.out.println("POST Response: " + postResponse);

        // Example GET request
        String getResponse = sendGetRequest("testUser", "strongPassword");
        System.out.println("GET Response: " + getResponse);
    }

    private static String sendPostRequest(String user, String password, String email) throws Exception {
        String encryptedUser = crypt.encrypt(user);
        String encryptedPass = crypt.encrypt(password);
        String encryptedEmail = crypt.encrypt(email);

        String url = String.format("%s/post?user=%s&password=%s&email=%s",
                BASE_URL,
                encryptedUser,
                encryptedPass,
                encryptedEmail);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    private static String sendGetRequest(String user, String password) throws Exception {
        String encryptedUser = crypt.encrypt(user);
        String encryptedPass = crypt.encrypt(password);

        String url = String.format("%s/get?user=%s&password=%s",
                BASE_URL,
                encryptedUser,
                encryptedPass);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }
}

## Technical Implementation
- **Data Transmission**: AES-256 encryption for all parameters
- **Password Storage**: 
  - SCrypt hashing
  - Salted hashes stored in SQLite

## Required Improvements
🚀 **High Priority Feature Request**  
Add request caching to improve processing speed:  
TODO: Implement caching
