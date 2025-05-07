## Zadanie A1 - "Inteligentny" dom

Aplikacja ma pozwalać na zdalne zarządzanie urządzeniami tzw. inteligentnego domu, którego wyposażeniem są różne urządzenia, 
np. czujniki czadu czy zdalnie sterowane lodówki, piece, kamery monitoringu z opcją PTZ, bulbulatory, itp. 
Każde z urządzeń może występować w kilku nieznacznie się różniących odmianach, a każda z nich w pewnej (niewielkiej) liczbie instancji. 
Dom ten nie oferuje obecnie możliwości budowania złożonych układów, pozwala użytkownikom jedynie na zdalne sterowanie pojedynczymi urządzeniami oraz odczytywanie ich stanu.

### Dodatkowe informacje i wymagania:

- Każde z urządzeń inteligentnego domu jest reprezentowane przez obiekt/usługę strony serwerowej. Sposób jego integracji i komunikacji z rzeczywistym, sterowanym urządzeniem nie jest przedmiotem zainteresowania projektu. Urządzenia mogą działać na wielu instancjach serwerów (tj. w wielu procesach) (demonstracja: na co najmniej dwóch).
- Projektując interfejs IDL urządzeń należy używać także typów bardziej złożonych niż string czy int/long (tj. struktury, sekwencje itp.). Trzeba pamiętać o deklaracji i zgłaszaniu wyjątków lub błędów tam, gdzie to może mieć zastosowanie.
- Wystarczająca jest obsługa dwóch-trzech typów urządzeń, jeden-dwa z nich mają mieć dwa-trzy podtypy.
- Należy odwzorować podane wymagania do cech wybranej technologii w taki sposób, by jak najlepiej wykorzystać oferowane przez nią możliwości budowy takiej aplikacji i by osiągnąć jak najbardziej eleganckie rozwiązanie (gdyby żądanej funkcjonalności nie dało się wprost osiągnąć). Decyzje projektowe trzeba umieć uzasadnić.
- Zestaw urządzeń może być niezmienny w czasie życia serwera (tj. dodanie nowego urządzenia może wymagać modyfikacji kodu serwera i restartu procesu). Aplikacja kliencka może być świadoma obsługiwanych typów urządzeń w czasie kompilacji.
- Początkowy stan instancji obsługiwanego urządzenia może być zawarty w kodzie źródłowym strony serwerowej lub pliku konfiguracyjnym.
- Aplikacja kliencka ma pozwalać zademonstrować sterowanie wszystkimi urządzeniami bez konieczności restartu w celu przełączenia na inne urządzenie.
- Serwer może zapewnić funkcjonalność wylistowania nazw (identyfikatorów) aktualnie dostępnych instancji urządzeń.

Technologia middleware: ICE - należy zaimplementować poszczególne urządzenia inteligentnego domu jako osobne obiekty middleware, 
do których dostęp jest możliwy po podaniu jego identyfikatora Identity („Joe”).  
Języki programowania: Java - serwer, Python - klient  
Maksymalna punktacja: 12

## Uruchomienie

### Uruchomienie serwerów:

Lokalizacja serwerów:  
> server/src/servers/CamerasServer.py  
> server/src/servers/LightingServer.py  
> server/src/servers/ThermostatsServer.py  

### Uruchomienie klienta:

Proszę uruchomić klienta w wersji python 3.12.  
Wymagane pakiety:
> zeroc-ice 3.7.10.1

Lokalizacja klienta:
> client/client.py


## Kompilacja pliku slice `slice/SmartHome.ice`

Aby skompilować plik slice, należy użyć polecenia:
```bash
slice2py -I slice/SmartHome.ice
slice2java -I slice/SmartHome.ice
```
