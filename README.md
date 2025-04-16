# Как запускать

* Получить токен в телеграм-боте [@BotFather](https://t.me/BotFather)
* Создать файл `.env` в корневой директории с содержанием
    ```
    BOT_TOKEN="<токен>"
    PG_DSN="<dsn>"
    INN_CHECK="<true|false>"
    ```
* Для запуска в докер-контейнере:
  ```sh
  make up
  ```

* Для запуска через IntelliJ Idea нужно в Run Configuration указать Main Class `ru.itmo.Main`, а в Environment
  Variables -- путь до `.env`-файла