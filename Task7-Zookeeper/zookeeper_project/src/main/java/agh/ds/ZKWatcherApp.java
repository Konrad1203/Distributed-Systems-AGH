package agh.ds;

import javafx.application.Application;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ZKWatcherApp implements Watcher {

    private static final String ZNODE_PATH = "/a";
    private static String ZK_ADDRESS = "localhost:2181,localhost:2182,localhost:2183";

    private ZooKeeper zooKeeper;

    public ZKWatcherApp(String serverPorts) {
        if (serverPorts != null && !serverPorts.trim().isEmpty()) {
            String[] ports = serverPorts.split(" ");
            if (ports.length > 0)
                ZK_ADDRESS = Stream.of(ports).map(it -> "localhost:" + it.trim()).collect(Collectors.joining(","));
        }
        System.out.println("Ustawiony serwer: " + ZK_ADDRESS);
    }

    public static void main(String[] args) throws IOException {
        Thread fxThread = new Thread(() ->
                Application.launch(GuiApp.class, args));
        fxThread.setDaemon(false);
        fxThread.start();

        try { Thread.sleep(1000); }
        catch (InterruptedException e) { System.err.println("Przerwano inicjalizację gui"); }

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Podaj w kolejności porty do serwerów: ");
        String input = br.readLine().trim();


        var zkApp = new ZKWatcherApp(input);
        zkApp.run();
    }

    public void run() {
        try {
            connect();
            watchRecursively(ZNODE_PATH);
            System.out.println("ZKWatcherApp started");
//            GuiApp.showWindow(ZK_ADDRESS);
//            updateTree();
//            updateDescendantsCount();
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            updateStatus("Błąd połączenia: " + e.getMessage());
        }
    }

    public void connect() throws IOException {
        zooKeeper = new ZooKeeper(ZK_ADDRESS, 5_000, this);
        updateStatus("Połączono z Replicated ZooKeeper");
    }

    @Override
    public void process(WatchedEvent event) {
        String path = event.getPath();
        if (path == null) return;
        System.out.println("Event: " + event.getType() + " na ścieżce: " + path);
        try {
            switch (event.getType()) {
                case Event.EventType.NodeCreated:
                    updateStatus("Utworzono węzeł: " + path);
                    GuiApp.showWindow(ZK_ADDRESS);
                    watchRecursively(ZNODE_PATH);
                    break;
                case Event.EventType.NodeDeleted:
                    updateStatus("Usunięto węzeł: " + path);
                    if (ZNODE_PATH.equals(path)) GuiApp.hideWindow();
                    zooKeeper.exists(ZNODE_PATH, this);
                    break;
                case Event.EventType.NodeChildrenChanged:
                    updateStatus("Zmieniono potomków w: " + path);
                    watchRecursively(ZNODE_PATH);
                    break;
            }
            updateTree();
            updateDescendantsCount();
        } catch (Exception e) {
            updateStatus("Błąd: " + e.getMessage());
        }
    }

    private void watchRecursively(String path) throws KeeperException, InterruptedException {
        Stat stat = zooKeeper.exists(path, this);
        if (stat == null) return;
        List<String> children = zooKeeper.getChildren(path, this);
        for (String child : children) {
            String childPath = path + "/" + child;
            watchRecursively(childPath);
        }
    }

    private void updateTree() {
        try {
            String tree = getTree(ZNODE_PATH);
            System.out.println("Tree: \n" + tree.trim());
            GuiApp.updateTree(tree);
        } catch (KeeperException | InterruptedException e) {
            updateStatus("Błąd pobierania drzewa: " + e.getMessage());
        }
    }

    private void updateStatus(String status) {
        System.out.println(status);
        GuiApp.updateStatus(status);
    }

    private void updateDescendantsCount() {
        int descendantsCount = getDescendantsCount(ZNODE_PATH);
        System.out.println("Liczba potomków węzła " + ZNODE_PATH + ": " + descendantsCount);
        GuiApp.updateDescendantsCount(descendantsCount);
    }

    private int getDescendantsCount(String path) {
        try {
            List<String> children = zooKeeper.getChildren(path, false);
            int count = children.size();
            for (String child : children) {
                count += getDescendantsCount(path + "/" + child);
            }
            return count;
        } catch (KeeperException | InterruptedException e) {
            updateStatus("Błąd pobierania potomków: " + e.getMessage());
        }
        return -1;
    }

    public String getTree(String path) throws KeeperException, InterruptedException {
        return getTree(path, new StringBuilder(), 0, true, true)
                .toString();
    }

    private StringBuilder getTree(String path, StringBuilder sb, int indent, boolean isFirst, boolean isLast) throws KeeperException, InterruptedException {
        Stat stat = zooKeeper.exists(path, false);
        if (stat != null) {
            if (isFirst) {
                sb.append(path).append("\n");
            } else {
                addIntent(sb, indent-1);
                sb.append(isLast ? "┗━" : "┣━");
                sb.append(path.substring(path.lastIndexOf('/'))).append("\n");
            }
            List<String> children = zooKeeper.getChildren(path, false);
            for (int i = 0; i < children.size()-1; i++) {
                String child = children.get(i);
                getTree(path + "/" + child, sb, indent + 1, false, false);
            }
            if (!children.isEmpty()) {
                String child = children.getLast();
                getTree(path + "/" + child, sb, indent + 1, false, true);
            }
        }
        return sb;
    }

    private void addIntent(StringBuilder sb, int indent) {
        sb.repeat("   ", indent);
    }
}

