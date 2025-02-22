package ru.destroy.restapi;

import ru.destroy.restapi.crypto.AESCrypt;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;

public class TestPost {

    public static void main(String[] args) throws IOException, InterruptedException {

        AESCrypt crypt = AESCrypt.getCrypt();
        crypt.setKey("b2b1d40fca6a1746ab14db81f50d0c4c24ec58c3adbb16d315cbe8b2cf56b0bc");

        long time = System.currentTimeMillis();
        for (int i =0; i<100;i++) {
            String user = crypt.encrypt(shuffleString());
            String password = crypt.encrypt(shuffleString());
            String email = crypt.encrypt(shuffleString()+"@mail.ru");

            System.out.println("user = " + user);
            System.out.println("email = " + email);
            System.out.println("password = " + password);

            sendPost(user,password,email);
            sendGet(user,password);
        }
        time = System.currentTimeMillis()-time;

        System.out.println("Time taken for 100 post and get requests: "+time+"ms");
    }

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    // Говно-рандомизация. Было лень писать самому
    public static String shuffleString() {
        String input = generateRandomString(10);
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }
        return new String(characters);
    }



    // пример post запроса
    private static void sendPost(String user, String pas, String email) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:8080/api/post?user="+user+"&password="+pas+"&email="+email))
                .POST(HttpRequest.BodyPublishers.noBody()).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());

    }

    // пример get запроса
    private static void sendGet(String user, String pas) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:8080/api/get?user="+user+"&password="+pas))
                .GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());

    }

}
