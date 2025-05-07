import grpc
import proto.application_pb2 as pb2
import proto.application_pb2_grpc as pb2_grpc

def generate_text_chunks():
    texts = ["Hello", " world", "! This is a test."]
    for text in texts:
        yield pb2.TextChunk(text=text)

def message_stream():
    f = open("../send_text.txt", "r", encoding="utf-8")
    line = f.readline().strip()
    while line:
        yield pb2.TextChunk(text=line)
        line = f.readline().strip()
    f.close()

def run():
    with grpc.insecure_channel('localhost:50051') as channel:
        stub = pb2_grpc.ApplicationStub(channel)

        print("==================== Method: Avg ====================")
        numbers = [7, 10, 21]
        print("Dane wejściowe:", numbers)
        response = stub.Avg(pb2.RepeatedInt32(args=numbers))
        print(f"Wynik: {response.res:.3f}")

        print()

        print("==================== Method: CountLetters ====================")
        response = stub.CountLetters(message_stream())
        print("Częstotliwość liter:")
        items = dict(sorted(response.frequencies.items(), key=lambda item: item[1], reverse=True))
        print(items)

        print()

        print("==================== Method: GetInfo ====================")
        request_name = pb2.Name(name="Konrad Tendaj")
        response_iterator = stub.GetInfo(request_name)
        for text_chunk in response_iterator:
            print(text_chunk.text, end='', flush=True)

        print()

        print("==================== Method: GroupByAgeGroup ====================")
        person_list: pb2.PersonList = pb2.PersonList()
        person_list.people.append(pb2.Person(name="Konrad Tendaj", birthDate=pb2.Date(day=5, month=12, year=2003)))
        person_list.people.append(pb2.Person(name="Jan Kowalski", birthDate=pb2.Date(day=23, month=8, year=1995)))
        person_list.people.append(pb2.Person(name="Anna Nowak", birthDate=pb2.Date(day=7, month=3, year=1976)))
        person_list.people.append(pb2.Person(name="Józef Cebula", birthDate=pb2.Date(day=31, month=1, year=2016)))
        person_list.people.append(pb2.Person(name="Michał Nowak", birthDate=pb2.Date(day=15, month=4, year=1990)))
        person_list.people.append(pb2.Person(name="Brian Griffin", birthDate=pb2.Date(day=12, month=9, year=2011)))
        person_list.people.append(pb2.Person(name="Zbigniew Góralczyk", birthDate=pb2.Date(day=29, month=11, year=1964)))
        person_list.people.append(pb2.Person(name="Adam Małysz", birthDate=pb2.Date(day=17, month=7, year=1977)))
        response = stub.GroupByAgeGroup(person_list)
        items = dict(sorted(response.categories.items(), key=lambda item: item[0]))
        for (age_group, people) in items.items():
            print(f"{age_group}: {[f"{person.name}: {person.age}" for person in people.people]}")

        print()
        print("=========================================================================================")


if __name__ == '__main__':
    run()
