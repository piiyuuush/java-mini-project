module com.compressor {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop; 
    requires transitive javafx.graphics;

    opens com.example.imgcompressor to javafx.fxml;
    exports com.example.imgcompressor;
}
