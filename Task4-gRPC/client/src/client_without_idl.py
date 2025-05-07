import grpc
from google.protobuf.descriptor import ServiceDescriptor, MethodDescriptor, FieldDescriptor
from grpc_reflection.v1alpha.proto_reflection_descriptor_database import ProtoReflectionDescriptorDatabase
from google.protobuf.descriptor_pool import DescriptorPool
from google.protobuf.message_factory import GetMessageClass
from google._upb._message import Descriptor


field_label = {1: "optional", 2: "required", 3: "repeated"}
field_type = {
    1: "double", 2: "float",
    3: "int64", 4: "uint64", 5: "int32", 13: "uint32",
    8: "bool", 9: "string", 10: "group",
    11: "message", 12: "bytes", 14: "enum"
}


def main():
    with grpc.insecure_channel("localhost:50051") as channel:

        reflection_db = ProtoReflectionDescriptorDatabase(channel)
        services: list[str] = list(reflection_db.get_services())
        print("\nAll services:", services)

        desc_pool = DescriptorPool(reflection_db)
        service_desc: ServiceDescriptor = desc_pool.FindServiceByName('application.Application')

        print_methods_and_fields(service_desc)

        methods = [run_avg_method, run_count_letters_method, run_get_info_method, run_group_by_age_group_method]
        for method in methods:
            method(channel, service_desc)
            print()
        print("=========================================================================================")


def print_methods_and_fields(service_desc):
    print("\nAvailable methods:", [method.full_name for method in service_desc.methods])
    for method in service_desc.methods:
        method: MethodDescriptor
        print("Method:", method.full_name)

        input_type: Descriptor = method.input_type
        print("  Input type:", input_type.name)
        for i, field in enumerate(input_type.fields):
            field: FieldDescriptor
            print(f"    {i + 1}. {field.name}: of type: {field_label[field.label]} {field_type[field.type]}")

        output_type: Descriptor = method.output_type
        print("  Output type:", output_type.name)
        for i, field in enumerate(output_type.fields):
            field: FieldDescriptor
            print(f"    {i + 1}. {field.name}: of type: {field_label[field.label]} {field_type[field.type]}")
        print()


def run_avg_method(channel, service_desc):
    avg_method = service_desc.methods_by_name['Avg']
    print("==================== Method:", avg_method.full_name, "====================")

    input_message = GetMessageClass(avg_method.input_type)
    output_message = GetMessageClass(avg_method.output_type)

    numbers = [7, 10, 21]
    print("Dane wejściowe:", numbers)
    msg = input_message(args=numbers)

    stub = channel.unary_unary(
        f"/{service_desc.full_name}/{avg_method.name}",
        request_serializer=msg.SerializeToString,
        response_deserializer=output_message.FromString
    )
    response = stub(msg)
    print(f"Wynik: {response.res:.3f}")


def run_count_letters_method(channel, service_desc):
    count_letters_method = service_desc.methods_by_name['CountLetters']
    print("==================== Method:", count_letters_method.full_name, "====================")

    input_message = GetMessageClass(count_letters_method.input_type)
    output_message = GetMessageClass(count_letters_method.output_type)

    def message_stream():
        f = open("../send_text.txt", "r", encoding="utf-8")
        line = f.readline().strip()
        while line:
            # print(line)
            yield input_message(text=line)
            line = f.readline().strip()
        f.close()

    stub = channel.stream_unary(
        f"/{service_desc.full_name}/{count_letters_method.name}",
        request_serializer=input_message.SerializeToString,
        response_deserializer=output_message.FromString
    )
    response = stub(message_stream())

    print("Częstotliwość liter:")
    items = dict(sorted(response.frequencies.items(), key=lambda item: item[1], reverse=True))
    print(items)


def run_get_info_method(channel, service_desc):
    get_info_method = service_desc.methods_by_name['GetInfo']
    print("==================== Method:", get_info_method.full_name, "====================")

    input_message = GetMessageClass(get_info_method.input_type)
    output_message = GetMessageClass(get_info_method.output_type)

    msg = input_message(name="Konrad Tendaj")

    stub = channel.unary_stream(
        f"/{service_desc.full_name}/{get_info_method.name}",
        request_serializer=msg.SerializeToString,
        response_deserializer=output_message.FromString
    )
    response = stub(msg)
    for text in response:
        print(text.text, end='', flush=True)


def run_group_by_age_group_method(channel, service_desc):
    group_by_age_group_method = service_desc.methods_by_name['GroupByAgeGroup']
    print("==================== Method:", group_by_age_group_method.full_name, "====================")

    input_message = GetMessageClass(group_by_age_group_method.input_type)
    output_message = GetMessageClass(group_by_age_group_method.output_type)

    msg = input_message()
    msg.people.add(**{"name": "Konrad Tendaj", "birthDate": {"day": 5, "month": 12, "year": 2003}})
    msg.people.add(**{"name": "Jan Kowalski", "birthDate": {"day": 23, "month": 8, "year": 1995}})
    msg.people.add(**{"name": "Anna Nowak", "birthDate": {"day": 7, "month": 3, "year": 1976}})
    msg.people.add(**{"name": "Józef Cebula", "birthDate": {"day": 31, "month": 1, "year": 2016}})
    msg.people.add(**{"name": "Michał Nowak", "birthDate": {"day": 15, "month": 4, "year": 1990}})
    msg.people.add(**{"name": "Brian Griffin", "birthDate": {"day": 12, "month": 9, "year": 2011}})
    msg.people.add(**{"name": "Zbigniew Góralczyk", "birthDate": {"day": 29, "month": 11, "year": 1964}})
    msg.people.add(**{"name": "Adam Małysz", "birthDate": {"day": 17, "month": 7, "year": 1977}})

    stub = channel.unary_unary(
        f"/{service_desc.full_name}/{group_by_age_group_method.name}",
        request_serializer=msg.SerializeToString,
        response_deserializer=output_message.FromString
    )
    response = stub(msg)
    items = dict(sorted(response.categories.items(), key=lambda item: item[0]))

    for (age_group, people) in items.items():
        print(f"{age_group}: {[f"{person.name}: {person.age}" for person in people.people]}")


if __name__ == "__main__":
    main()
