package ru.itmo.infra.handler.usecase.admin.forceupdate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.domain.type.PracticePlace;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.BadRequestException;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Перечисление полей, доступных для обновления через команду /forceupdate.
 * <p>
 * Каждое поле содержит:
 * <ul>
 *     <li>Множество alias-ов для парсинга команды (поддержка коротких и русских/английских вариантов)</li>
 *     <li>Функцию валидации значения</li>
 *     <li>Функцию для получения списка допустимых значений (для информативных ошибок)</li>
 * </ul>
 * <p>
 * Примеры использования:
 * <ul>
 *     <li>status, статус - поле статуса студента</li>
 *     <li>place, место, mp - место прохождения практики</li>
 *     <li>inn, инн, ic - ИНН компании</li>
 * </ul>
 *
 */
@Getter
@RequiredArgsConstructor
public enum ForceUpdateField {
    /**
     * Поле статуса студента.
     * Валидируется через enum {@link StudentStatus}.
     */
    STATUS(
            Set.of("статус", "status"),
            ForceUpdateField::validateStatus,
            StudentStatus::getAvailableValues
    ),

    /**
     * Поле места прохождения практики.
     * Валидируется через enum {@link PracticePlace}.
     */
    PRACTICE_PLACE(
            Set.of("место_практики", "place", "место", "mp"),
            ForceUpdateField::validatePracticePlace,
            PracticePlace::getAvailableValues
    ),

    /**
     * Поле формата практики.
     * Валидируется через enum {@link PracticeFormat}.
     */
    PRACTICE_FORMAT(
            Set.of("формат_практики", "format", "формат", "fp"),
            ForceUpdateField::validatePracticeFormat,
            PracticeFormat::getAvailableValues
    ),

    /**
     * Поле ИНН компании.
     * Валидация: ровно 10 цифр.
     */
    COMPANY_INN(
            Set.of("инн_компании", "inn", "инн", "ic"),
            ForceUpdateField::validateInn,
            () -> "10 цифр (например: 7801234567)"
    ),

    /**
     * Поле названия компании.
     * Валидация: любая непустая строка.
     */
    COMPANY_NAME(
            Set.of("имя_компании", "company", "компания", "kc"),
            ForceUpdateField::validateCompanyName,
            () -> "любая непустая строка"
    ),

    /**
     * Поле ФИО руководителя практики.
     * Валидация: любая непустая строка.
     */
    LEAD_FULL_NAME(
            Set.of("фио_руководителя", "lead", "руководитель", "fr"),
            ForceUpdateField::validateLeadFullName,
            () -> "любая строка (например: Иванов Иван Иванович)"
    ),

    /**
     * Поле телефона руководителя.
     * Валидация: российский формат телефона.
     */
    LEAD_PHONE(
            Set.of("телефон_руководителя", "phone", "телефон", "tp"),
            ForceUpdateField::validatePhone,
            () -> "формат телефона: +7XXXXXXXXXX, 8XXXXXXXXXX или XXXXXXXXXX"
    ),

    /**
     * Поле email руководителя.
     * Валидация: базовый формат email.
     */
    LEAD_EMAIL(
            Set.of("почта_руководителя", "email", "почта", "pe"),
            ForceUpdateField::validateEmail,
            () -> "формат email: example@mail.ru"
    ),

    /**
     * Поле должности руководителя.
     * Валидация: любая непустая строка.
     */
    LEAD_JOB_TITLE(
            Set.of("должность_руководителя", "title", "должность", "dr"),
            ForceUpdateField::validateLeadJobTitle,
            () -> "любая строка (например: Главный инженер)"
    );

    /**
     * Множество ключевых слов для распознавания поля.
     * Включает русские и английские варианты, а также короткие алиасы.
     */
    private final Set<String> aliases;

    /**
     * Функция валидации значения поля.
     */
    private final ValidationFunction validator;

    /**
     * Функция для получения списка допустимых значений.
     * Используется для формирования информативных сообщений об ошибках.
     */
    private final ValueSupplier availableValuesSupplier;

    /**
     * Регулярное выражение для валидации email.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    /**
     * Регулярное выражение для валидации телефона (российский формат).
     * Поддерживает форматы: +7XXXXXXXXXX, 8XXXXXXXXXX, XXXXXXXXXX, с разделителями.
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+7|8)?[\\s\\-]?\\(?\\d{3}\\)?[\\s\\-]?\\d{3}[\\s\\-]?\\d{2}[\\s\\-]?\\d{2}$"
    );

    /**
     * Находит поле по ключевому слову (алиасу).
     *
     * @param key ключевое слово из команды (может быть на русском или английском)
     * @return найденное поле или null, если ключ неизвестен
     */
    public static ForceUpdateField findByKey(String key) {
        if (key == null) {
            return null;
        }
        String normalizedKey = key.trim().toLowerCase();
        for (ForceUpdateField field : values()) {
            if (field.aliases.contains(normalizedKey)) {
                return field;
            }
        }
        return null;
    }

    /**
     * Проверяет, является ли ключ допустимым именем поля.
     *
     * @param key ключевое слово для проверки
     * @return true, если ключ допустим; false в противном случае
     */
    public static boolean isValidKey(String key) {
        return findByKey(key) != null;
    }

    /**
     * Возвращает строку со списком всех допустимых полей для справки.
     * Используется при вызове команды с ключом --help.
     *
     * @return форматированный список полей с их алиасами и допустимыми значениями
     */
    public static String getAvailableFieldsList() {
        StringBuilder sb = new StringBuilder();
        
        for (ForceUpdateField field : values()) {
            String mainAlias = field.aliases.iterator().next();
            
            String otherAliases = field.aliases.stream()
                    .skip(1)
                    .collect(Collectors.joining(", "));
            
            sb.append(mainAlias).append(":\n");
            if (!otherAliases.isEmpty()) {
                sb.append("  алиасы: ").append(otherAliases).append("\n");
            }
            sb.append("  значение: ").append(field.availableValuesSupplier.get()).append("\n\n");
        }
        
        return sb.toString();
    }

