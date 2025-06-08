package com.rp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerApplication extends Application {

    public static final String APP_ID = "Screen Share Server";
    private static final Logger logger = LoggerFactory.getLogger(ServerApplication.class);

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/scene/HomeScene.fxml"));
        Scene homeScene = new Scene(root);
        stage.setScene(homeScene);
        stage.setTitle(APP_ID);
        stage.show();
        logger.info("Started ServerApplication");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
