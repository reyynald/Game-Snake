package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import main.Main;
import database.DBUtil;
import java.sql.*;

public class MenuController {
    @FXML
    private Label welcomeLabel;
    @FXML
    private ComboBox<String> snakeColorBox;
    @FXML
    private ComboBox<String> foodColorBox;
    @FXML
    private Slider volumeSlider;
    public static Color selectedSnakeColor = Color.GREEN;
    public static Color selectedFoodColor = Color.RED;

    @FXML
    private void initialize() {
        try (Connection conn = DBUtil.connect()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT MAX(score) AS max_score FROM scores");
            if (rs.next()) {
                int max = rs.getInt("max_score");
                if (rs.wasNull()) {
                    welcomeLabel.setText("Belum ada skor tersimpan.");
                } else {
                    welcomeLabel.setText("Skor Tertinggi: " + max);
                }
            } else {
                welcomeLabel.setText("Belum ada skor tersimpan.");
            }
        } catch (Exception e) {
            welcomeLabel.setText("Gagal load skor: " + e.getMessage());
            e.printStackTrace();
        }
        snakeColorBox.getItems().addAll("Hijau", "Biru", "Kuning", "Ungu");
        foodColorBox.getItems().addAll("Merah", "Biru", "Kuning", "Ungu");
        snakeColorBox.setValue("Hijau");
        foodColorBox.setValue("Merah");
        snakeColorBox.setOnAction(e -> updateSelectedColors());
        foodColorBox.setOnAction(e -> updateSelectedColors());
        updateSelectedColors();
        if (volumeSlider != null) {
            volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                Main.setMusicVolume(newVal.doubleValue());
            });
            volumeSlider.setValue(0.5);
        }
    }

    @FXML
    private void handlePlay() {
        try {
            Main.changeScene("game.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleToggleTheme() {
        Main.toggleTheme();
    }

    private void updateSelectedColors() {
        switch (snakeColorBox.getValue()) {
            case "Hijau" -> selectedSnakeColor = Color.GREEN;
            case "Biru" -> selectedSnakeColor = Color.BLUE;
            case "Kuning" -> selectedSnakeColor = Color.YELLOW;
            case "Ungu" -> selectedSnakeColor = Color.PURPLE;
        }
        switch (foodColorBox.getValue()) {
            case "Merah" -> selectedFoodColor = Color.RED;
            case "Biru" -> selectedFoodColor = Color.BLUE;
            case "Kuning" -> selectedFoodColor = Color.YELLOW;
            case "Ungu" -> selectedFoodColor = Color.PURPLE;
        }
    }
}