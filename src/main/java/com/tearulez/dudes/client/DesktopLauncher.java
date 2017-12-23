package com.tearulez.dudes.client;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.*;
import java.util.Optional;
import java.util.Properties;

public class DesktopLauncher extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Metrics.startReporter();
        Optional<ClientConfig> optional = getClientConfig();
        optional.ifPresent(clientConfig -> launchApplication(clientConfig.host, clientConfig.port, clientConfig.volume));
    }

    private static Optional<ClientConfig> getClientConfig() {

        // Read properties
        Properties properties;
        try (FileReader reader = new FileReader(new File("client.properties"))) {
            properties = new Properties();
            properties.load(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Create the custom dialog.
        Dialog<ClientConfig> dialog = new Dialog<>();
        dialog.setTitle("Dudes");
        dialog.setHeaderText("Settings");

        // Set the button types.
        ButtonType connectButtonType = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL);

        // Create fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField serverField = new TextField();
        serverField.setText(properties.getProperty("server"));

        // Server filed
        grid.add(new Label("Server:"), 0, 0);
        grid.add(serverField, 1, 0);

        // Volume field
        Slider slider = new Slider();
        slider.setMin(0);
        slider.setMax(1);
        slider.setValue(Float.valueOf(properties.getProperty("volume")));
        grid.add(new Label("Volume:"), 0, 1);
        grid.add(slider, 1, 1);

        // Set content
        dialog.getDialogPane().setContent(grid);

        // Request focus on the server field by default.
        Platform.runLater(serverField::requestFocus);

        // Convert the result to a ClientConfig when connect button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == connectButtonType) {

                // Parse server field
                String[] strings = serverField.getText().split(":");
                if (strings.length != 2) return null;

                // Save properties
                String server = serverField.getText();
                String volume = String.valueOf(slider.getValue());
                properties.setProperty("server", server);
                properties.setProperty("volume", volume);
                saveProperties(properties);

                // Return client config
                return new ClientConfig(strings[0], Integer.valueOf(strings[1]), (float) slider.getValue());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private static void saveProperties(Properties properties) {
        try {
            FileOutputStream fos = new FileOutputStream("client.properties");
            try {
                properties.store(fos, "");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void launchApplication(String host, int port, float volume) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Dudes";
        config.width = 800;
        config.height = 480;
        GameClient gameClient = new GameClient(host, port);
        DudesGame game = new DudesGame(gameClient, volume);
        new LwjglApplication(game, config);
    }

    private static class ClientConfig {
        final String host;
        final int port;
        final float volume;

        ClientConfig(String host, int port, float volume) {
            this.host = host;
            this.port = port;
            this.volume = volume;
        }
    }
}