package ru.itmo.infra.client;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class NalogRuClient {

    private static final String BASE_URL = "https://egrul.nalog.ru/";
    private static final String POST_URL = BASE_URL;
    private static final String GET_URL_TEMPLATE = BASE_URL + "search-result/%s";

    private static String sendPostRequest(String inn) throws IOException {
        URL url = new URL(POST_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String params = "query=" + URLEncoder.encode(inn, StandardCharsets.UTF_8);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = params.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse.getString("key");
        }
    }

    private static JSONObject sendGetRequest(String key) throws IOException {
        URL url = new URL(String.format(GET_URL_TEMPLATE, key));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            return new JSONObject(response.toString());
        }
    }

    public static String getCompanyNameByInn(String inn) throws IOException {
        String key = sendPostRequest(inn);

        JSONObject jsonResponse = sendGetRequest(key);

        JSONArray rows = jsonResponse.getJSONArray("rows");
        if (!rows.isEmpty()) {
            JSONObject company = rows.getJSONObject(0);
            return company.getString("c");
        } else {
            return null;
        }
    }
}
