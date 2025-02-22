package ru.destroy.restapi.controller;

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

    // Вообщем, не уверен, что aes шифрование вообще нужно.
    // Скорее всего https протокол сам всё шифрует, но я перестраховался на всякий
    private static final AESCrypt crypt = AESCrypt.getCrypt();
    private final Database database = Database.getDatabase();
    static {
        crypt.setKey("b2b1d40fca6a1746ab14db81f50d0c4c24ec58c3adbb16d315cbe8b2cf56b0bc");
    }

    @RequestMapping(value = "/post", method = RequestMethod.POST)
    public String onRequest(String user, String password, String email) {

        // System.out.println("AES KEY => "+crypt.getKey());

        user = crypt.decrypt(user).isEmpty() ? null : crypt.decrypt(user).get();
        password = crypt.decrypt(password).isEmpty() ? null : crypt.decrypt(password).get();
        email = crypt.decrypt(email).isEmpty() ? null : crypt.decrypt(email).get();

        System.out.println("user = " + user);
        System.out.println("password = " + password);
        System.out.println("email = " + email);

        if (user == null || password == null || email == null) {
            // тут можно добавить проверку кол-ва попыток,
            // а мне лень
            return "WRONG AES KEY";
        }

        int randomLength = ThreadLocalRandom.current().nextInt(16,32);
        SCrypt.Salt salt = SCrypt.generateSalt(randomLength);

        String pas = SCrypt.hashPassword(password,salt);

        Response response = database.insertUser(user,pas,email,salt);

        switch (response) {
            case EMAIL_USED -> {
                return "Email already taken.";
            }
            case USER_USED -> {
                return "Username already taken.";
            }
        }

        return "Added user with password: "+pas+" "+response;
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public String onResponse(String user, String password) {

        System.out.println("AES KEY => "+crypt.getKey());

        user = crypt.decrypt(user).isEmpty() ? null : crypt.decrypt(user).get();
        password = crypt.decrypt(password).isEmpty() ? null : crypt.decrypt(password).get();

        System.out.println("user = " + user);
        System.out.println("password = " + password);

        if (user == null || password == null) {
            // тут можно добавить проверку кол-ва попыток,
            // а мне лень
            return "WRONG AES KEY";
        }

        boolean isCorrect = database.isPasswordCorrect(user,password);

        if (isCorrect) {
            return "Password correct";
        }
        return "Password not correct";
    }

}
