package com.rp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientApplication extends Application {

    private static final Logger logger = LoggerFactory.getLogger(ClientApplication.class);
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/scene/HomeScene.fxml"));
        Scene homeScene = new Scene(root);
        stage.setScene(homeScene);
        stage.setTitle("Screen Share Client");
        stage.show();

        logger.info("Started ClientApplication");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
