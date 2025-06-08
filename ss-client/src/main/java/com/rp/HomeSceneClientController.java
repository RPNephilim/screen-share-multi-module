package com.rp;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class HomeSceneClientController {

    @FXML private Label statusLabel;
    @FXML private ImageView imageView;

    private static final Logger logger = LoggerFactory.getLogger(HomeSceneClientController.class);

    private ServerSocket streamServerSocket;
    private Socket streamSocket;
    private BufferedInputStream inputStream;
    private boolean isStreaming = false;

    private Socket connRequestSocket;
    private DataOutputStream connRequestOutputStream;

    private static final int REQUEST_PORT = 5000;
    private static final int OUTPUT_PORT = 5001;

    private static final int BUFFER_SIZE = 1024 * 64;

    private LinkedList<WritableImage> imageLinkedList;

    public void start(ActionEvent event){
        statusLabel.setText("Connected");
        try{
            if(Objects.isNull(streamServerSocket) || streamServerSocket.isClosed()) {
                connRequestSocket = new Socket("localhost", REQUEST_PORT);
                connRequestOutputStream = new DataOutputStream(connRequestSocket.getOutputStream());

                connRequestOutputStream.writeBoolean(true);

                streamServerSocket = new ServerSocket(OUTPUT_PORT);
                isStreaming = true;

                imageLinkedList = new LinkedList<>();
            }
            receive();
        }catch (Exception e){
            logger.error("Error starting stream", e);
            throw new RuntimeException(e);
        }
    }
    public void stop(ActionEvent event){
        statusLabel.setText("Idle");
        isStreaming = false;
        try{
            connRequestOutputStream.writeBoolean(false);
            connRequestOutputStream.flush();
            connRequestOutputStream.close();
            connRequestSocket.close();


//            inputStream.close();
            streamSocket.close();
            streamServerSocket.close();
        }catch (Exception e){
            logger.error("Error stopping stream", e);
            throw new RuntimeException(e);
        }
    }
    private void receive() {
        new Thread(() -> {
            try {
                streamSocket = streamServerSocket.accept();
                inputStream = new BufferedInputStream(streamSocket.getInputStream(), BUFFER_SIZE) ;
                DataInputStream dataInputStream = new DataInputStream(inputStream);
                logger.info("Listening on PORT 5000...");

                while (isStreaming && !streamSocket.isClosed()) {
                    int length = dataInputStream.readInt(); // First, read size
                    byte[] imageBytes = new byte[length];
                    dataInputStream.readFully(imageBytes); // Read full image

                    ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
                    BufferedImage bufferedImage = ImageIO.read(bais);

                    if (bufferedImage != null) {
                        WritableImage image = SwingFXUtils.toFXImage(bufferedImage, null);
                        Platform.runLater(() -> {
                            imageView.setImage(image);
                            logger.info("Frame received...");
                        });
                    }
                }
                logger.info("Stopped listening");
            } catch (IOException e) {
                logger.error("Error reading inputStream", e);
                throw new RuntimeException(e);
            }
        }).start();
    }

    private List<Image> getAndUpdateQueue() {
        if (imageLinkedList != null && imageLinkedList.size() >= 180){
            List<Image> images = new ArrayList<>();
            for(int i = 0; i < 180; i++) {
                images.add(imageLinkedList.pollFirst());
            }
            return images;
        }
        return null;
    }
}