    /**
     * Возвращает множество всех alias-ов для автодополнения или подсказок.
     *
     * @return множество всех допустимых ключевых слов
     */
    public static Set<String> getAllAliases() {
        return Arrays.stream(values())
                .flatMap(field -> field.aliases.stream())
                .collect(Collectors.toSet());
    }

    /**
     * Возвращает строку со списком всех alias-ов для использования в командах.
     *
     * @return форматированный список "основной_алиас -> все_алиасы"
     */
    public static String getAliasesList() {
        StringBuilder sb = new StringBuilder();
        
        for (ForceUpdateField field : values()) {
            String mainAlias = field.aliases.iterator().next();
            String allAliases = String.join(", ", field.aliases);
            sb.append(mainAlias).append(" -> ").append(allAliases).append("\n");
        }
        
        return sb.toString();
    }

    /**
     * Валидирует значение статуса студента.
     *
     * @param value строковое значение статуса
     * @throws BadRequestException если значение недопустимо
     */
    private static void validateStatus(String value) throws BadRequestException {
        StudentStatus.valueOfIgnoreCaseChecked(value);
    }

    /**
     * Валидирует значение места практики.
     *
     * @param value строковое значение места практики
     * @throws BadRequestException если значение недопустимо
     */
    private static void validatePracticePlace(String value) throws BadRequestException {
        PracticePlace.valueOfIgnoreCaseChecked(value);
    }

    /**
     * Валидирует значение формата практики.
     *
     * @param value строковое значение формата практики
     * @throws BadRequestException если значение недопустимо
     */
    private static void validatePracticeFormat(String value) throws BadRequestException {
        PracticeFormat.valueOfIgnoreCaseChecked(value);
    }

    /**
     * Валидирует ИНН компании.
     * Требования: ровно 10 цифр.
     *
     * @param value строковое значение ИНН
     * @throws BadRequestException если ИНН невалиден
     */
    private static void validateInn(String value) throws BadRequestException {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException("ИНН не может быть пустым");
        }

        String digitsOnly = value.replaceAll("\\D", "");

        if (digitsOnly.length() != 10) {
            throw new BadRequestException(
                    "ИНН должен состоять из 10 цифр. Получено: \"%s\" (%d цифр)".formatted(value, digitsOnly.length())
            );
        }

        if (!digitsOnly.equals(value.trim()) && !value.trim().matches("\\d+")) {
            throw new BadRequestException(
                    "ИНН должен содержать только цифры. Получено: \"%s\"".formatted(value)
            );
        }
    }

    /**
     * Валидирует название компании.
     * Название компании не может быть пустым.
     *
     * @param value строковое значение названия компании
     * @throws BadRequestException если название пустое
     */
    private static void validateCompanyName(String value) throws BadRequestException {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException("Название компании не может быть пустым");
        }
    }

    /**
     * Валидирует ФИО руководителя практики.
     * ФИО не может быть пустым.
     *
     * @param value строковое значение ФИО
     * @throws BadRequestException если ФИО пустое
     */
    private static void validateLeadFullName(String value) throws BadRequestException {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException("ФИО руководителя не может быть пустым");
        }
    }

    /**
     * Валидирует номер телефона руководителя.
     * Допустимые форматы: +7XXXXXXXXXX, 8XXXXXXXXXX, XXXXXXXXXX, с разделителями.
     *
     * @param value строковое значение телефона
     * @throws BadRequestException если формат телефона невалиден
     */
    private static void validatePhone(String value) throws BadRequestException {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException("Телефон не может быть пустым");
        }

        String normalized = value.replaceAll("[\\s\\-]", "");

        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            throw new BadRequestException(
                    "Неверный формат телефона: \"%s\"\n" +
                    "Допустимые форматы:\n" +
                    "- +7XXXXXXXXXX\n" +
                    "- 8XXXXXXXXXX\n" +
                    "- XXXXXXXXXX\n" +
                    "(цифры могут быть разделены пробелами, дефисами или скобками)".formatted(value)
            );
        }
    }

    /**
     * Валидирует email руководителя.
     * Проверяется базовый формат email: user@domain.ru
     *
     * @param value строковое значение email
     * @throws BadRequestException если формат email невалиден
     */
    private static void validateEmail(String value) throws BadRequestException {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException("Email не может быть пустым");
        }

        if (!EMAIL_PATTERN.matcher(value.trim()).matches()) {
            throw new BadRequestException(
                    "Неверный формат email: \"%s\"\n" +
                    "Пример корректного email: example@mail.ru".formatted(value)
            );
        }
    }

    /**
     * Валидирует должность руководителя.
     * Должность не может быть пустой.
     *
     * @param value строковое значение должности
     * @throws BadRequestException если должность пустая
     */
    private static void validateLeadJobTitle(String value) throws BadRequestException {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException("Должность руководителя не может быть пустой");
        }
    }

    /**
     * Функциональный интерфейс для валидации значения поля.
     */
    @FunctionalInterface
    public interface ValidationFunction {
        /**
         * Валидирует значение поля.
         *
         * @param value значение для валидации
         * @throws BadRequestException если значение невалидно
         */
        void validate(String value) throws BadRequestException;
    }

    /**
     * Функциональный интерфейс для получения списка допустимых значений.
     */
    @FunctionalInterface
    public interface ValueSupplier {
        /**
         * Возвращает строку с описанием допустимых значений.
         *
         * @return описание допустимых значений
         */
        String get();
    }
}
