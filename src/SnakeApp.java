import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnakeApp extends Application {

    private static final int SQUARE_SIZE = 25;
    private static final int GRID_SIZE = 25;
    private static final int PLAYING_ZONE_SIZE = GRID_SIZE * SQUARE_SIZE;

    private Pane root;
    private BorderPane borderPane;
    private Scene scene;
    private List<Rectangle> snake;
    private Rectangle redPixel;
    private Rectangle boundary;
    private int snakeX;
    private int snakeY;
    private int score;
    private Direction currentDirection;

    private Label scoreLabel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SnakeFX");

        root = new Pane();
        borderPane = new BorderPane();
        scene = new Scene(borderPane, PLAYING_ZONE_SIZE, PLAYING_ZONE_SIZE + 100);

        // Initialize snake position in the middle of the grid
        snakeX = GRID_SIZE / 2;
        snakeY = GRID_SIZE / 2;
        currentDirection = Direction.RIGHT; // Initial direction

        snake = new ArrayList<>();
        snake.add(createSnakeSegment());
        root.getChildren().addAll(snake);

        redPixel = createRedPixel();
        root.getChildren().add(redPixel);

        scoreLabel = new Label("Score: 0");
        borderPane.setTop(scoreLabel);

        scene.setOnKeyPressed(event -> handleKeyPress(event.getCode()));

        // WASD buttons
        Button btnW = new Button("U");
        btnW.setOnAction(e -> changeDirection(Direction.UP));

        Button btnA = new Button("L");
        btnA.setOnAction(e -> changeDirection(Direction.LEFT));

        Button btnS = new Button("D");
        btnS.setOnAction(e -> changeDirection(Direction.DOWN));

        Button btnD = new Button("R");
        btnD.setOnAction(e -> changeDirection(Direction.RIGHT));

        // WASD layout
        GridPane wasdGrid = new GridPane();
        wasdGrid.add(btnW, 1, 0);
        wasdGrid.add(btnA, 0, 1);
        wasdGrid.add(btnS, 1, 1);
        wasdGrid.add(btnD, 2, 1);
        wasdGrid.setAlignment(Pos.CENTER);

        // Draw boundary around the playing zone
        boundary = new Rectangle(PLAYING_ZONE_SIZE, PLAYING_ZONE_SIZE);
        boundary.setFill(null);
        boundary.setStroke(Color.GREEN);
        boundary.setStrokeWidth(2);
        root.getChildren().add(boundary);

        // Set up the layout
        borderPane.setCenter(root);
        borderPane.setBottom(wasdGrid);
        BorderPane.setAlignment(wasdGrid, Pos.CENTER);

        primaryStage.setScene(scene);
        primaryStage.show();

        spawnRedPixel();
        startGameLoop();
    }

    private Rectangle createSnakeSegment() {
        Rectangle segment = new Rectangle(SQUARE_SIZE, SQUARE_SIZE, Color.BLACK);
        segment.setStroke(Color.BLACK);
        segment.setStrokeType(StrokeType.INSIDE);
        // Set initial position in the middle
        segment.setTranslateX(snakeX * SQUARE_SIZE);
        segment.setTranslateY(snakeY * SQUARE_SIZE);
        return segment;
    }

    private Rectangle createRedPixel() {
        Rectangle pixel = new Rectangle(SQUARE_SIZE, SQUARE_SIZE, Color.RED);
        pixel.setStroke(Color.RED);
        pixel.setStrokeType(StrokeType.INSIDE);
        return pixel;
    }

    private void spawnRedPixel() {
        Random random = new Random();
        int redPixelX;
        int redPixelY;

        do {
            redPixelX = random.nextInt(GRID_SIZE);
            redPixelY = random.nextInt(GRID_SIZE);
        } while (isOnSnake(redPixelX, redPixelY));

        redPixel.setTranslateX(redPixelX * SQUARE_SIZE);
        redPixel.setTranslateY(redPixelY * SQUARE_SIZE);
    }

    private boolean isOnSnake(int x, int y) {
        for (Rectangle segment : snake) {
            if (x == (int) segment.getTranslateX() / SQUARE_SIZE &&
                    y == (int) segment.getTranslateY() / SQUARE_SIZE) {
                return true;
            }
        }
        return false;
    }

    private void moveSnake() {
        int newSnakeX = snakeX;
        int newSnakeY = snakeY;

        // Move in the current direction
        switch (currentDirection) {
            case UP:
                newSnakeY = Math.floorMod(snakeY - 1, GRID_SIZE);
                break;
            case DOWN:
                newSnakeY = Math.floorMod(snakeY + 1, GRID_SIZE);
                break;
            case LEFT:
                newSnakeX = Math.floorMod(snakeX - 1, GRID_SIZE);
                break;
            case RIGHT:
                newSnakeX = Math.floorMod(snakeX + 1, GRID_SIZE);
                break;
        }

        // Check collision with red pixel
        if (newSnakeX == (int) redPixel.getTranslateX() / SQUARE_SIZE &&
                newSnakeY == (int) redPixel.getTranslateY() / SQUARE_SIZE) {
            // Snake has eaten the red pixel
            score++;
            scoreLabel.setText("Score: " + score);

            // Increase snake size
            Rectangle newSegment = createSnakeSegment();
            snake.add(newSegment);
            root.getChildren().add(newSegment);

            // Spawn a new red pixel
            spawnRedPixel();
        }

        // Move each snake segment
        for (int i = snake.size() - 1; i > 0; i--) {
            Rectangle currentSegment = snake.get(i);
            Rectangle nextSegment = snake.get(i - 1);
            currentSegment.setTranslateX(nextSegment.getTranslateX());
            currentSegment.setTranslateY(nextSegment.getTranslateY());
        }

        // Move the head of the snake
        snake.get(0).setTranslateX(newSnakeX * SQUARE_SIZE);
        snake.get(0).setTranslateY(newSnakeY * SQUARE_SIZE);

        snakeX = newSnakeX;
        snakeY = newSnakeY;

        checkCollisionWithBoundary();
        checkCollisionWithSelf();
    }

    private void changeDirection(Direction newDirection) {
        // Prevent going back in the opposite direction
        if (currentDirection != null && !isOppositeDirection(currentDirection, newDirection)) {
            currentDirection = newDirection;
        }
    }

    private boolean isOppositeDirection(Direction current, Direction newDirection) {
        return (current == Direction.UP && newDirection == Direction.DOWN) ||
               (current == Direction.DOWN && newDirection == Direction.UP) ||
               (current == Direction.LEFT && newDirection == Direction.RIGHT) ||
               (current == Direction.RIGHT && newDirection == Direction.LEFT);
    }

    private void checkCollisionWithBoundary() {
        if (snakeX < 0 || snakeY < 0 || snakeX >= GRID_SIZE || snakeY >= GRID_SIZE) {
            // Snake collided with the boundary, game over
            resetGame();
        }
    }

    private void checkCollisionWithSelf() {
        for (int i = 1; i < snake.size(); i++) {
            if (snakeX == (int) snake.get(i).getTranslateX() / SQUARE_SIZE &&
                snakeY == (int) snake.get(i).getTranslateY() / SQUARE_SIZE) {
                // Snake collided with itself, game over
                resetGame();
            }
        }
    }

    private void resetGame() {
        // Reset snake position and size
        snakeX = GRID_SIZE / 2;
        snakeY = GRID_SIZE / 2;
        currentDirection = Direction.RIGHT;
        snake.forEach(segment -> root.getChildren().remove(segment));
        snake.clear();
        snake.add(createSnakeSegment());
        root.getChildren().addAll(snake);

        // Reset score
        score = 0;
        scoreLabel.setText("Score: 0");

        // Respawn red pixel
        spawnRedPixel();
    }

    private void handleKeyPress(KeyCode code) {
        switch (code) {
            case W:
                changeDirection(Direction.UP);
                break;
            case A:
                changeDirection(Direction.LEFT);
                break;
            case S:
                changeDirection(Direction.DOWN);
                break;
            case D:
                changeDirection(Direction.RIGHT);
                break;
        }
    }

    private void startGameLoop() {
        // Game loop to move the snake periodically
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(150), event -> moveSnake()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
}
