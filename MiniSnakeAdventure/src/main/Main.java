package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class Main extends Application {
    public static Stage primaryStage;
    public static Scene mainScene;
    public static boolean isDark = false;
    public static MediaPlayer mediaPlayer;

    public static void copyCssIfNeeded() {
        try {
            String binCssPath = System.getProperty("user.dir") + "/bin/css/theme.css";
            java.io.File binCssFile = new java.io.File(binCssPath);
            if (!binCssFile.exists()) {
                java.io.File binCssDir = new java.io.File(binCssFile.getParent());
                if (!binCssDir.exists()) binCssDir.mkdirs();
                java.io.InputStream in = Main.class.getResourceAsStream("css/theme.css"); // tanpa '/' di depan
                if (in != null) {
                    java.nio.file.Files.copy(in, binCssFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("File theme.css berhasil dicopy ke bin/css/");
                } else {
                    System.err.println("Resource css/theme.css tidak ditemukan untuk dicopy!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        copyCssIfNeeded();
        primaryStage = stage;
        Parent root = FXMLLoader.load(getClass().getResource("/view/menu.fxml"));
        if (!root.getStyleClass().contains("root")) root.getStyleClass().add("root");
        mainScene = new Scene(root);
        applyTheme();
        playBackgroundMusic();
        stage.setScene(mainScene);
        stage.setTitle("Snake Adventure");
        stage.show();
    }

    public static void playBackgroundMusic() {
        try {
            // Otomatis copy file audio jika belum ada di bin/audio
            String binAudioPath = System.getProperty("user.dir") + "/bin/audio/bgm.mp3";
            java.io.File binAudioFile = new java.io.File(binAudioPath);
            if (!binAudioFile.exists()) {
                java.io.File binAudioDir = new java.io.File(binAudioFile.getParent());
                if (!binAudioDir.exists()) binAudioDir.mkdirs();
                java.io.InputStream in = Main.class.getResourceAsStream("/audio/bgm.mp3");
                if (in != null) {
                    java.nio.file.Files.copy(in, binAudioFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("File bgm.mp3 berhasil dicopy ke bin/audio/");
                } else {
                    System.err.println("Resource /audio/bgm.mp3 tidak ditemukan untuk dicopy!");
                }
            }
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            var url = Main.class.getResource("/audio/bgm.mp3");
            System.out.println("Mencari audio di: /audio/bgm.mp3, url: " + url);
            Media media = null;
            if (url != null) {
                media = new Media(url.toExternalForm());
            } else {
                // Coba akses file secara absolut
                java.io.File file = binAudioFile;
                System.out.println("Coba akses file absolut: " + file.getAbsolutePath() + ", exists: " + file.exists());
                if (file.exists()) {
                    media = new Media(file.toURI().toString());
                } else {
                    System.err.println("⚠️ bgm.mp3 tidak ditemukan di classpath maupun output folder!");
                    return;
                }
            }
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop
            mediaPlayer.setVolume(0.5); // Volume 50%
            mediaPlayer.setOnError(() -> System.err.println("MediaPlayer error: " + mediaPlayer.getError()));
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void changeScene(String fxml) throws Exception {
        Parent pane = FXMLLoader.load(Main.class.getResource("/view/" + fxml));
        if (!pane.getStyleClass().contains("root")) pane.getStyleClass().add("root");
        mainScene.setRoot(pane);
        applyTheme();
    }

    public static void applyTheme() {
        mainScene.getStylesheets().clear();
        try {
            String cssPath = System.getProperty("user.dir") + "/bin/css/theme.css";
            java.io.File cssFile = new java.io.File(cssPath);
            if (cssFile.exists()) {
                mainScene.getStylesheets().add(cssFile.toURI().toString());
            } else {
                System.err.println("⚠️ theme.css not found at: " + cssFile.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isDark) {
            mainScene.getRoot().getStyleClass().add("dark");
        } else {
            mainScene.getRoot().getStyleClass().remove("dark");
        }
    }

    public static void toggleTheme() {
        isDark = !isDark;
        applyTheme();
    }

    public static void setMusicVolume(double volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
