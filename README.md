# Лабораторная работа №5. Штатная база данных Android: SQLite через библиотеку Room

Студент: Ретунский Савелий

## Описание проекта
Приложение разработано в рамках лабораторной работы №5 по курсу мобильной разработки для ОС Android.

Цель работы — освоение взаимодействия со штатной реляционной СУБД SQLite через официальную архитектурную библиотеку Room (часть Android Jetpack).

### Архитектура приложения:
1. **Сущность (`Note.kt`)**:
   - Аннотация `@Entity(tableName = "notes")`.
   - Поля: `id` (`@PrimaryKey(autoGenerate = true)`), `title`, `content`, `timestamp`.

2. **DAO-интерфейс (`NoteDao.kt`)**:
   - Методы CRUD: `@Insert`, `@Update`, `@Delete` с поддержкой Kotlin корутин (`suspend`).
   - Реактивный запрос: `@Query("SELECT * FROM notes ORDER BY timestamp DESC")` с возвратом `LiveData<List<Note>>`.

3. **База данных (`AppDatabase.kt`)**:
   - Наследник `RoomDatabase` с аннотацией `@Database(entities = [Note::class], version = 1)`.
   - Реализация потокобезопасного синглтона (`@Volatile` и `synchronized`).

4. **Архитектурный слой ViewModel (`NoteViewModel.kt`)**:
   - Наследник `AndroidViewModel`.
   - Инкапсуляция обращений к DAO через `viewModelScope.launch`.
   - Предоставление реактивного потока данных `allNotes: LiveData<List<Note>>` для UI.

5. **Пользовательский интерфейс**:
   - Автоматическое обновление списка в `RecyclerView` благодаря подписке на `LiveData` (`viewModel.allNotes.observe(...)`).
   - Валидация входных данных: запрет добавления заметки с пустым заголовком с уведомлением через `Toast`.
   - Кнопки операций: «Добавить», «Обновить», «Удалить», «Сброс».
   - Выбор заметки из списка для быстрого редактирования или удаления.

## Сборка и запуск
- Минимальная версия Android SDK: API 24 (Android 7.0)
- Целевая версия Android SDK: API 35
- Язык разработки: Kotlin
- Готовый установочный файл приложения: `app-debug.apk` в корне репозитория.


## Проверка и отчёт

[Отчёт со скриншотами и фрагментами кода](reports/REPORT.md).

Проверка сборки и Android Lint: `./verify.ps1`. Проверка сценариев на отдельном эмуляторе: `./verify.ps1 -DeviceTests` (очищает учебные данные приложения на эмуляторе). Актуальный APK: [app-debug.apk](app-debug.apk).
