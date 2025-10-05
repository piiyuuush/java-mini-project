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

import javax.imageio.IIOImage; // ImageIO libraries for image reading and writing
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream; 

import javafx.application.Application; // JavaFX libraries for GUI components
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

public class ImgCompressorApp extends Application { // main application class
    
    private final File initialDir = new File("./Images"); // default directory
    private String nameWithoutExt = "compressed"; // default name
    private double compression_factor = 0.0; // compression factor
    private BufferedImage bufferedImage;
    private File currentFile;
    
    @Override
    public void start(Stage primaryStage) {
        
        // Layout setup
        BorderPane root = new BorderPane();

        // buttons and slider components
        Button openBtn = new Button("Open Image");
        Button saveBtn = new Button("Save Image");
        Label sliderLabel = new Label("Compression Quality (%)");

        // slider congiguration
        Slider slider = new Slider(0, 100, 0);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(25);
        slider.setBlockIncrement(5);

        // grouping slider components in a vertical box
        VBox sliderBox = new VBox(5, sliderLabel, slider);
        sliderBox.setAlignment(Pos.CENTER);

        // grouping buttons and slider in a horizontal box
        HBox controls = new HBox(20, openBtn, sliderBox, saveBtn);
        controls.setPadding(new Insets(10));
        controls.setAlignment(Pos.CENTER);

        // original image view component
        Label originalTitle = new Label("Original");
        originalTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        ImageView originalView = new ImageView();
        originalView.setPreserveRatio(true);
        originalView.setFitWidth(350);
        originalView.setFitHeight(350);
        Label originalSizeLabel = new Label("Original Size: -");
        
        // compressed image preview component
        Label compressedTitle = new Label("Compressed (0%)");
        compressedTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        ImageView previewView = new ImageView();
        previewView.setPreserveRatio(true);
        previewView.setFitWidth(350);
        previewView.setFitHeight(350);
        Label compressedSizeLabel = new Label("Compressed Size: -");

        // grouping origin view components in vertical box
        VBox originalBox = new VBox(5, originalTitle, originalView, originalSizeLabel);
        originalBox.setAlignment(Pos.CENTER);

        // grouping compressed view components in vertical box
        VBox compressedBox = new VBox(5, compressedTitle, previewView, compressedSizeLabel);
        compressedBox.setAlignment(Pos.CENTER);

        // grouping both image views in horizontal box
        HBox previews = new HBox(20, originalBox, compressedBox);
        previews.setPadding(new Insets(10));
        previews.setAlignment(Pos.CENTER);

        // setting up the main layout
        root.setTop(controls);
        root.setCenter(previews);

        //open button action -> load image
        openBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();

            if(initialDir.exists()) {
                chooser.setInitialDirectory(initialDir);
            } // set initial directory if exists

            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png") 
            ); // supports JPG and PNG
            
            currentFile = chooser.showOpenDialog(primaryStage); //load selected file
            String originalName = currentFile.getName();
            int dotIndex = originalName.lastIndexOf('.');
            nameWithoutExt = (dotIndex == -1) ? originalName : originalName.substring(0, dotIndex); // extract file name without extension
            try {
                Image fxImage = new Image(new FileInputStream(currentFile)); // load image into JavaFX Image
                originalView.setImage(fxImage); // display original image
                bufferedImage = ImageIO.read(currentFile); // read image into BufferedImage for processing

                // Update original size
                originalSizeLabel.setText("Original Size: " + formatSize(currentFile.length()));

                // Initial preview (100% quality = original copy)
                previewView.setImage(fxImage);
                compressedSizeLabel.setText("Compressed Size: " + formatSize(currentFile.length()));

            } catch (IOException ex) {
                System.err.println("Error opening image: " + ex.getMessage()); 
            }
        });


        // --- Slider Change -> Live Preview ---
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            compression_factor = (newVal.doubleValue() / 100.0); // calculate compression factor
            compressedTitle.setText("Compressed (" + (int) newVal.doubleValue() + "%)");
            if (bufferedImage != null) {
                byte[] compressedBytes = compressToBytes(bufferedImage, compression_factor); // compress image to bytes
                if (compressedBytes != null) {
                    previewView.setImage(new Image(new ByteArrayInputStream(compressedBytes))); // update preview
                    compressedSizeLabel.setText("Compressed Size: " + formatSize(compressedBytes.length)); // update size label
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
                        saveCompressedImage(bufferedImage, outFile, compression_factor);
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
    private byte[] compressToBytes(BufferedImage image, double compression_factor) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (!writers.hasNext()) throw new IllegalStateException("No writers found");

            ImageWriter writer = writers.next();
            try (MemoryCacheImageOutputStream output = new MemoryCacheImageOutputStream(baos)) {
                writer.setOutput(output);
                ImageWriteParam param = writer.getDefaultWriteParam();
                if (param.canWriteCompressed()) {
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality((float) compression_factor); 
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
    private void saveCompressedImage(BufferedImage image, File outFile, double compression_factor) throws IOException {
        byte[] data = compressToBytes(image, compression_factor);
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
