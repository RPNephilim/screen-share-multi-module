package com.rp;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.stage.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.rp.util.SocketUtil.isOpen;

public class HomeSceneServerController {

    @FXML private Label statusLabel;

    private static final Logger logger = LoggerFactory.getLogger(HomeSceneServerController.class);

    private ServerSocket connRequestServerSocket;
    private Socket connRequestSocket;
    private DataInputStream connRequestInputStream;

    private ExecutorService executor;

    private Socket streamSocket;
    private BufferedOutputStream streamOutputStream;
    private ScheduledExecutorService scheduledExecutor;
    private Robot robot;
    private Rectangle rectangle;

    private static final int REQUEST_PORT = 5000;
    private static final int OUTPUT_PORT = 5001;

    private static final int BUFFER_SIZE = 1024 * 64;

    public void openConnection(ActionEvent event){
        statusLabel.setText("Connected");
        try {
            if (!isOpen(connRequestServerSocket)) {
                logger.info("Initializing resources...");
                connRequestServerSocket = new ServerSocket(REQUEST_PORT);

                executor = Executors.newSingleThreadExecutor();
                scheduledExecutor = Executors.newScheduledThreadPool(5);

                logger.info("Successfully initialized resources");
            }

            executor.execute(()-> {
                try {
                    logger.info("New thread executed");
                    while (isOpen(connRequestServerSocket)) {
                        logger.info("Waiting to establish connection");
                        connRequestSocket = connRequestServerSocket.accept();
                        connRequestInputStream = new DataInputStream(connRequestSocket.getInputStream());
                        logger.info("Connection established");
                        boolean toStream = connRequestInputStream.readBoolean();
                        if (toStream) {

                            robot = new Robot();
                            Rectangle2D screen = Screen.getPrimary().getBounds();
                            rectangle = new Rectangle((int) screen.getWidth(), (int) screen.getHeight());

                            streamSocket = new Socket("localhost", OUTPUT_PORT);
                            streamOutputStream = new BufferedOutputStream(streamSocket.getOutputStream(), BUFFER_SIZE);
                            startStream();
                        }else {
                            stopStream();
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error starting stream", e);
                    throw new RuntimeException(e);
                }
            });

        }catch (Exception e) {
            logger.error("Error opening connection", e);
            throw new RuntimeException(e);
        }
    }

    private void closeResources() {
        try {
            if(streamOutputStream != null) {
                streamOutputStream.flush();
            }
            if(scheduledExecutor != null) {
                scheduledExecutor.shutdown();
            }
            if(streamOutputStream != null) {
                streamOutputStream.close();
            }
            if(streamSocket != null) {
                streamSocket.close();
            }
            if(connRequestInputStream != null) {
                connRequestInputStream.close();
            }
            if(connRequestSocket != null) {
                connRequestSocket.close();
            }
            if(connRequestServerSocket != null){
                connRequestServerSocket.close();
            }
        } catch (Exception e) {
            logger.error("Error closing resources", e);
            throw new RuntimeException(e);
        }
    }

    public void closeConnection(ActionEvent event) {
        try {
            statusLabel.setText("Idle");
            if(streamOutputStream != null) {
                streamOutputStream.flush();
            }
            if(scheduledExecutor != null) {
                scheduledExecutor.shutdown();
            }
            if(streamOutputStream != null) {
                streamOutputStream.close();
            }
            if(streamSocket != null) {
                streamSocket.close();
            }
            if(connRequestInputStream != null) {
                connRequestInputStream.close();
            }
            if(connRequestSocket != null) {
                connRequestSocket.close();
            }
            if(connRequestServerSocket != null){
                connRequestServerSocket.close();
            }
        } catch (Exception e) {
            logger.error("Error closing resources", e);
            throw new RuntimeException(e);
        }
    }

    private void startStream(){
        Platform.runLater(()->{
            statusLabel.setText("Sending...");
        });
        scheduledExecutor.scheduleAtFixedRate(()->{
            try {
                if(isOpen(streamSocket)){
                    BufferedImage bufferedImage = robot.createScreenCapture(rectangle);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, "jpeg", baos);
                    byte[] imageBytes = baos.toByteArray();

                    DataOutputStream dos = new DataOutputStream(streamOutputStream);
                    dos.writeInt(imageBytes.length); // send image size
                    dos.write(imageBytes);// send image
                    dos.flush();
                }
            }catch (Exception e){
                logger.error("Error writing to output stream", e);
                throw new RuntimeException(e);
            }
        }, 0, 16, TimeUnit.MILLISECONDS);
    }

    private void stopStream() throws IOException {
        try {
            scheduledExecutor.shutdown();
            streamOutputStream.close();
            streamSocket.close();

            connRequestInputStream.close();
            connRequestSocket.close();
            connRequestServerSocket.close();
            Platform.runLater(() -> {
                statusLabel.setText("Idle");
            });
        } catch (Exception e) {
            logger.error("Error stopping stream", e);
            throw new RuntimeException(e);
        }
    }
}
