package controller;

import database.DBUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.awt.Point;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.LinkedList;
import java.util.Random;

import static controller.MenuController.selectedSnakeColor;
import static controller.MenuController.selectedFoodColor;

public class GameController {
    @FXML
    private Canvas gameCanvas;
    @FXML
    private VBox overlay;
    @FXML
    private Label scoreLabel;
    @FXML
    private Button backButton;
    @FXML
    private Label infoLabel;
    @FXML
    private Button pauseButton;
    @FXML
    private Label gameOverLabel;
    @FXML
    private Label fpsLabel;

    private final int tileSize = 10;
    private int width;
    private int height;

    private LinkedList<Point> snake = new LinkedList<>();
    private Point food;
    private int dx = tileSize;
    private int dy = 0;
    private int score = 0;
    private int speed = 100;
    private Timeline timeline;
    private Random rand = new Random();
    private boolean paused = false;
    private Color snakeColor;
    private Color foodColor;

    // Posisi monster (tetap, bisa diubah sesuai selera)
    private Point[] monsters;

    // Efek partikel saat makan
    private class Particle {
        double x, y, dx, dy;
        Color color;
        int life;
        Particle(double x, double y, double dx, double dy, Color color, int life) {
            this.x = x; this.y = y; this.dx = dx; this.dy = dy; this.color = color; this.life = life;
        }
    }
    private java.util.List<Particle> particles = new java.util.ArrayList<>();

    private long lastFpsTime = 0;
    private int frames = 0;
    private int currentFps = 0;

    public void initialize() {
        width = (int) gameCanvas.getWidth();
        height = (int) gameCanvas.getHeight();
        // Inisialisasi posisi monster setelah width/height diketahui
        monsters = new Point[] {
            new Point(60, 60),
            new Point(width/2, height - 80),
            new Point(width - 80, height/2)
        };
        resetGame();

        timeline = new Timeline(new KeyFrame(Duration.millis(speed), e -> run()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        gameCanvas.setFocusTraversable(true);
        gameCanvas.setOnKeyPressed(this::handleKey);

        overlay.setVisible(false);
        backButton.setOnAction(e -> handleBack());

        if (infoLabel != null) {
            infoLabel.setText("Tekan 1: Lambat, 2: Normal, 3: Cepat");
        }

        snakeColor = selectedSnakeColor;
        foodColor = selectedFoodColor;
        if (fpsLabel != null) fpsLabel.setText("FPS: 0");
    }

    private void handleKey(KeyEvent e) {
        switch (e.getCode()) {
            case UP -> { if (dy == 0) { dx = 0; dy = -tileSize; } }
            case DOWN -> { if (dy == 0) { dx = 0; dy = tileSize; } }
            case LEFT -> { if (dx == 0) { dx = -tileSize; dy = 0; } }
            case RIGHT -> { if (dx == 0) { dx = tileSize; dy = 0; } }
            case DIGIT1 -> changeSpeed(150); // lambat
            case DIGIT2 -> changeSpeed(100); // normal
            case DIGIT3 -> changeSpeed(60); // cepat
            case P -> togglePause();
            default -> {}
        }
    }

    // Gambar monster di arena
    private void drawMonsters(GraphicsContext gc) {
        for (Point m : monsters) {
            // Badan monster (lingkaran ungu)
            gc.setFill(Color.MEDIUMPURPLE);
            gc.fillOval(m.x, m.y, tileSize * 2, tileSize * 2);
            // Mata monster
            gc.setFill(Color.WHITE);
            gc.fillOval(m.x + 5, m.y + 5, 5, 5);
            gc.fillOval(m.x + tileSize * 2 - 10, m.y + 5, 5, 5);
            gc.setFill(Color.BLACK);
            gc.fillOval(m.x + 7, m.y + 7, 2, 2);
            gc.fillOval(m.x + tileSize * 2 - 8, m.y + 7, 2, 2);
            // Mulut monster
            gc.setStroke(Color.DARKRED);
            gc.setLineWidth(2);
            gc.strokeArc(m.x + 6, m.y + tileSize + 2, tileSize, 6, 0, 180, null);
        }
    }

    private void draw() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, width, height);

        // Gambar wall di sekeliling arena
        gc.setFill(Color.DARKGRAY);
        int wallThickness = 10;
        gc.fillRect(0, 0, width, wallThickness);
        gc.fillRect(0, height - wallThickness, width, wallThickness);
        gc.fillRect(0, 0, wallThickness, height);
        gc.fillRect(width - wallThickness, 0, wallThickness, height);

        // Gambar monster
        drawMonsters(gc);

        // Gambar makanan (apel)
        gc.setFill(foodColor); // badan apel
        gc.fillOval(food.x, food.y, tileSize, tileSize);
        gc.setStroke(Color.SADDLEBROWN);
        gc.setLineWidth(2);
        gc.strokeLine(food.x + tileSize/2, food.y, food.x + tileSize/2, food.y - tileSize/3);
        gc.setFill(Color.LIMEGREEN);
        gc.fillOval(food.x + tileSize/2, food.y - tileSize/3, tileSize/3, tileSize/4);

        // Efek partikel
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            gc.setFill(p.color);
            gc.fillOval(p.x, p.y, 3, 3);
            p.x += p.dx;
            p.y += p.dy;
            p.life--;
            if (p.life <= 0) particles.remove(i);
        }

