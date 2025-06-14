# Zadanie domowe - RESTful API
**24.03.2025**


## Temat
Celem zadania jest napisanie prostego serwisu udostępniającego dane zgodne z REST w oparciu o otwarte serwisy udostępniające REST API. Stworzyć macie Państwo serwis, który:
1. udostępni klientowi statyczną stronę HTML z formularzem do zebrania parametrów żądania,
2. strona serwerowa udostępnia REST API, odbierze zapytanie od klienta, i następnie je przetworzy,
3. odpyta serwis publiczny (różne endpointy), a lepiej kilka serwisów o dane potrzebne do skonstruowania odpowiedzi,
4. dokona odróbki otrzymanych odpowiedzi (np.: wyciągnięcie średniej, znalezienie ekstremów, porównanie wartości z różnych serwisów itp.),
5. wygeneruje i wyśle odpowiedź do klienta.
Wybranie realizowanej funkcjonalności i używanych serwisów pozostawiam Państwa wyobraźni, zainteresowaniom i rozsądkowi. Przykładowo:
Klient podaje miasto i okres czasu ('daty od/do' lub 'ostatnie n dni'), serwer odpytuje ogólnodostępny serwis pogodowy o temperatury w poszczególne dni, oblicza średnią i znajduje ekstrema i wysyła do klienta wszystkie wartości (tzn. prostą stronę z tymi danymi). Ewentualnie serwer odpytuje kilka serwisów pogodowych i podaje różnice w podawanych prognozach.
Z reguły wygrywa prognoza pogody lub kursy walut, ale liczę na wykazanie się większą kreatywnością ;-)

## Punktacja
### (6 pkt)
- Strona serwerowa udostępnia REST API, klient może komunikować się z udostępnionym API
- Premiowane będą rozwiązanie inne niż prognoza pogody i kursy walut
- Klient (przeglądarka) ma wysyłać żądanie w oparciu o dane z formularza (statyczny HTML) i otrzymać odpowiedź w formie prostej strony www, wygenerowanej przez tworzony serwis. Wystarczy użyć czystego HTML, bez stylizacji, bez dodatkowych bibliotek frontendowych (nie jest to elementem oceny). Nie musi być piękne, ma działać
- Tworzony serwis powinien wykonać kilka zapytań (np.: o różne dane, do kilku serwisów itp.). Niech Państwa rozwiązanie nie będzie tylko takim proxy do jednego istniejącego serwisu i niech zapewnia dodatkową logikę (to będzie elementem oceny, najlepiej 2 lub więcej)
- Odpowiedź dla klienta musi być generowana przez serwer na podstawie: 1) żądań REST do publicznych serwisów i 2) lokalnej obróbki uzyskanych odpowiedzi.
- Serwer ma być uruchomiony na własnym serwerze aplikacyjnym działającym poza IDE (lub analogicznej technologii)
- Dodatkowym (ale nieobowiązkowym) atutem jest wystawienie serwisu w chmurze (np.: Heroku). To jest część dla zainteresowanych i nie podlega podstawowej ocenie
- Dopuszczalna jest realizacja zadania w dowolnym wybranym języku/technologii (oczywiście sugerowany jest Python i FastAPI). Proszę jednak o zachowanie analogicznego poziomu abstrakcji (operowanie bezpośrednio na żądaniach/odpowiedziach HTTP, kontrola generowania/odbierania danych)
- Wybieramy serwisy otwarte lub dające dostęp ograniczony, lecz darmowy, np.: używające kodów deweloperskich

### (9 pkt)
- Wymagania z funkcjonalności powyżej
- Przygotowujemy test zapytań HTTP z wykorzystaniem POSTMANa/SwaggerUI (klient-serwer i serwer-serwis_publiczny). Do oddania proszę mieć je już zapisane i gotowe do demo
- Obsługę (a)synchroniczności zapytań serwera do serwisów zewnętrznych (np.: promises)
- Walidację danych wprowadzanych przez klienta/przyjmowanych przez REST API
- Warto zwrócić uwagę na obsługę błędów i odpowiedzi z serwisów (np.: jeśli pojawi się błąd komunikacji z serwisem zewnętrznym, to generujemy odpowiedni komunikat do klienta, a nie 501 Internal server error)

### (10 pkt)
- Wymagania z funkcjonalności powyżej
- Implementacja elementów bezpieczeństwa i zabezpieczania API

## Uwagi
Podczas oddawania na pewno zadam następujące pytania z tej części:
- proszę o zaprezentowanie funkcjonalności (demo)
- proszę przedstawić i opisać stworzone REST API (dokumentacja w postaci Open API (lub coś podobnego), zgodność z regułami REST (dołączone materiały, prezentacja strona 21))
