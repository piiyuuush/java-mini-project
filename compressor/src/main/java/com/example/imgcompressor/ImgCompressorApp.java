package com.example.imgcompressor;
// importing required libraries 
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ImgCompressorApp extends Application {

    private final ImageView originalView = new ImageView();
    private final ImageView previewView = new ImageView();
    private final Label originalSizeLabel = new Label("Original Size: -");
    private final Label compressedSizeLabel = new Label("Compressed Size: -");
    private final Label compressedTitle = new Label("Compressed (0%)");
    private final File initialDir = new File("./Images");
    
    private File currentFile;
    private BufferedImage bufferedImage;
    private double quality = 1.0; // default 100%
    private String nameWithoutExt = "compressed";
    

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // --- Controls ---
        Button openBtn = new Button("Open Image");
        Button saveBtn = new Button("Save Image");

        Label sliderLabel = new Label("Compression Quality (%)");
        Slider slider = new Slider(0, 100, 0);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(25);
        slider.setBlockIncrement(5);

        VBox sliderBox = new VBox(5, sliderLabel, slider);
        sliderBox.setAlignment(Pos.CENTER);

        HBox controls = new HBox(20, openBtn, sliderBox, saveBtn);
        controls.setPadding(new Insets(10));
        controls.setAlignment(Pos.CENTER);

        // --- Image Views ---
        originalView.setPreserveRatio(true);
        originalView.setFitWidth(350);
        originalView.setFitHeight(350);

        previewView.setPreserveRatio(true);
        previewView.setFitWidth(350);
        previewView.setFitHeight(350);

        Label originalTitle = new Label("Original");
        originalTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        compressedTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        VBox originalBox = new VBox(5, originalTitle, originalView, originalSizeLabel);
        originalBox.setAlignment(Pos.CENTER);

        VBox compressedBox = new VBox(5, compressedTitle, previewView, compressedSizeLabel);
        compressedBox.setAlignment(Pos.CENTER);

        HBox previews = new HBox(20, originalBox, compressedBox);
        previews.setPadding(new Insets(10));
        previews.setAlignment(Pos.CENTER);

        root.setTop(controls);
        root.setCenter(previews);

        // --- Open Button ---
        openBtn.setOnAction(e -> {
        FileChooser chooser = new FileChooser();

        if(initialDir.exists()) {
            chooser.setInitialDirectory(initialDir);
        }

        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png")
        );
        
        currentFile = chooser.showOpenDialog(primaryStage);
        String originalName = currentFile.getName();
        int dotIndex = originalName.lastIndexOf('.');
        nameWithoutExt = (dotIndex == -1) ? originalName : originalName.substring(0, dotIndex);
        if (currentFile != null) {
            try {
                Image fxImage = new Image(new FileInputStream(currentFile));
                originalView.setImage(fxImage);
                bufferedImage = ImageIO.read(currentFile);

                // Update original size
                originalSizeLabel.setText("Original Size: " + formatSize(currentFile.length()));

                // Initial preview (100% quality = original copy)
                previewView.setImage(fxImage);
                compressedSizeLabel.setText("Compressed Size: " + formatSize(currentFile.length()));

            } catch (IOException ex) {
                System.err.println("Error opening image: " + ex.getMessage());
            }
        }
    });


        // --- Slider Change -> Live Preview ---
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            quality = 1-(newVal.doubleValue() / 100.0);
            compressedTitle.setText("Compressed (" + (int) newVal.doubleValue() + "%)");
            if (bufferedImage != null) {
                byte[] compressedBytes = compressToBytes(bufferedImage, quality);
                if (compressedBytes != null) {
                    previewView.setImage(new Image(new ByteArrayInputStream(compressedBytes)));
                    compressedSizeLabel.setText("Compressed Size: " + formatSize(compressedBytes.length));
                }
            }
        });

        // --- Save Button ---
        saveBtn.setOnAction(e -> {
            if (currentFile != null && bufferedImage != null) {
                try {
                    FileChooser chooser = new FileChooser();
                    if(initialDir.exists()) {
                        chooser.setInitialDirectory(initialDir);
                    }
                    chooser.setInitialFileName(nameWithoutExt + "_compressed.jpg");
                    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG", "*.jpg"));
                    File outFile = chooser.showSaveDialog(primaryStage);

                    if (outFile != null) {
                        saveCompressedImage(bufferedImage, outFile, quality);
                    }
                } catch (IOException ex) {
                    System.err.println("Error saving image: " + ex.getMessage());
                }
            }
        });

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("JPEG Compressor (Live Preview)");
        primaryStage.show();
    }

    /** Compress and return bytes */
    private byte[] compressToBytes(BufferedImage image, double quality) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (!writers.hasNext()) throw new IllegalStateException("No writers found");

            ImageWriter writer = writers.next();
            try (MemoryCacheImageOutputStream output = new MemoryCacheImageOutputStream(baos)) {
                writer.setOutput(output);
                ImageWriteParam param = writer.getDefaultWriteParam();
                if (param.canWriteCompressed()) {
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality((float) quality);
                }
                writer.write(null, new IIOImage(image, null, null), param);
            } finally {
                writer.dispose();
            }
            return baos.toByteArray();
        } catch (IOException e) {
            System.err.println("Compression error: " + e.getMessage());
            return null;
        }
    }

    /** Save compressed image to disk */
    private void saveCompressedImage(BufferedImage image, File outFile, double quality) throws IOException {
        byte[] data = compressToBytes(image, quality);
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write(data);
        }
    }

    /** Format bytes to human-readable KB/MB */
    private String formatSize(long bytes) {
        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format("%.1f KB", kb);
        else return String.format("%.2f MB", kb / 1024.0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
