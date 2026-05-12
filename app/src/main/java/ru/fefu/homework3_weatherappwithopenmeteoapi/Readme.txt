Стародубов Павел Павлович - Б9123.09.03.03(ЦТЭ)
Выбрал Api Open - meteo для получения данных о погоде и городе по названию
скрины в комментариях, так - как иначе у меня не загружаются

Чеклист:
A) Навигация
Минимум 2 экрана:
1. List/Search - список результатов (и поиск/фильтр)
2. Detail/{id} - детали элемента по аргументу в route
B) Архитектура
UiState
ViewModel
UI-экраны максимально stateless: получают
Repository между ViewModel и Retrofit
C) Coroutines + Retrofit
Все сетевые запросы - через
state + callbacks (
onEvent )
suspend функции Retrofit
Запуск из
viewModelScope
D) UI состояния
Empty (если ничего не найдено)
Success (список)
E) Избранное (локально, без БД)
Можно добавлять/убирать элемент в favourites
Избранное должно переживать:
поворот экрана (т.е. хранить в ViewModel state)
Технические требования
Compose + Material3
Navigation Compose
ViewModel + viewModelScope
Retrofit