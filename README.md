# TransportBehaviorTracker

Android-приложение для автоматического отслеживания поездок на общественном транспорте Москвы. Определяет тип транспорта (пешком, автобус, трамвай, метро, МЦД) по GPS без ручного ввода.

## Возможности

- **Автоматическое определение транспорта** — классифицирует тип передвижения по скорости, близости к остановкам, входам в метро и станциям МЦД
- **Сегменты поездки** — фиксирует пересадки внутри одной поездки (например: пешком → метро → автобус)
- **Дистанция** — подсчитывает пройденное расстояние с фильтрацией GPS-спайков
- **Карта маршрута** — отображает GPS-трек каждой поездки на интерактивной карте
- **Аналитика** — выявляет паттерны поведения (например: «В будние дни утром вы чаще ездите на метро»)
- **Экспорт данных** — выгрузка всех поездок в CSV через стандартный Android share sheet
- **Живые уведомления** — foreground-сервис обновляет уведомление при смене типа транспорта
- **Ручная коррекция** — долгое нажатие на карточку поездки позволяет исправить тип транспорта
- **Локализация** — русский и английский интерфейс (переключается по языку устройства)

## Архитектура

```
app/
├── data/
│   ├── local/          # Room DB (entities, DAOs, migrations)
│   └── repository/     # TransportRepository
├── domain/
│   ├── model/          # Trip, TransportType, AnalyticsPattern, ...
│   └── usecase/        # TripDetector, TripAnalyzer, AnalyticsGenerator, matchers
├── service/            # LocationTrackingService (foreground, GPS)
├── ui/
│   ├── home/           # Экран отслеживания
│   ├── trips/          # Список поездок
│   ├── analytics/      # Аналитика
│   ├── map/            # Карта маршрута (osmdroid)
│   ├── components/     # TripCard, TransportTypeUi
│   └── navigation/     # AppNavigation, Screen
├── utils/              # TripFormatter, TripUiMapper, ExportManager, ...
└── di/                 # Hilt AppModule
```

**Стек:** Jetpack Compose · Material 3 · Hilt · Room · Kotlin Coroutines/Flow · FusedLocationProvider · osmdroid

**База данных:** Room v3, три миграции. Таблицы: `trips`, `trip_segments`, `gps_points`, `patterns`.

**Определение транспорта:**
1. `StopMatcher` — сопоставляет координаты с остановками из GTFS (assets)
2. `MetroMatcher` / `McdMatcher` — близость к входам в метро и МЦД
3. `WalkDetector` — низкая скорость + нет рядом транспортной инфраструктуры
4. `SegmentVoter` — голосование по накопленным сэмплам транспорта за сегмент
5. GPS-потери (тоннель) → дополнительный сигнал метро

## Требования

- Android 8.0+ (minSdk 26)
- Разрешения: `ACCESS_FINE_LOCATION`, `FOREGROUND_SERVICE`, `POST_NOTIFICATIONS`
- Рекомендуется: исключить из оптимизации батареи для надёжной работы в фоне

## Сборка

```bash
git clone <repo>
cd TransportBehaviorTracker
./gradlew assembleDebug
```

Установка на устройство / эмулятор:

```bash
./gradlew installDebug
```

## Тесты

```bash
# Юнит-тесты
./gradlew testDebugUnitTest

# Инструментированные тесты (нужен эмулятор или устройство)
./gradlew connectedDebugAndroidTest
```

Покрыты: `WalkDetector`, `SegmentVoter`, `TripAnalyzer`, `AnalyticsGenerator`, UI-тест `HomeScreen`.

## Скриншоты

| Главная | Поездки | Аналитика | Карта |
|---------|---------|-----------|-------|
| Кнопка запуска/остановки трекинга | Список поездок с сегментами | Паттерны и статистика | GPS-маршрут поездки |
