package ru.destroy.restapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.destroy.restapi.crypto.AESCrypt;
import ru.destroy.restapi.crypto.SCrypt;
import ru.destroy.restapi.database.Database;
import ru.destroy.restapi.database.Response;

import java.util.concurrent.ThreadLocalRandom;

@RequestMapping("/api")
@RestController
public class RestAPIController {

    private static final AESCrypt crypt = AESCrypt.getCrypt();
    private final Database database = Database.getDatabase();

    static {
        crypt.setKey("b2b1d40fca6a1746ab14db81f50d0c4c24ec58c3adbb16d315cbe8b2cf56b0bc");
    }

    @RequestMapping(value = "/post", method = RequestMethod.POST)
    public ResponseEntity<String> registerUser(String user, String password, String email) {
        try {
            user = crypt.decrypt(user).orElse(null);
            password = crypt.decrypt(password).orElse(null);
            email = crypt.decrypt(email).orElse(null);

            if (user == null || password == null || email == null) {
                return ResponseEntity.badRequest().body("Invalid encryption parameters");
            }

            int randomLength = ThreadLocalRandom.current().nextInt(16, 32);
            SCrypt.Salt salt = SCrypt.generateSalt(randomLength);
            String hashedPassword = SCrypt.hashPassword(password, salt);

            Response response = database.insertUser(user, hashedPassword, email, salt);

            return switch (response) {
                case EMAIL_USED -> ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
                case USER_USED -> ResponseEntity.status(HttpStatus.CONFLICT).body("Login already taken");
                case SUCCESS -> ResponseEntity.ok("Registration successful");
            };

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Server error: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public ResponseEntity<String> verifyPassword(String user, String password) {
        try {
            user = crypt.decrypt(user).orElse(null);
            password = crypt.decrypt(password).orElse(null);

            if (user == null || password == null) {
                return ResponseEntity.badRequest().body("Invalid encryption parameters");
            }

            boolean isCorrect = database.isPasswordCorrect(user, password);

            return isCorrect
                    ? ResponseEntity.ok("Password valid")
                    : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Server error: " + e.getMessage());
        }
    }
}