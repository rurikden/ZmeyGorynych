# Описание проекта ZmeyGorynych

## Обзор
Это мобильное Android-приложение, разработанное на Kotlin, вероятно, предназначенное для управления персоналом и рабочим расписанием. Приложение использует надежную архитектуру с Room для постоянного хранения локальных данных, ViewModel и LiveData для обработки данных, связанных с пользовательским интерфейсом, а также Navigation Component для навигации по приложению.

## Технологии и зависимости

### Основные
*   **Язык:** Kotlin
*   **Плагин Android Gradle (AGP):** 8.13.2
*   **minSdk:** 34
*   **targetSdk:** 35

### Библиотеки AndroidX
*   `androidx.core:core-ktx`: Расширения Kotlin для библиотек AndroidX.
*   `androidx.appcompat:appcompat`: Библиотека поддержки для старых версий Android.
*   `com.google.android.material:material`: Компоненты Material Design.
*   `androidx.activity:activity-ktx`: Расширения KTX для Activity.
*   `androidx.constraintlayout:constraintlayout`: Гибкая система макетов.

### Навигация
*   `androidx.navigation:navigation-fragment-ktx:2.7.7`: Расширения Kotlin для фрагментов навигации.
*   `androidx.navigation:navigation-ui-ktx:2.7.7`: Расширения Kotlin для пользовательского интерфейса навигации.

### База данных (Room)
*   `androidx.room:room-runtime:2.6.1`: Среда выполнения базы данных Room.
*   `androidx.room:room-ktx:2.6.1`: Расширения Kotlin для Room (поддержка сопрограмм).
*   `androidx.room:room-compiler:2.6.1`: Процессор аннотаций Room (kapt).

### Жизненный цикл (Lifecycle)
*   `androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0`: Расширения Kotlin для ViewModel.
*   `androidx.lifecycle:lifecycle-livedata-ktx:2.7.0`: Расширения Kotlin для LiveData.

### Тестирование
*   `junit:junit:4.13.2`: Стандартный фреймворк для тестирования JUnit.
*   `androidx.test.ext:junit:1.2.1`: Расширения JUnit для AndroidX.
*   `androidx.espresso:espresso-core:3.6.1`: Фреймворк для тестирования пользовательского интерфейса.

## Структура проекта и ключевые компоненты

*   **`BaseActivity.kt`**: Абстрактный базовый класс для активностей, предоставляющий общую функциональность, такую как настройка навигационного ящика (Navigation Drawer) и обработка нажатий кнопки "Назад".
*   **`MainActivity.kt`**: Главная точка входа в приложение. Расширяет `BaseActivity` и обрабатывает навигацию к `WorkActivity`.
*   **`WorkActivity.kt`**: Эта активность отвечает за управление задачами, связанными с работой. Она включает `CustomCalendarView` для выбора даты, несколько виджетов `Spinner` для выбора персонала и поля `TextView` для отображения и выбора временных диапазонов и рассчитанных часов.
*   **`Personnel.kt`**: Класс данных `@Entity` Room, представляющий запись о персонале с полями для `id`, `lastName`, `firstName`, `middleName`, `position` и `company`. Он также включает геттер `fullName`.
*   **`PersonnelDao.kt`**: Интерфейс `@Dao` Room, определяющий методы для операций с базой данных в таблице `personnel`, таких как вставка, обновление, удаление и запрос записей персонала.
*   **`PersonnelRepository.kt`**: Класс репозитория, который абстрагирует источник данных (базу данных Room) для данных о персонале. Он предоставляет методы для взаимодействия с `PersonnelDao` и обычно используется `ViewModel`.
*   **`PersonnelViewModel.kt`**: Класс `ViewModel`, отвечающий за подготовку и управление данными для пользовательского интерфейса, в частности, данными, связанными с персоналом. Он взаимодействует с `PersonnelRepository` и предоставляет данные через `LiveData` для `Activity` или `Fragment`.
*   **`AppDatabase.kt`**: Главный класс базы данных Room, аннотированный `@Database`. Он определяет сущности (`Personnel`) и предоставляет абстрактный метод для получения `PersonnelDao`. Он также включает паттерн `singleton` для управления экземпляром базы данных.
*   **`AppSettings.kt`**: Класс, вероятно, используемый для управления настройками или предпочтениями приложения.
*   **`ColorPaletteDialog.kt`**: Фрагмент диалога или класс для отображения и обработки выбора цветовой палитры.
*   **`PersonnelActivity.kt`**: Вероятно, активность для просмотра или редактирования индивидуальных данных о персонале.
*   **`PersonnelListActivity.kt`**: Активность, отвечающая за отображение списка персонала, часто с использованием `RecyclerView` и взаимодействующая с `PersonnelViewModel` и `PersonnelRepository`.
