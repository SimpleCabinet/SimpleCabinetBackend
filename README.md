# SimpleCabinet (Backend)
Эта часть кабинета является модулем для лаунчсервера
# Установка модуля
 - Скопируйте файл модуля(**SimpleCabinet-XXXX.jar** в папку modules лаунчсервера)
 - Запустите лаунчсервер для создания конфига модуля(он будет находится в `config/SimpleCabinetModule/Config.json`)
# Автоматическая установка
- Выполните `cabinet helper install` и следуйте инструкциям по установке
- Выберите вашу базу данных: mysql или postgresql
- - Если вы выбрали mysql вам предложат ответить на вопрос `Используете ли вы MariaDB 10.3 или выше?`, на которой вы должны ответить Y - если у вас MariaDB 10.3 или выше или N в случае если у вас другая база данных.
- - Если вы ответите N - вам предложат выбрать диалект, соответствующий версии вашей базы данных. список диалектов [тут](https://github.com/SimpleCabinet/SimpleCabinetBackend#%D1%81%D0%BF%D0%B8%D1%81%D0%BE%D0%BA-%D0%B4%D0%B8%D0%B0%D0%BB%D0%B5%D0%BA%D1%82%D0%BE%D0%B2-%D0%B4%D0%BB%D1%8F-mysql-%D0%B8-mariadb). После выбора возвращайтесь к этой инструкции
- Введите адрес, имя пользователя, пароль, и название заранее созданной **чистой** базы данных. Если у вас есть уже установленная CMS - **создайте новую базу данных**, данные старой вы укажите познее во время миграции
- Согласитесь с удалением и пересозданием таблиц
- Во время создания таблиц вы можете получить длинные исключения `таблица не найдена`. **Не пугайтесь - всё в порядке, так и должно быть при первом создании таблиц**.
- Если вы получили сообщение об успешной установке - поздравляю, можете переходить к более детальной настройке
# Ручная установка: Шаг 1: Подключение к базе данных
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
## Что если вы выбрали неправильный диалект
- Остановите лаунчсервер и в `LaunchServer.json` измените диалект на правильный
- Удалите созданные таблицы из базы данных, во избежании сюрпризов
- Запустите лаунчсервер с опцией `-Dhibernate.hbm2ddl.auto=create` для пересоздания таблиц
# Ручная установка: Шаг 2: Настройка AuthProvider/AuthHandler/HWIDProvider
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
# Настройка конфигурации
```
{
  "schedulerCorePoolSize": 2, //Колличество потоков планировщика, используемых для нужд ЛК
  "workersCorePoolSize": 3, //Колличество рабочих потоков, используемых для нужд ЛК
  "uploads": [ //Разрешенные размеры скинов и плащей
    {
      "config": {
        "maxHeight": 64, //Максимальная высота
        "maxWidth": 64, //Максимальная ширина
        "maxBytes": 102400, //Максимальный размер в байтах
        "url": "updates/skins/%s.png", //Путь
        "useUuidInUrl": false //Использовать UUID вместо никнейма
      },
      "skinType": "SKIN" // SKIN для скина, CLOAK для плаща
    },
    {
      "config": {
        "maxHeight": 32,
        "maxWidth": 32,
        "maxBytes": 40960,
        "url": "updates/cloaks/%s.png",
        "useUuidInUrl": false
      },
      "skinType": "CLOAK"
    },
    {
      "config": {
        "maxHeight": 1024,
        "maxWidth": 1024,
        "maxBytes": 1048576,
        "url": "updates/skins/%s.png",
        "useUuidInUrl": false
      },
      "groupName": "HD", // Пользователи, не состоящие в этой группе, не будут учитыватся
      "skinType": "SKIN"
    },
    {
      "config": {
        "maxHeight": 512,
        "maxWidth": 512,
        "maxBytes": 524288,
        "url": "updates/cloaks/%s.png",
        "useUuidInUrl": false
      },
      "groupName": "HD",
      "skinType": "CLOAK"
    }
  ],
  "groups": [ // Список групп ЛК. Если на вашем проекте есть покупка привилегий, вы должны указать их этом разделе
    {
      "name": "HD",
      "displayName": "HD",
      "permission": 0,
      "priority": 1
    }
  ],
  "deliveryProviders": { // Методы выдачи товара, о них вы можете посмотреть в отдельном разделе
    "debug": {
     "type": "debug"
    }
  },
  "payments": { // Настройка платежных систем
    "unitPay": {
      "secretKey": "yourSecretKey",
      "projectId": 0,
      "testMode": false
    },
    "robokassa": {
      "merchantId": "yourMerchantId",
      "password1": "yourPassword1",
      "password2": "yourPassword2",
      "test": true
    }
  },
  "mail": { // Настройка отправки почты
    "host": "smtp.yandex.com",
    "port": 465,
    "auth": true,
    "username": "noreply@example.com",
    "password": "yourpassword",
    "from": "noreply@example.com"
  },
  "urls": { // Ссылки
    "frontendUrl": "https://cabinet.yoursite.ru", // Адрес фронтенда ЛК
    "shopPictureBaseUrl": "https://launcher.yoursite.ru/products/" // Адрес папки с картинками товаров
  },
  "migratorSource": { // Укажите данные вашей старой БД при необходимости миграции
    "address": "localhost",
    "port": 3306,
    "useSSL": false,
    "verifyCertificates": false,
    "username": "root",
    "password": "",
    "database": "launchserver",
    "enableHikari": false
  }
}
```
# Магазин
Магазин предметов/привилегий и других товаров работает по следующей схеме:  
- Администратор через ЛК или базу данных добавляет товары, которые могут покупать игроки
- Игрок, у которого достаточно donateMoney для покупки нажимает кнопку покупки, ЛК отправляет запрос создания заказа
- Если все условия соблюдены заказ получает номер и статус CREATED(создан) и передается в обработку внутри лаунчсервера
- Лаунчсервер устанавливает статус заказа PROCESSED и обращается к deliveryProvider по имени, записанному в sysDeliveryProvider. Если он не будет найден - заказ завершится с ошибкой
- deliveryProvider по возможности мгновенно выдает заказ и устанавливает статус FINISHED, либо, при невозможности мгновенной выдачи(например игрок отсутствует на сервере) устанавливает статус DELIVERY(ожидание действий игрока)
- Сторонний сервис или плагин(например плагины интеграции) по запросу игрока получают сведенья о заказе, выдает заказ полностью или частично и устанавливает статус FINISHED если выдача заказа полностью завершена. При частичной выдаче информация о невыданной части заказа отправляется лаунчсерверу
# Выдача привилегий
Выдача привилегий осуществляется с помощью двух типов deliveryProvider - `group` и `luckperms`
Тип `group` предлязначен для выдачи обычных групп, не выходящих за рамки ЛК(например группа HD). При создании товара достаточно указать только itemId - название группы так, как это описывается в конфигурации модуля. itemExtra и itemNbt игнорируются. В quantity указывается колличество дней(0 - навсегда), в течении которых будет действовать группа  
Конфигурация:  
```
    "group": {
     "type": "group"
    }
```
Тип `luckperms` дредлязначен для выдачи групп привилегий на сервере, используя базу данных LuckPerms. Поддерживается как MySQL, так и PostgreSQL.  
itemId - название группы, которую хотите выдавать. ItemExtra - название мира, в котором действует группа(`null` если в любом). ItemNbt - название сервера, в котором действует группа(`null` если в любом). При вставке itemId будет автоматически конвертирован в lowercase. Если существует группа ЛК с таким же названием - она будет выдана, а так же по ней будет считатся продление.  
Для MySQL:
```
"luckperms": {
      "mySQLSource": {
        "address": "localhost",
        "port": 3306,
        "username": "simplecabinet",
        "password": "intotheunknown",
        "database": "database?serverTimezone=UTC"
      },
    "sql": "INSERT INTO `lp_user_permissions` (`uuid`, `permission`, `value`, `server`, `world`, `expiry`, `contexts`) VALUES (?, ?, ?, ?, ?, ?, ?);",
    "type": "luckperms"
    }
```
Для PostgreSQL:
```
"luckperms": {
      "postgreSQLSource": {
        "address": "localhost",
        "port": 5432,
        "username": "simplecabinet",
        "password": "intotheunknown",
        "database": "database"
      },
    "sql": "INSERT INTO `lp_user_permissions` (`uuid`, `permission`, `value`, `server`, `world`, `expiry`, `contexts`) VALUES (?, ?, ?, ?, ?, ?, ?);",
    "type": "luckperms"
    }
```
## Выдача предметов
Для выдачи предметов существует только один тип deliveryProvider - `event`, но работать он может в двух режимах  
**Вы должны установить на свой сервер плагин интеграции**  
Ошибки, которые нельзя допускать:  
- Нельзя создавать аккаунт сервера вручную - используйте `cabinet helper addserver`, так корректно будут выданы права и флаги. При несоблюдении этого правила игрок либо не сможет получить товар, либо сможет получить его сколько угодно раз  
- Нельзя использовать один аккаунт сервера на несколько серверов. При несоблюдении этого правила игрок сможет получить товар на всех серверах сразу, имеющих один аккаунт.    
Режим 1 - одиночный сервер:
```
    "servername": {
     "serverUUID": "UUID аккаунта сервера, который используется в ServerWrapper",
     "multiserver": false,
     "noAutoDelivery": false, //При включении этой опции заказ сразу перейдет в состояние DELIVERY без попытки мгновенной выдачи. Использовать при проблемах с мгновенной выдачей
     "type": "event"
    }
```
Режим 2 - мультисерверность:  
В этом режиме товар можно получить на одном из нескольких серверов  
```
    "event": {
     "multiserver": true,
     "list": ["UUID аккаунта первого сервера", "UUID аккаунта второго сервера"] // Колличество серверов не ограничено
     "noAutoDelivery": false, //При включении этой опции заказ сразу перейдет в состояние DELIVERY без попытки мгновенной выдачи. Использовать при проблемах с мгновенной выдачей
     "type": "event"
    }
```
Дополнительную информацию о формате itemId/itemExtra и itemNbt вы можете узнать на странице вашего плагина интеграции
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
