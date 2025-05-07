## Zadanie I1 - Wywołanie dynamiczne

Celem zadania jest demonstracja działania wywołania dynamicznego po stronie klienta middleware. 
Wywołanie dynamiczne to takie, w którym nie jest wymagana znajomość interfejsu zdalnego obiektu lub usługi w czasie kompilacji, lecz jedynie w czasie wykonania 
(w zadaniu: klient ma nie mieć dołączonych żadnych klas/bibliotek stub będących wynikiem kompilacji IDL). 
Wywołania mają być zrealizowane dla kilku (co najmniej trzech) różnych operacji/procedur używających przynajmniej w jednym przypadku nietrywialnych struktur danych 
(np. listy (sekwencji) struktur) i sposobu komunikacji (gRPC: wywołanie strumieniowe). 
Nie trzeba tworzyć żadnego formatu opisującego żądania użytkownika ani parsera jego żądań - wystarczy zawrzeć to wywołanie "na sztywno" w kodzie źródłowym, co najwyżej z konsoli parametryzując szczegóły danych. 
Jako bazę można wykorzystać projekt z zajęć. Trzeba przemyśleć i umieć przedyskutować przydatność takiego podejścia w budowie aplikacji rozproszonych.  
ICE: Dynamic Invocation https://doc.zeroc.com/ice/3.7/client-server-features/dynamic-ice/dynamic-invocation-and-dispatch.  
Odnośnie ograniczeń warto spojrzeć tu: https://doc.zeroc.com/ice/3.7/client-server-features/dynamic-ice/streaming-interfaces  
gRPC: Dopuszczalne (rekomendowane?) jest użycie usługi refleksji.  
Równorzędnymi funkcjonalnie (w stosunku do stworzonego klienta) narzędziami są grpcurl oraz Postman - należy umieć zademonstrować ich działanie w czasie oddawania zadania.

Technologia middleware: Ice albo gRPC  
Języki programowania: dwa różne (jeden dla klienta, drugi dla serwera)  
Maksymalna punktacja: 8


## Uruchomienie

### Uruchomienie serwera:

```bash
cd server
./gradlew build
./gradlew run
```

Lokalizacja serwera:  
> server/serc/main/kotlin/Main.kt

### Uruchomienie klienta:

Proszę uruchomić klienta w wersji python 3.12.  
Wymagane pakiety:
> grpcio            1.71.0  
> grpcio-reflection 1.71.0  
> grpcio-tools      1.71.0  
> protobuf          5.29.4  


Z wymaganym idl'em:
```bash
cd client/src
python client_with_idl.py
```
> client/src/client_with_idl.py

Bez idl'a:
```bash
cd client/src
python client_without_idl.py
```
> client/src/client_without_idl.py


## Kompilacja pliku application.protoc

Do javy (z zainstalowanym protoc):
```bash
protoc -I. --java_out=server/src/main/gen proto/application.proto
protoc -I. --java_out=server/src/main/gen --plugin=protoc-gen-grpc-java=proto/grpc-java-1.71.0.exe --grpc-java_out=server/src/main/gen proto/application.proto
```

Do pythona (przez grpc_tools):
```bash
python -m grpc_tools.protoc -I. --python_out=client/src --grpc_python_out=client/src proto/application.proto
```