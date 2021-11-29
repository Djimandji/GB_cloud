package dji.client;
import javafx.application.Application;
import javafx.fxml.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("D:\\JAVA-projects\\GB_cloud\\client\\src\\main\\resources\\sample.fxml"));
        primaryStage.setTitle("CloudService");
        primaryStage.setScene(new Scene(root, 400, 305));
        primaryStage.show();
        new Client().start();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

