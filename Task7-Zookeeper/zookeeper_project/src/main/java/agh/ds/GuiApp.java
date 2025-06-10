package agh.ds;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;

public class GuiApp extends Application {

    private static Stage primaryStage;
    private static volatile boolean isInitialized = false;

    private static final Label serverLabel = new Label("Serwer: ");
    private static final Label statusLabel = new Label("");
    private static final Label descendantsCountLabel = new Label("");
    private static final Label treeLabel = new Label("");

    static {
        Platform.setImplicitExit(false);
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        treeLabel.setId("treeLabel");

        HBox descendantCountInfo = new HBox(10);
        descendantCountInfo.getChildren().addAll(new Label("Liczba potomków: "), descendantsCountLabel);

        root.getChildren().addAll(serverLabel, statusLabel, descendantCountInfo, treeLabel);

        Scene scene = new Scene(root, 400, 300);
        URL url = getClass().getResource("/style.css");
        assert url != null;
        scene.getStylesheets().add(url.toExternalForm());
        primaryStage.setTitle("Zookeeper Monitor");
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            Platform.runLater(() -> primaryStage.hide());
        });

        primaryStage.hide();
        isInitialized = true;
        System.out.println("GUI initialized!");
    }

    public static void showWindow(String server) {
        if (!isInitialized) {
            System.out.println("GUI nie zostało jeszcze zainicjalizowane!");
            return;
        }
        Platform.runLater(() -> {
            if (primaryStage != null) {
                serverLabel.setText("Serwer: " + server);
                primaryStage.show();
                System.out.println("Okno zostało pokazane");
            }
        });
    }

    public static void hideWindow() {
        if (!isInitialized) {
            System.out.println("GUI nie zostało jeszcze zainicjalizowane!");
            return;
        }
        Platform.runLater(() -> {
            if (primaryStage != null) {
                primaryStage.hide();
                System.out.println("Okno zostało ukryte");
            }
        });
    }

    public static void updateStatus(String status) {
        Platform.runLater(() -> statusLabel.setText("Status: " + status));
    }

    public static void updateDescendantsCount(int count) {
        Platform.runLater(() -> descendantsCountLabel.setText(Integer.toString(count)));
    }

    public static void updateTree(String tree) {
        Platform.runLater(() -> treeLabel.setText(tree));
    }

    @Override
    public void stop() {
        System.out.println("Aplikacja została zamknięta");
    }
}

