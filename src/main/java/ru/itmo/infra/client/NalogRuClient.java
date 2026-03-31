package ru.itmo.infra.client;

import lombok.extern.java.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


/**
 * Сервис для отправки запросов к сайту egrul.nalog.ru
 * для получения данных о компаниях по ИНН
 */
@Log
public class NalogRuClient {
    private static Long lastRequestTime = System.currentTimeMillis();   // Время последнего запроса к сайту
    private static final Long REQUEST_TIME_PERIOD = 2000L;              // Период, выдерживаемый между запросами
    private static final Integer CONNECTION_TIMEOUT = 3000;             // Таймаут для подключения к сайту
    private static final String BASE_URL = "https://egrul.nalog.ru/";
    private static final String POST_URL = BASE_URL;
    private static final String GET_URL_TEMPLATE = BASE_URL + "search-result/%s";

    private static String sendPostRequest(String inn) throws IOException {
        URL url = new URL(POST_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
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
            return jsonResponse.getString("t");
        }
    }

    private static JSONObject sendGetRequest(String key) throws IOException {
        URL url = new URL(String.format(GET_URL_TEMPLATE, key));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(CONNECTION_TIMEOUT);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            return new JSONObject(response.toString());
        }
    }

    /**
     * Метод для получения названия и региона регистрации компании по ИНН через запрос к egrul.nalog.ru
     * @param inn ИНН компании
     * @return массив из названия и региона регистрации компании, если была найдена ровно одна подходящая компания,
     * иначе массив из null
     */
    public static String[] getCompanyInfoByInn(String inn) throws IOException {
        JSONObject jsonResponse;

        synchronized(NalogRuClient.class) { // блок отправки запроса к сайту
            Long time_bonus = (long) (Math.random() * 250L);    // надбавка ко времени, для динамичности времени между
            // запросами

            // Ожидание таймаута для нового запроса. Необходимо выдерживать для избежания появления капчи
            while (System.currentTimeMillis() - lastRequestTime < REQUEST_TIME_PERIOD + time_bonus) {}

            // следующие запросы могут вызвать ошибку, поэтому время обновляется до и после
            lastRequestTime = System.currentTimeMillis();
            String key = sendPostRequest(inn);
            jsonResponse = sendGetRequest(key);
            lastRequestTime = System.currentTimeMillis();
        }

        try {
            JSONObject company;
            JSONArray rows = jsonResponse.getJSONArray("rows"); // массив с результатами
            for (int i = rows.length() - 1; i >= 0; i--) {
                company = rows.getJSONObject(i);
                // поля e и v есть только у недействительных компаний
                if (company.has("e") || company.has("v")) {
                    rows.remove(i);
                }
            }


            if (rows.length() == 1) {
                company = rows.getJSONObject(0);
                String companyName = company.has("c") ? company.getString("c") : company.getString("n");
                String region = company.getString("rn");
                return new String[] { companyName, region };
            } else {
                return new String[] { null, null };
            }
        } catch (JSONException ex) {
            return new String[] { null, null };
        }
    }
}
