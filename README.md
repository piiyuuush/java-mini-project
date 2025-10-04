# 📸 JavaFX Image Compressor

This is a **JavaFX project built with Maven**. It is a **GUI-based image compression program** that compresses images using a lossy JPEG method. While some image data is discarded during compression, the results are visually promising and highly effective in reducing file size. This makes the project both **practical** and **educational**, especially for learning about image processing and JavaFX.

---

## ✨ Features

* **Open Images** – Select any image (JPG/PNG) from your system.
* **Compression Slider** – Adjust compression quality between **0%–100%**.
* **Live Preview** – Instantly see the effect of compression before saving.
* **File Size Display** – Compare original and compressed sizes in real time.
* **Recently Compressed Thumbnails** – Keeps track of the last 4 compressed images.
* **Save Option** – Export the compressed image to your computer.

---

## 🛠️ Tech Stack

* **JavaFX** – User Interface framework
* **Maven** – Build and dependency management
* **javax.imageio** – Core Java image handling (part of `java.desktop`)
* **TwelveMonkeys ImageIO** – Extended image format support

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/javafx-image-compressor.git
cd javafx-image-compressor
```

### 2. Build with Maven

```bash
mvn clean install
```

### 3. Run the Application

Using Maven plugin:

```bash
mvn javafx:run
```

Or using a fat JAR (if packaged with Shade plugin):

```bash
java -jar target/img-compressor-1.0-SNAPSHOT-shaded.jar
```

---

## 📂 Project Structure

```
src/main/java/
 └── com/compressor/
     └── ImgCompressorApp.java   # Main JavaFX application
src/main/resources/              # (optional resources)
pom.xml                          # Maven build configuration
```

---

## 📖 How JPEG Compression Works

The app uses the **JPEG lossy compression algorithm**:

1. **RGB → YCbCr** conversion (separating brightness from color).
2. **Downsampling** of color channels (optional).
3. **8×8 block division** of the image.
4. **Discrete Cosine Transform (DCT)** to convert spatial data into frequencies.
5. **Quantization** to discard less noticeable details.
6. **Zig-zag scanning & Run-Length Encoding** for compact representation.
7. **Huffman coding** for entropy compression.

The **slider adjusts quantization strength**, directly controlling file size vs quality trade-off.

---

## 🎯 Learning Outcomes

* Hands-on practice with **JavaFX GUI development**.
* Understanding **image compression workflows**.
* Exposure to **lossy compression techniques**.
* Using **Maven dependencies** and Java modules effectively.

---

## 📜 License

This project is open-source and intended for **educational purposes**.

---
