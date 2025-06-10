## System rozproszony z użyciem Zookeeper

Klient do śledzenia zmian w strukturze drzewa znodów Zookeepera.

Stworzyć aplikację w środowisku Zookeeper (Java, …), która wykorzystując mechanizm obserwatorów (watches), umożliwia następujące funkcjonalności:
    - Jeśli tworzony jest znode o nazwie „a”, uruchamiana jest zewnętrzna aplikacja graficzna (dowolna, określona w linii poleceń), 
    - Jeśli jest kasowany „a”, aplikacja zewnętrzna jest zatrzymywana,
    - Każde dodanie potomka do „a” powoduje wyświetlenie graficznej informacji na ekranie o aktualnej ilości potomków.

Dodatkowo aplikacja powinna mieć możliwość wyświetlenia całej struktury drzewa „a”.

Stworzona aplikacja powinna działać w środowisku „Replicated ZooKeeper”.

## Uruchomienie serwera Zookeeper
```bash
cd <server_directory>/bin
./zkServer2.cmd zoo1.cfg
./zkServer2.cmd zoo2.cfg
./zkServer2.cmd zoo3.cfg
./zkCli.cmd localhost:2181
```
