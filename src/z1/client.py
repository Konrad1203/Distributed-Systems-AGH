import socket
import struct
import threading

server_addr = ("127.0.0.1", 12345)
multicast_addr = ("227.0.0.7", 40455)
encoder = 'utf-8'

tcp_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
tcp_sock.connect(server_addr)
print("Connected with a TCP server")

udp_sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
udp_sock.bind(tcp_sock.getsockname())

send_multi_sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
send_multi_sock.setsockopt(socket.IPPROTO_IP, socket.IP_MULTICAST_TTL, struct.pack('b', 1))

recv_multi_sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
recv_multi_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
recv_multi_sock.bind(('', multicast_addr[1]))

group = socket.inet_aton(multicast_addr[0])
mreq = struct.pack("4sL", group, socket.INADDR_ANY)
recv_multi_sock.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)

nick = None

while True:
    nick = input("Enter your username: ")
    tcp_sock.send((nick + '\n').encode('utf-8'))

    if not nick or not nick.strip() or len(nick) > 32:
        print("Username must be non-empty and at most 32 characters long")
        continue

    nick_from_server = tcp_sock.recv(1024)
    if not nick_from_server:
        print("Server closed connection")
        tcp_sock.close()
        exit()
    elif nick != nick_from_server.decode('utf-8').strip():
        print("Username already taken. Enter a different one")
    else:
        break

print("You can start chatting now")

def receiver_tcp():
    while True:
        try:
            buff = tcp_sock.recv(1024)
            print(str(buff, encoder).strip())
            print(">", end=" ")
            if not buff:
                print("receiver_tcp: server disconnected")
                tcp_sock.close()
                exit()
        except ConnectionResetError:
            print("receiver_tcp: server closed connection")
            tcp_sock.close()
            exit()

def receiver_udp():
    while True:
        try:
            buff, _ = udp_sock.recvfrom(1024)
            print("DGRAM: " + str(buff, encoder).strip())
            print(">", end=" ")
            if not buff:
                print("receiver_udp: server disconnected")
                udp_sock.close()
                exit()
        except ConnectionResetError:
            print("receiver_udp: server closed connection")
            udp_sock.close()
            exit()

def receiver_multicast():
    while True:
        try:
            buff, addr = recv_multi_sock.recvfrom(1024)
            multicast_message = str(buff, encoder).strip()
            if multicast_message.startswith(nick): continue
            print("MULTICAST: " + multicast_message)
            print(">", end=" ")
            if not buff:
                print("receiver_multicast: server disconnected")
                send_multi_sock.close()
                recv_multi_sock.close()
                exit()
        except ConnectionResetError:
            print("receiver_multicast: server closed connection")
            send_multi_sock.close()
            recv_multi_sock.close()
            exit()


threading.Thread(target=receiver_tcp, daemon=True).start()
threading.Thread(target=receiver_udp, daemon=True).start()
threading.Thread(target=receiver_multicast, daemon=True).start()

while True:
    message = input("> ")
    if not message or not message.strip():
        continue
    try:
        if message.startswith("/"):
            command_type = message[1]
            message = message[2:].strip()
            if command_type == "U":
                udp_sock.sendto(bytes((message + '\n'), encoder), server_addr)
            elif command_type == "M":
                send_multi_sock.sendto(bytes(f"{nick}: {message}\n", encoder), multicast_addr)
            else:
                print("Invalid command")
        else:
            tcp_sock.send(bytes((message + '\n'), encoder))
    except ConnectionResetError:
        print("sender: server closed connection")
