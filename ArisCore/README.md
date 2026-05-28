# ArisCore

Один-jar плагин для Minecraft 1.21.x (Paper). Объединяет:

- **Auth** — регистрация/логин с BCrypt, **сессия 5 минут** (если вышел и зашел в течение 5 минут — логиниться не надо).
- **Защита OP** — белый список ника в `operators.yml`; `/op` и `/deop` работают только для ников из списка. Команда `/oper reload` обновляет список без рестарта. Заблокированы хакерские команды (`stop`, `restart`, `reload`, `execute`, `fill`, `setblock`, `summon`, `datapack`, и т.п.) для не-OP.
- **Economy** — валюта **aris**, `/a give|giveall|reset|set|pay <ник> <сумма>`.
- **Spawn/TPA** — `/spawn`, `/setspawn`, `/tpa`, `/tpaccept`, `/tpadeny` (с кликабельными `[ACCEPT]` / `[DENY]`); телепорт на спавн при заходе и при смерти.
- **Warps** — `/setwarp`, `/warp`, `/delwarp`, `/warps`.
- **Shop** — `/shop` (GUI), товары 100..2500 aris, нельзя просто забрать — только купить.
- **Kits** — `/free` (стартовый кит, один раз), `/kits` (GUI), КД от 2 ч до 30+ дней, донат-эксклюзивные киты, каждый донат — свой кит.
- **Donates** — `/don` / `/donate` (GUI с шалкерами, описание и красивое RGB-имя), `/ad give|set|reset|list|reload`. **Если у игрока уже стоит более высокий донат — низший не выдается.** 20+ донатов и стафф-ранков (VIP, Premium, Elite, Hero, Legend, Mythic, Divine, Titan, Ultimate, Aris — самый мощный; Helper, DHelper, Moder, Moder GL, ML Moder, Moder Sponsor, Curator, Admin, Admin D, Admin GL, ML Admin, Owner).
- **Regions** — Region Block (именной железный блок), `/rg`, `/rg flag`, `/rg addowner`, `/rg addmember`, `/rg rename`, `/rg info`, `/rg list`. **20+ флагов** (pvp, build, break, interact, container, vehicle, item-drop, item-pickup, bucket, fire, lava, ice, tnt, creeper, mob-griefing, mob-spawn, sleep, redstone, trample, pistons, frost-walk, chorus-teleport). Кликабельный GUI флагов в чате.
- **Crates** — `/dc list`, `/dc place <type>`, `/dc remove`, `/dc preview <type>`. Бесплатные и платные (за aris). Над каждым кейсом ArmorStand-голограмма. Если в кейсе выпал донат, который уже есть у игрока (или выше) — отдается обычный предмет.
- **Scoreboard** — сбоку: имя, ранг (RGB), aris, kills, deaths, время в игре, онлайн.
- **TAB** — заголовок `ArisWorld`, "Online: N" (без слеша), у каждого игрока в TAB слева ранг (жирный, RGB), потом ник (белый, жирный).
- **Утилиты** — `/speed`, `/fly`, `/v` (vanish), `/god`, `/heal`, `/feed`, `/gm`/`/gmc`/`/gms`/`/gma`/`/gmsp`, `/ariscorereload`.
- **RGB-градиенты** — везде где есть текст (заголовки, GUI, скорборд, чат-кнопки и т.д.), парсятся теги `<grad:#start:#end>...</grad>`, `<click:run:/foo>...</click>`, `<hover:...>...</hover>`, `<b>`, `<i>`.

## Сборка

JDK 21+ и Maven 3.6+:

```
cd ArisCore
mvn -B clean package -DskipTests
```

Готовый файл: `ArisCore/target/ArisCore-1.0.0.jar`.

## Установка

1. Положи `ArisCore-1.0.0.jar` в `plugins/`.
2. Запусти сервер (Paper 1.21.x, Java 21).
3. После первого запуска появятся `plugins/ArisCore/operators.yml`, `donates.yml`, `kits.yml`, `shop.yml`, `crates.yml`, `config.yml`.
4. Открой `operators.yml`, добавь свой ник, в игре выполни `/oper reload`.

## Главные команды

| Команда | Назначение |
| --- | --- |
| `/register <pw> <pw>` | Регистрация |
| `/login <pw>` | Вход (если нет сессии) |
| `/oper reload\|add\|remove\|list` | Управление списком OP |
| `/spawn` | Телепорт на спавн |
| `/tpa <ник>` | Запрос телепорта (кликабельный ответ) |
| `/a [ник]`, `/a give\|giveall\|reset\|set\|pay` | Aris-экономика |
| `/shop` | GUI магазина (100..2500 aris) |
| `/kits`, `/kit <id>`, `/free` | Киты |
| `/don` / `/donate` | GUI донатов |
| `/ad give\|set\|reset\|list\|reload` | Админ-донаты |
| `/rg`, `/rg flag`, `/rg addowner`, `/rg addmember`, `/rg rename`, `/rg info` | Приваты-регионы |
| `/regionblock` | Получить именной блок региона |
| `/dc list\|place\|remove\|preview` | Донат-кейсы |
| `/speed`, `/fly`, `/v`, `/god`, `/heal`, `/feed`, `/gm` | Утилиты |
| `/ariscorereload` | Перезагрузить конфиги |
