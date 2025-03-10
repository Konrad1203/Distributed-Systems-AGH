package z1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class Server {

    public static void main(String[] args) {
        int port = 12345;
        var server = new Server(port);
        server.run();
    }

    private record UdpClientData(InetAddress address, int port) {}

    private final int port;
    private final DatagramSocket dgramSocket;
    private final byte[] dgramBuffer = new byte[1024];
    private final Map<String, PrintWriter> clientsOutChannel = new ConcurrentHashMap<>();
    private final Map<UdpClientData, String> udpAddressToClient = new ConcurrentHashMap<>();

    public Server(int port) {
        this.port = port;
        try {
            this.dgramSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    private void run() {
        System.out.println("Server is running on port " + port);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var serverSocket = new ServerSocket(port)) {
            executor.submit(this::handleDgramSocket);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void handleDgramSocket() {
        try {
            var dp = new DatagramPacket(dgramBuffer, dgramBuffer.length);
            while (true) {
                dgramSocket.receive(dp);
                if (dp.getLength() > 0) {
                    String message = new String(dp.getData()).trim();
                    String username = udpAddressToClient.get(new UdpClientData(dp.getAddress(), dp.getPort()));
                    System.out.println("Received datagram from " + username + ": " + message);
                    byte[] sendMessage = (username + ": " + message).getBytes(StandardCharsets.UTF_8);
                    for (UdpClientData data : udpAddressToClient.keySet()) {
                        if (!dp.getAddress().equals(data.address) || data.port != dp.getPort()) {
                            dgramSocket.send(new DatagramPacket(sendMessage, sendMessage.length, data.address, data.port));
                        }
                    }
                    Arrays.fill(dgramBuffer, (byte) 0);
                }
            }
        } catch (IOException e) {
            System.err.println("Error in datagram socket: " + e.getMessage());
        }
    }

    private void sendToOthers(String nick, String message) {
        for (var entry : clientsOutChannel.entrySet()) {
            if (!entry.getKey().equals(nick)) entry.getValue().println(nick + ": " + message);
        }
    }

    private void handleClient(Socket clientSocket) {
        String nick = null;
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8))) {

            while ((nick = in.readLine()) != null) {
                nick = nick.trim();
                if (clientsOutChannel.containsKey(nick)) {
                    out.println("_nick_already_exists_");
                } else {
                    clientsOutChannel.put(nick, out);
                    udpAddressToClient.put(new UdpClientData(clientSocket.getInetAddress(), clientSocket.getPort()), nick);
                    System.out.println("New client connected! " + nick);
                    out.println(nick);
                    break;
                }
            }
            String message;
            while ((message = in.readLine()) != null) {
                message = message.trim();
                System.out.println("Received message from " + nick + ": " + message);
                sendToOthers(nick, message);
            }
        } catch (IOException e) {
            System.err.println("Error in client " + nick + ": " + e.getMessage());
        }

        if (nick != null) clientsOutChannel.remove(nick);
        System.out.println("Client " + nick + " disconnected!");
        try { clientSocket.close(); }
        catch (IOException e) { System.err.println("Error during closing in client " + nick + ": " + e.getMessage()); }
    }
}

