DO $$ BEGIN
    CREATE TYPE st_status AS ENUM (
        'NOT_REGISTERED',
        'REGISTERED',
        'PRACTICE_IN_ITMO_MARKINA',
        'COMPANY_INFO_WAITING_APPROVAL',
        'COMPANY_INFO_RETURNED',
        'PRACTICE_APPROVED',
        'APPLICATION_WAITING_SUBMISSION',
        'APPLICATION_WAITING_APPROVAL',
        'APPLICATION_RETURNED',
        'APPLICATION_WAITING_SIGNING',
        'APPLICATION_SIGNED'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE st_practice_format AS ENUM (
        'NOT_SPECIFIED',
        'OFFLINE',
        'HYBRID',
        'ONLINE'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE st_practice_place AS ENUM (
        'NOT_SPECIFIED',
        'ITMO_MARKINA',
        'ITMO_UNIVERSITY',
        'OTHER_COMPANY'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE company_request_status AS ENUM (
        'PENDING',
        'APPROVED',
        'REJECTED'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

CREATE TABLE IF NOT EXISTS tg_user (
    chat_id                 bigint              PRIMARY KEY,
    is_admin                boolean             NOT NULL DEFAULT FALSE,
    is_banned               boolean             NOT NULL DEFAULT FALSE,
    username                text                NOT NULL
);

CREATE TABLE IF NOT EXISTS admin_token (
    token                   uuid                PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS edu_stream (
    name                    text                PRIMARY KEY CHECK (name <> ''),
    year                    smallint            NOT NULL,
    date_from               date                NOT NULL,
    date_to                 date                NOT NULL
);

CREATE TABLE IF NOT EXISTS practice_format (
    id                      bigserial           PRIMARY KEY,
    code                    text                NOT NULL,
    display_name            text                NOT NULL,
    is_active               boolean             NOT NULL DEFAULT TRUE,
    created_at              timestamp           NOT NULL DEFAULT now(),
    updated_at              timestamp           NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_practice_format_code_unique ON practice_format (lower(code));
CREATE UNIQUE INDEX IF NOT EXISTS idx_practice_format_display_name_unique ON practice_format (lower(display_name));

INSERT INTO practice_format (code, display_name)
VALUES
    ('OFFLINE', 'Очно'),
    ('HYBRID', 'Очно с применением дистанционных технологий'),
    ('ONLINE', 'С применением дистанционных технологий')
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS student (
    chat_id                 bigint              REFERENCES tg_user(chat_id) ON DELETE CASCADE DEFAULT NULL,
    edu_stream_name         text                NOT NULL REFERENCES edu_stream(name) ON DELETE CASCADE ON UPDATE CASCADE,
    isu                     int                 NOT NULL,
    st_group                varchar(8)          NOT NULL,
    fullname                text                NOT NULL,
    status                  st_status           NOT NULL DEFAULT 'NOT_REGISTERED',
    application             text                NOT NULL DEFAULT '',
    notifications           text                NOT NULL DEFAULT '',
    comments                text                NOT NULL DEFAULT '',
    call_status_comments    text                NOT NULL DEFAULT '',
    practice_place          st_practice_place   NOT NULL DEFAULT 'NOT_SPECIFIED',
    practice_format         st_practice_format  NOT NULL DEFAULT 'NOT_SPECIFIED',
    company_inn             bigint              DEFAULT NULL,
    company_name            text                DEFAULT NULL,
    company_lead_fullname   text                DEFAULT NULL,
    company_lead_phone      text                DEFAULT NULL,
    company_lead_email      text                DEFAULT NULL,
    company_lead_job_title  text                DEFAULT NULL,
    practice_option_id      bigint              DEFAULT NULL,
    cell_hex_color          varchar(32)         NOT NULL DEFAULT 'FFFFFF',
    managed_manually        boolean             NOT NULL DEFAULT false,
    exported_at             timestamp           NOT NULL DEFAULT now(),
    updated_at              timestamp           NOT NULL DEFAULT now(),
    application_bytes       bytea               DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS practice_option (
    id                      bigserial           PRIMARY KEY,
    title                   text                NOT NULL UNIQUE CHECK (title <> ''),
    enabled                 boolean             NOT NULL DEFAULT TRUE,
    requires_itmo_info      boolean             NOT NULL DEFAULT FALSE,
    requires_company_info   boolean             NOT NULL DEFAULT TRUE
);

ALTER TABLE practice_option
    ADD COLUMN IF NOT EXISTS requires_itmo_info boolean NOT NULL DEFAULT FALSE;
ALTER TABLE practice_option
    ADD COLUMN IF NOT EXISTS requires_company_info boolean NOT NULL DEFAULT TRUE;

ALTER TABLE student
    ADD COLUMN IF NOT EXISTS practice_option_id bigint DEFAULT NULL;

DO $$ BEGIN
    ALTER TABLE student
        ADD CONSTRAINT fk_student_practice_option
            FOREIGN KEY (practice_option_id) REFERENCES practice_option(id) ON DELETE SET NULL;
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

INSERT INTO practice_option (title, enabled, requires_itmo_info, requires_company_info) VALUES
('Практика в лаборатории ИТМО', TRUE, TRUE, FALSE),
('Практика в сторонней компании', TRUE, FALSE, TRUE),
('Практика в ИТМО (работаю)', TRUE, TRUE, FALSE),
('Практика на факультете', TRUE, TRUE, FALSE),
('Практика по целевому обучению', TRUE, FALSE, TRUE)
ON CONFLICT (title) DO NOTHING;

UPDATE student s
SET practice_option_id = po.id
FROM practice_option po
WHERE s.practice_option_id IS NULL
  AND (
    (s.practice_place = 'ITMO_UNIVERSITY' AND po.title = 'Практика в лаборатории ИТМО')
    OR (s.practice_place = 'OTHER_COMPANY' AND po.title = 'Практика в сторонней компании')
    OR (s.practice_place = 'ITMO_MARKINA' AND po.title = 'Практика в ИТМО (работаю)')
  );

ALTER TABLE student ADD IF NOT EXISTS practice_format_id bigint;

DROP INDEX IF EXISTS idx_student_practice_format_id;
CREATE INDEX IF NOT EXISTS idx_student_practice_format_id_lookup ON student (practice_format_id);

-- backfill
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_student_practice_format_id'
    ) THEN
        ALTER TABLE student
            ADD CONSTRAINT fk_student_practice_format_id
            FOREIGN KEY (practice_format_id) REFERENCES practice_format(id)
            ON DELETE SET NULL;
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS idx_pk_student ON student (chat_id, edu_stream_name) WHERE chat_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_isu_edu_stream_name_student ON student (isu, edu_stream_name) WHERE chat_id IS NULL;

CREATE TABLE IF NOT EXISTS guide_section (
    id                   serial PRIMARY KEY,
    slug                 text                NOT NULL UNIQUE CHECK (slug <> ''),
    title                text                NOT NULL CHECK (title <> ''),
    menu_order           int                 NOT NULL UNIQUE,
    command              text                NOT NULL UNIQUE CHECK (command <> ''),
    is_active            boolean             NOT NULL DEFAULT TRUE,
    is_hidden            boolean             NOT NULL DEFAULT FALSE
);

ALTER TABLE guide_section
    ADD COLUMN IF NOT EXISTS is_hidden boolean NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS guide_subsection (
    id                       serial PRIMARY KEY,
    section_id               int             NOT NULL REFERENCES guide_section(id) ON DELETE CASCADE,
    title                    text            NOT NULL CHECK (title <> ''),
    body                     text            NOT NULL DEFAULT '',
    prev_subsection_id       int             REFERENCES guide_subsection(id) ON DELETE SET NULL,
    next_subsection_id       int             REFERENCES guide_subsection(id) ON DELETE SET NULL,
    item_order               int             NOT NULL,
    updated_at               timestamp       NOT NULL DEFAULT now(),
    UNIQUE (section_id, item_order)
);

CREATE INDEX IF NOT EXISTS idx_guide_subsection_section ON guide_subsection (section_id);

INSERT INTO guide_section (slug, title, menu_order, command, is_active) VALUES
('practice_options', 'Способы прохождения практики', 1, '/practice_options', true),
('practice_stages', 'Этапы прохождения практики', 2, '/practice_stages', true),
('place_selection', 'Выбор места прохождения практики', 3, '/place_selection', true),
('application_process', 'Процесс работы с заявкой', 4, '/application_process', true),
('general_info', 'Общее', 5, '/general_info', true)
ON CONFLICT (slug) DO UPDATE SET
    title = EXCLUDED.title,
    menu_order = EXCLUDED.menu_order,
    command = EXCLUDED.command,
    is_active = EXCLUDED.is_active;

INSERT INTO guide_section (slug, title, menu_order, command, is_active, is_hidden)
VALUES ('registration_instruction', 'Инструкция по регистрации', 900, '/_registration_instruction', true, true)
ON CONFLICT (slug) DO UPDATE SET
    title = EXCLUDED.title,
    menu_order = EXCLUDED.menu_order,
    command = EXCLUDED.command,
    is_active = EXCLUDED.is_active,
    is_hidden = EXCLUDED.is_hidden;

INSERT INTO guide_subsection (section_id, title, body, item_order)
SELECT s.id, 'Инструкция по регистрации', $reg_instr$
При регистрации, требуется указать

* название потока  
* номер ИСУ

**Текущее название потока:**
Весна 2025/2026

Введите название вашего потока
$reg_instr$, 1
FROM guide_section s WHERE s.slug = 'registration_instruction'
ON CONFLICT (section_id, item_order) DO NOTHING;

INSERT INTO guide_subsection (section_id, title, body, item_order)
SELECT s.id, 'Содержание', $toc_po$
Данный раздел мануала описывает, какие способы доступны студентам для прохождения производственной практики.

Всего есть 4 возможных варианта:

• Практика у Маркиной Т.А.

• Практика в ИТМО

• В компании, подобранной самостоятельно

• В компании, предложенной куратором

Переходите по кнопкам ниже для изучения каждого из них по отдельности.
$toc_po$, 1
FROM guide_section s WHERE s.slug = 'practice_options'
ON CONFLICT (section_id, item_order) DO NOTHING;

INSERT INTO guide_subsection (section_id, title, body, item_order)
SELECT s.id, 'Практика у Маркиной Т.А.', $po_m$
В данном случае практика проходит под руководством Маркиной Т.А. в подразделении ИТМО.

В рамках данного способа прохождения практики вам будет предложено в команде, состоящей из таких же практикантов, или индивидуально, выполнить задачу, поставленную заказчиком в лице Маркиной Т.А.

Для согласования данного способа прохождения практики требуется:

* Договориться с Маркиной Т.А.
* Выбрать соответствующий вариант в боте
$po_m$, 2
FROM guide_section s WHERE s.slug = 'practice_options'
ON CONFLICT (section_id, item_order) DO NOTHING;

INSERT INTO guide_subsection (section_id, title, body, item_order)
SELECT s.id, 'Практика В ИТМО', $po_itmo$
Практику в ИТМО возможно пройти в нескольких случаях:

* в лаборатории
* по месту работы в ИТМО
* на факультете – без привязки к лаборатории и без трудоустройства

Для согласования прохождения практики в лаборатории ИТМО требуется:

* Договориться с руководителем лаборатории
* Выбрать соответствующий вариант в боте, заполнить данные

Для согласования данного прохождения практики по месту работы в ИТМО требуется:

* Договориться с руководителем лаборатории
* Согласовать место практики с Маркиной Т.А.
* Подписать уведомление у Маркиной Т.А.
* Выбрать соответствующий вариант в боте, заполнить данные

Чтобы выполнить второй пункт (согласовать работу в ИТМО как способ прохождения практики) – требуется отправить письмо следующего содержания:

* ФИО студента
* Табельный номер студента
* Группу студента
* Подразделение в ИТМО
* ФИО руководителя практики
* Табельный номер руководителя практики
* Ссылку на руководителя в ИСУ
* Электронную почту руководителя практики в домене @itmo.ru
* Мобильный (не городской) номер телефона руководителя

Пример письма:

Я, Иванов Иван Иванович (345678), студент группы P3XXX, работаю в отделе разработки ИТМО и хочу проходить практику по месту своей работы.

Мой руководитель практики: Павлов Павел Павлович (123456), +7(999)999-99-99, pppavlov@itmo.ru. Ссылка на руководителя в ИСУ: https://isu.ifmo.ru/person/123456
$po_itmo$, 3
FROM guide_section s WHERE s.slug = 'practice_options'
ON CONFLICT (section_id, item_order) DO NOTHING;

INSERT INTO guide_subsection (section_id, title, body, item_order)
SELECT s.id, 'В сторонней компании', $po_ext$
В данном случае практика проходит в сторонней компании – из списка предложенных куратором практики, либо согласованной студентом самостоятельно.

Если компанию подобрал сам студент, то для согласования данного способа прохождения практики требуется:

* Согласовать место практики с Маркиной Т.А.
* Подписать заявку в компании
* Выбрать соответствующий вариант в боте, заполнить данные
* Загрузить скан заявки в боте
* Подписать уведомление у Маркиной Т.А.

Для выполнения первого пункта (согласования компании как места прохождения практики), вам необходимо направить на почту markina_t@itmo.ru следующее письмо:

**Тема:**

Согласование места практики 4 курс, ОП <Название обр. программы>

**Содержание:**

1. ИНН
2. Полное наименование компании
3. Контакты руководителя практики от компании:
- ФИО полностью
- Корпоративный email (в домене компании)
- Российский мобильный телефон (начинается с +7 или 8)

Остальные шаги согласования описаны в следующих разделах данного мануала.

Если компанию предложил куратор практики, то порядок согласования может отличаться. Например, заявку может составлять преподаватель, и в таком случае, требуется:

* Договориться с преподавателем, убедиться, что он включил вас в заявку
* Подписать уведомление у Маркиной Т.А.
* Выбрать соответствующий вариант в боте, заполнить данные
$po_ext$, 4
FROM guide_section s WHERE s.slug = 'practice_options'
ON CONFLICT (section_id, item_order) DO NOTHING;

INSERT INTO guide_subsection (section_id, title, body, item_order)
SELECT s.id, 'Согласование способа прохождения производственной практики', $st_agree$
Порядок согласования зависит от места прохождения практики и описан в разделе мануала «Способы прохождения практики».

/practice_options
$st_agree$, 1
FROM guide_section s WHERE s.slug = 'practice_stages'
ON CONFLICT (section_id, item_order) DO NOTHING;

INSERT INTO guide_subsection (section_id, title, body, item_order)
SELECT s.id, 'Подписание заявки о прохождении практики', $st_app$
Процесс описан в разделе мануала «Работа с заявкой на практику».

/application_process
$st_app$, 2
FROM guide_section s WHERE s.slug = 'practice_stages'
ON CONFLICT (section_id, item_order) DO NOTHING;

INSERT INTO guide_subsection (section_id, title, body, item_order)
SELECT s.id, 'Формирование и утверждение индивидуального задания (ИЗ)', $st_iz$
Перед прохождением производственной практики вам необходимо будет заполнить на странице практики в my.itmo.ru индивидуальное задание.

**В ИЗ есть 2 обязательных для всех этапа:**

Первый этап: *Инструктаж обучающегося*

Инструктаж обучающегося по ознакомлению с требованиями охраны труда, техники безопасности, пожарной безопасности, а также правилами внутреннего трудового распорядка

Последний этап: *Оформление отчётных документов и получение отзыва руководителя*

1. Должно быть подробное описание выполнения задач по этапам. Результаты задания необходимо разместить в приложения. 2. Оформление отчёта должно быть выполнено в соответствии с методическим пособием (https://books.ifmo.ru/file/pdf/2622.pdf) 3. Структура документа: титульный лист, введение, основная часть, заключение, приложения. 4. В основной части подробно описывается выполнение задач ***X-Y*** этапов, в приложении помещаются результаты данных этапов. 5. Отчёт необходимо подгрузить в модуле практика как "письменный отчёт"

**Общие требования:**

При создании всех этапов ИЗ, **кроме инструктажа,** должны указывать конкретные даты начала и конца каждого этапа. Индивидуальные задания, в которых нет дат, а есть только длительности – не принимаются.

Описание задач должно содержать следующую информацию:

* Что должно быть сделано
* Требования к решению
* Критерии приёмки
* Как успешное выполнение задачи будет отражено в отчёте
$st_iz$, 3
FROM guide_section s WHERE s.slug = 'practice_stages'
ON CONFLICT (section_id, item_order) DO NOTHING;

INSERT INTO guide_subsection (section_id, title, body, item_order)
SELECT s.id, 'Процесс работы с заявкой', $ap_main$
**1. Подписание заявки в компании**
Получите подпись и печать компании на заявке о прохождении практики. Если договор с компанией НЕ подписан, то вам необходимо и его подписать

**Требования к заявке:**
- Должна быть подпись ответственного лица от компании (НЕ руководителя практики)
- Должна быть печать компании
- Должен быть проставлен номер договора
- Все данные должны соответствовать информации из письма

**Важно:**
Сделайте качественный скан заявки после подписания её с двух сторон, так как оригинал может быть утерян, а по скан-копии можно восстановить документ.

**2. Загрузка скана заявки в ТГ-бот**
- После успешной регистрации загрузите скан заявки
- Заявка должна быть с печатью с двух сторон (компания и ИТМО)
- Используйте функцию загрузки документов в боте
$ap_main$, 1
FROM guide_section s WHERE s.slug = 'application_process'
ON CONFLICT (section_id, item_order) DO NOTHING;

INSERT INTO guide_subsection (section_id, title, body, item_order)
SELECT s.id, 'Выбор места прохождения практики', $pl_main$

Для того, чтобы выбрать место прохождения практики в боте, необходимо нажать на пункт меню "Выбор места практики" или воспользоваться командой /choose_place.

Это доступно только в том случае, если студент выбирает место впервые, или же куратор практики вернул данные о компании на доработку.

На выбор доступно 3 варианта:

* Практика у Маркиной Т.А.
* Практика в ИТМО
* В сторонней компании

В зависимости от варианта, потребуется ввести разную информацию.

**Практика в ИТМО**

Бот запросит следующую информацию:

1. ФИО руководителя практики
2. Подразделение ИТМО, в котором будет проходить практика

**В сторонней компании**

Бот запросит следующую информацию:

1. Формат практики: "Очная" / "Гибридная" / "Дистанционная"
2. ИНН компании
3. ФИО руководителя практики
4. Должность руководителя практики
5. Номер телефона руководителя практики от компании
6. Корпоративная почта руководителя практики от компании

**Практика у Маркиной Т.А:**

Не требуется вводить дополнительную информацию
$pl_main$, 1
FROM guide_section s WHERE s.slug = 'place_selection'
ON CONFLICT (section_id, item_order) DO NOTHING;

INSERT INTO guide_subsection (section_id, title, body, item_order)
SELECT s.id, 'Контакты руководителя практики', $gi_contacts$

Маркина Татьяна Анатольевна

Telegram: https://t.me/TatianaMark

Почта: markina_t@itmo.ru
$gi_contacts$, 1
FROM guide_section s WHERE s.slug = 'general_info'
ON CONFLICT (section_id, item_order) DO NOTHING;

-- Новые разделы мануала
DELETE FROM guide_section WHERE slug IN ('company_lead_mgmt', 'company_lead_teacher');

UPDATE guide_section SET menu_order = 9000
WHERE slug = 'search_students' AND menu_order <> 7;

INSERT INTO guide_section (slug, title, menu_order, command, is_active)
VALUES ('company_lead_student', 'Данные руководителя от компании', 6, '/lead_guide', true)
ON CONFLICT (slug) DO UPDATE SET
    title = EXCLUDED.title,
    menu_order = EXCLUDED.menu_order,
    command = EXCLUDED.command,
    is_active = EXCLUDED.is_active;

INSERT INTO guide_section (slug, title, menu_order, command, is_active)
VALUES ('search_students', 'Поиск студентов (для преподавателей)', 7, '/search_guide', true)
ON CONFLICT (slug) DO UPDATE SET
    title = EXCLUDED.title,
    menu_order = EXCLUDED.menu_order,
    command = EXCLUDED.command,
    is_active = EXCLUDED.is_active;

-- Подразделы: Данные руководителя от компании (для студентов)
INSERT INTO guide_subsection (section_id, title, body, item_order)
SELECT s.id, 'Содержание', $cls_toc$
В этом разделе описаны команды для просмотра и изменения данных руководителя практики от компании.

**Для студентов:**
Доступные действия:
• Просмотр текущих данных руководителя
• Изменение ФИО (полностью или отдельно фамилии, имени, отчества)
• Изменение номера телефона
• Изменение email
• Изменение должности

**Для преподавателей:**
Изменение данных руководителя практики выполняется студентом самостоятельно. Если требуется изменить данные — направьте студента к использованию команды /change_lead_info.
$cls_toc$, 1
FROM guide_section s WHERE s.slug = 'company_lead_student'
ON CONFLICT (section_id, item_order) DO NOTHING;

INSERT INTO guide_subsection (section_id, title, body, item_order)
SELECT s.id, 'Просмотр данных руководителя', $cls_view$
Чтобы просмотреть текущие данные руководителя практики от компании, воспользуйтесь одним из способов:

1. Нажмите кнопку «Данные руководителя от компании» в главном меню
2. Используйте команду /change_lead_info
3. В открывшемся меню нажмите «Посмотреть данные руководителя»

Также можно использовать команду /view_lead_info напрямую.

Бот отобразит:
• ФИО руководителя
• Номер телефона
• Email
• Должность
$cls_view$, 2
FROM guide_section s WHERE s.slug = 'company_lead_student'
ON CONFLICT (section_id, item_order) DO NOTHING;

INSERT INTO guide_subsection (section_id, title, body, item_order)
SELECT s.id, 'Изменение данных руководителя', $cls_change$
Для изменения данных руководителя практики:

1. Нажмите «Данные руководителя от компании» в главном меню или используйте команду /change_lead_info
2. Выберите нужное поле для изменения из списка кнопок
3. Введите новое значение
4. Бот подтвердит успешное обновление

**Доступные поля для изменения:**

• **ФИО руководителя** — полное ФИО (Фамилия Имя Отчество)
• **Фамилия руководителя** — только фамилия (остальные части ФИО сохраняются)
• **Имя руководителя** — только имя
• **Отчество руководителя** — только отчество
• **Телефон руководителя** — номер телефона (формат: +7XXXXXXXXXX или 8XXXXXXXXXX)
• **Email руководителя** — адрес электронной почты
• **Должность руководителя** — текст должности

**Важно:**
• Изменение отдельных частей ФИО (фамилии, имени, отчества) возможно только если текущее ФИО содержит ровно 3 части
• При изменении телефона и email проверяется корректность формата
• Команды доступны только после заполнения данных о компании
$cls_change$, 3
FROM guide_section s WHERE s.slug = 'company_lead_student'
ON CONFLICT (section_id, item_order) DO NOTHING;

-- Подразделы: Поиск студентов (для преподавателей)
INSERT INTO guide_subsection (section_id, title, body, item_order)
SELECT s.id, 'Содержание', $ss_toc$
В этом разделе описаны команды поиска информации о студентах, доступные преподавателю.

Доступные команды:
• Поиск по ISU номеру
• Поиск по группе и ФИО
$ss_toc$, 1
FROM guide_section s WHERE s.slug = 'search_students'
ON CONFLICT (section_id, item_order) DO NOTHING;

INSERT INTO guide_subsection (section_id, title, body, item_order)
SELECT s.id, 'Поиск по ISU', $ss_isu$
Для поиска студента по номеру ISU используйте команду:

/search_by_isu <номер ISU>

**Пример:**
/search_by_isu 345678

Бот выполнит поиск в текущем потоке и отобразит найденную информацию о студенте:
• ФИО, ISU, группа, статус
• Формат практики, компания
• Данные руководителя практики от компании (ФИО, телефон, email, должность)
• ChatId студента в Telegram
$ss_isu$, 2
FROM guide_section s WHERE s.slug = 'search_students'
ON CONFLICT (section_id, item_order) DO NOTHING;

INSERT INTO guide_subsection (section_id, title, body, item_order)
SELECT s.id, 'Поиск по группе и ФИО', $ss_group$
Для поиска студентов по группе и ФИО (или части ФИО) используйте команду:

/search_by_group <группа> <ФИО или часть ФИО>

**Примеры:**
/search_by_group M3100 Иванов
/search_by_group P3200 Петров Иван

Бот выполнит поиск в текущем потоке по указанной группе и частичному совпадению ФИО. Результаты отображаются в том же формате, что и при поиске по ISU.

**Примечание:** поиск выполняется по подстроке — достаточно указать фамилию или её часть.
$ss_group$, 3
FROM guide_section s WHERE s.slug = 'search_students'
ON CONFLICT (section_id, item_order) DO NOTHING;

UPDATE guide_subsection u SET
    prev_subsection_id = o.prev_id,
    next_subsection_id = o.next_id
FROM (
    SELECT id,
           lag(id) OVER (PARTITION BY section_id ORDER BY item_order) AS prev_id,
           lead(id) OVER (PARTITION BY section_id ORDER BY item_order) AS next_id
    FROM guide_subsection
) o
WHERE u.id = o.id;

CREATE TABLE IF NOT EXISTS company_approval_request (
    id                      bigserial               PRIMARY KEY,
    student_chat_id         bigint                  NOT NULL REFERENCES tg_user(chat_id) ON DELETE CASCADE,
    edu_stream_name         text                    NOT NULL REFERENCES edu_stream(name) ON DELETE CASCADE ON UPDATE CASCADE,
    inn                     bigint                  NOT NULL,
    company_name            text                    NOT NULL,
    company_address         text                    ,
    practice_format         st_practice_format      NOT NULL,
    company_lead_fullname   text                    NOT NULL,
    company_lead_phone      text                    NOT NULL,
    company_lead_email      text                    NOT NULL,
    company_lead_job_title  text                    NOT NULL,
    requires_spb_office_approval boolean            NOT NULL DEFAULT FALSE,
    status                  company_request_status  NOT NULL DEFAULT 'PENDING',
    processed_by_chat_id    bigint                  DEFAULT NULL REFERENCES tg_user(chat_id) ON DELETE SET NULL,
    created_at              timestamp               NOT NULL DEFAULT now(),
    processed_at            timestamp               DEFAULT NULL
);

ALTER TABLE company_approval_request
    ADD COLUMN IF NOT EXISTS requires_spb_office_approval boolean NOT NULL DEFAULT FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS idx_company_approval_request_pending
    ON company_approval_request (student_chat_id, edu_stream_name)
    WHERE status = 'PENDING';

CREATE TABLE IF NOT EXISTS cached_inn (
    company_inn             varchar(10)         PRIMARY KEY,
    name                    text                NOT NULL,
    region                  text                NOT NULL,
    cached_at               timestamp           NOT NULL DEFAULT now()
);

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_enum WHERE enumlabel = 'APPLICATION_PHOTO_UPLOADED'
                   AND enumtypid = (SELECT oid FROM pg_type WHERE typname = 'st_status')) THEN
        ALTER TYPE st_status ADD VALUE 'APPLICATION_PHOTO_UPLOADED' AFTER 'APPLICATION_WAITING_SIGNING';
    END IF;
END $$;

ALTER TABLE student ADD COLUMN IF NOT EXISTS signed_photo_path text DEFAULT NULL;
