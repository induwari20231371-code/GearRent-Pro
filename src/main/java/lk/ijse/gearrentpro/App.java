package lk.ijse.gearrentpro;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // Load Login View initially
        // Ensure '/view/Login.fxml' exists or will be created
        // For now, we point to it even if it doesn't exist yet
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
        try {
            Parent root = loader.load();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("GearRent Pro - Login");
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            System.err.println("Failed to load Login view. Check FXML path.");
            e.printStackTrace();
        }
    }

    public static void setRoot(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/view/" + fxml + ".fxml"));
        scene.setRoot(loader.load());
    }

    public static void main(String[] args) {
        launch();
    }
}
