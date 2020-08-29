# SimpleCabinet (Backend)
Эта часть кабинета является модулем для лаунчсервера
# Installation
 - Скопируйте файл модуля(**SimpleCabinet-XXXX.jar** в папку modules лаунчсервера)
 - Запустите лаунчсервер для создания конфига модуля(он будет находится в `config/SimpleCabinetModule/Config.json`)
# Шаг 1: Подключение к базе данных
## PostgreSQL
Скопируйте следующий код в LaunchServerConfig.json:
```json
  "dao": {
     "type": "simplecabinet",
     "driver": "org.postgresql.Driver",
     "url": "jdbc:postgresql://localhost/launchserver",
     "username": "launchserver",
     "password": "xxxxx",
     "pool_size": "4"
  }
```
## MySQL
Скопируйте следующий код в LaunchServerConfig.json:
```json
  "dao": {
     "type": "simplecabinet",
     "driver": "com.mysql.jdbc.Driver",
     "url": "jdbc:mysql://localhost/launchserver",
     "username": "launchserver",
     "password": "xxxxx",
     "pool_size": "4",
     "stringUUID": true,
     "dialect": "org.hibernate.dialect.MySQL5Dialect"
  }
```
Диалект влияет на возможности, которые будут использоваться при работе. Если какой то возможности будет не хватать - Hibernate будет её эмулировать, используя вспомогательные таблицы. В некоторых случаях при непраивльном выборе диалекта будут ошибки, а в других - просто снижение производительности.
### Список диалектов для MySQL и MariaDB
| Класс           | Версия  |
| ------------- |:-------------:|
org.hibernate.dialect.MySQL8Dialect   |   MySQL 8
org.hibernate.dialect.MySQL5Dialect   |     MySQL 5.x
org.hibernate.dialect.MySQLDialect    |     MySQL (до 5.x)
org.hibernate.dialect.MariaDB103Dialect  |  MariaDB 10.3 и новее
org.hibernate.dialect.MariaDB102Dialect  |  MariaDB 10.2
org.hibernate.dialect.MariaDB10Dialect   |  MariaDB 10.0 и 10.1
org.hibernate.dialect.MariaDB53Dialect   | MariaDB 5.3 и новее 5.х
org.hibernate.dialect.MariaDBDialect     | MariaDB 5.1 и 5.2
## Другие БД
## Создание таблиц
Для создания и обновления таблиц используется опция `-Dhibernate.hbm2ddl.auto`, которая может иметь следующие значения:
| Значение           | Описание  |
| ------------- |-------------|
validate | Проверяет правильнотсь схемы таблиц без каких либо изменений в базе данных
update | Обновит схему таблиц без потери данных
create | Создаст таблицы **с уничтожением данных**
create-drop | Создаст таблицы **с уничтожением данных** и **удалит их при закрытии лаунчсервера**
none | Ничего не делать (по умолчанию)
# Шаг 2: Настройка AuthProvider/AuthHandler/HWIDProvider
Для авторизации через SimpleCabinet укажите следующие параметры в секции auth:
```json
      "provider": {
        "type": "cabinet"
      },
      "handler": {
        "type": "hibernate"
      },
```
Для использования HWID следуйте гайду на официальной вики лаунчера и укажите следующий HWIDProvider:
```json
  "protectHandler": {
    "profileWhitelist": {},
    "allowUpdates": [],
    "enableHardwareFeature": true,
    "provider": {
        "warningSpoofingLevel": 0.5,
        "criticalCompareLevel": 1.0,
        "type": "cabinet"
    },
    "type": "advanced"
  },
```
После этого шага перезапустите лаунчсервер для приминения изменений
# Шаг 3: Настройка конфигурации
```json
{
  "maxSkin": {
    "maxHeight": 1024, //Максимальная высота картинки
    "maxWidth": 512, //Максимальная ширина картинки
    "maxBytes": 1048576, //Максимальный размер(в байтах)
    "url": "updates/skins/%s.png", //Путь относительно лаунчсервера, куда будут загружены скины
    "useUuidInUrl": false //Использовать UUID вместо никнейма
  },
  "maxCloak": { //Аналогичен maxSkin
    "maxHeight": 512,
    "maxWidth": 256,
    "maxBytes": 262144,
    "url": "updates/cloaks/%s.png",
    "useUuidInUrl": false
  }
}
```
# Миграция
SimpleCabinet поддерживает возможность миграции с другой CMS, если та использует поддерживаемый алгоритм хеширования пароля.
- Первым шагом укажите источник, откуда мигрировать. Добавьте следующий код в конфигурацию **модуля** SimpleCabinet:
```json
         "migratorSource": {
            "address": "localhost",
            "port": 3306,
            "username": "launchserver",
            "password": "password",
            "database": "db",
            "timezone": "UTC"
          }
```
Все параметры аналогичны mySQLHolder из MySQLAuthProvider/MySQLAuthHandler. После перезапуска лаунчсервера выполните следуюющую команду для начала базовой миграции:
`cabinet migrator BCRYPT "SELECT yusername as username, yuuid as uuid, ypassword as password FROM your_users_table"`, где yusername - поле с именем пользователя. yuuid - поле с uuid, ypassword - поле с паролем. BCRYPT - тип хеширования вашего пароля.