        // Gambar ular
        for (int i = 0; i < snake.size(); i++) {
            Point p = snake.get(i);
            gc.setFill(snakeColor);
            if (i == 0) {
                gc.fillOval(p.x, p.y, tileSize + 2, tileSize + 2);
                gc.setFill(Color.WHITE);
                gc.fillOval(p.x + 2, p.y + 2, 3, 3);
                gc.fillOval(p.x + tileSize - 3, p.y + 2, 3, 3);
                gc.setFill(Color.BLACK);
                gc.fillOval(p.x + 3, p.y + 3, 1.5, 1.5);
                gc.fillOval(p.x + tileSize - 2, p.y + 3, 1.5, 1.5);
            } else {
                gc.setFill(snakeColor);
                gc.fillOval(p.x, p.y, tileSize, tileSize);
            }
        }

        // FPS counter
        frames++;
        long now = System.currentTimeMillis();
        if (now - lastFpsTime >= 1000) {
            currentFps = frames;
            frames = 0;
            lastFpsTime = now;
            if (fpsLabel != null) fpsLabel.setText("FPS: " + currentFps);
        }

        scoreLabel.setText("Skor: " + score);
    }

    private boolean isCollideWithMonster(Point head) {
        for (Point m : monsters) {
            int mx = m.x, my = m.y;
            int r = tileSize + 2; // radius kepala ular
            int mr = tileSize * 2; // diameter monster
            // Cek overlap bounding box sederhana
            if (head.x + r > mx && head.x < mx + mr && head.y + r > my && head.y < my + mr) {
                return true;
            }
        }
        return false;
    }

    private void run() {
        if (paused) return;

        Point head = new Point(snake.getFirst().x + dx, snake.getFirst().y + dy);

        int wallThickness = 10;
        // Cek tabrak wall (area abu-abu), badan, atau monster
        if (head.x < wallThickness || head.y < wallThickness ||
            head.x >= width - wallThickness || head.y >= height - wallThickness ||
            snake.contains(head) || isCollideWithMonster(head)) {
            saveScore(score);
            timeline.stop();
            overlay.setVisible(true);
            if (gameOverLabel != null) {
                gameOverLabel.setText("\uD83D\uDC80 GAME OVER \uD83D\uDC80");
                // Animasi game over: teks membesar dan berkedip
                gameOverLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #e53935; -fx-effect: dropshadow(gaussian, gold, 12, 0.7, 0, 0);");
                javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(900), gameOverLabel);
                st.setFromX(1.0); st.setToX(1.25); st.setFromY(1.0); st.setToY(1.25); st.setAutoReverse(true); st.setCycleCount(6);
                st.play();
                javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(900), gameOverLabel);
                ft.setFromValue(1.0); ft.setToValue(0.3); ft.setAutoReverse(true); ft.setCycleCount(6);
                ft.play();
            }
            return;
        }

        snake.addFirst(head);

        if (head.equals(food)) {
            score += 10;
            spawnParticles(food.x + tileSize/2, food.y + tileSize/2, foodColor);
            spawnFood();
        } else {
            snake.removeLast();
        }

        draw();
    }

    private void spawnFood() {
        int cols = width / tileSize;
        int rows = height / tileSize;
        int wallThickness = 10; // harus sama dengan draw()
        int minDistance = tileSize * 3; // minimal 3 tile dari monster
        Point newFood;
        boolean tooClose;
        do {
            int minCol = wallThickness / tileSize;
            int maxCol = (width - wallThickness) / tileSize - 1;
            int minRow = wallThickness / tileSize;
            int maxRow = (height - wallThickness) / tileSize - 1;
            int col = rand.nextInt(maxCol - minCol + 1) + minCol;
            int row = rand.nextInt(maxRow - minRow + 1) + minRow;
            newFood = new Point(col * tileSize, row * tileSize);
            tooClose = false;
            if (monsters != null) {
                for (Point m : monsters) {
                    double dist = Math.hypot(newFood.x - m.x, newFood.y - m.y);
                    if (dist < minDistance) {
                        tooClose = true;
                        break;
                    }
                }
            }
        } while (snake.contains(newFood) || tooClose);
        food = newFood;
    }

    private void saveScore(int score) {
        try (Connection conn = DBUtil.connect()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO scores(score) VALUES (?)");
            stmt.setInt(1, score);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetGame() {
        score = 0;
        dx = tileSize;
        dy = 0;
        snake.clear();
        snake.add(new Point(width / 2, height / 2));
        spawnFood();
        if (gameOverLabel != null) gameOverLabel.setText("");
    }

    private void handleBack() {
        try {
            main.Main.changeScene("menu.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeSpeed(int newSpeed) {
        speed = newSpeed;
        timeline.stop();
        timeline.getKeyFrames().clear();
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(speed), e -> run()));
        timeline.play();
        if (infoLabel != null) {
            String mode = "";
            if (speed == 150) mode = "Lambat";
            else if (speed == 100) mode = "Normal";
            else if (speed == 60) mode = "Cepat";
            infoLabel.setText("Speed: " + mode + " (Tekan 1: Lambat, 2: Normal, 3: Cepat)");
        }
    }

    private void togglePause() {
        paused = !paused;
        if (paused) {
            timeline.pause();
            if (infoLabel != null) infoLabel.setText("PAUSED | Tekan P atau tombol Pause untuk lanjut");
            pauseButton.setText("Resume");
        } else {
            timeline.play();
            if (infoLabel != null) infoLabel.setText("Tekan 1: Lambat, 2: Normal, 3: Cepat | P: Pause/Resume");
            pauseButton.setText("Pause");
        }
    }

    private void spawnParticles(double x, double y, Color color) {
        for (int i = 0; i < 18; i++) {
            double angle = 2 * Math.PI * i / 18;
            double speed = 1.5 + Math.random();
            double dx = Math.cos(angle) * speed;
            double dy = Math.sin(angle) * speed;
            particles.add(new Particle(x, y, dx, dy, color, 18 + (int)(Math.random()*8)));
        }
    }
}