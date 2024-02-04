import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;
import java.util.Vector;

public class SnakeApp extends Application {

    private Vector<Rectangle> snake = new Vector<>();
    private Rectangle redPixel;
    private Direction direction = Direction.RIGHT;
    private int score = 0;
    private Timeline timeline;
    private StackPane root;
    private Label scoreLabel;
    private Scene scene; // Added for access in methods

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        root = new StackPane();
        scene = new Scene(root, 600, 600);

        // Add a black square
        Rectangle square = new Rectangle(280, 300, 20, 20);
        square.setFill(Color.BLACK);
        root.getChildren().add(square);

        // Add a rectangle to serve as a boundary
        Rectangle limit = new Rectangle(0, 0, 600, 600);
        limit.setFill(Color.WHITE);
        root.getChildren().add(limit);

        // Add 4 buttons
        Button upButton = new Button("Up");
        Button downButton = new Button("Down");
        Button leftButton = new Button("Left");
        Button rightButton = new Button("Right");
        root.getChildren().addAll(upButton, downButton, leftButton, rightButton);

        // Add event handlers for the buttons
        upButton.setOnAction(e -> moveSnake(Direction.UP));
        downButton.setOnAction(e -> moveSnake(Direction.DOWN));
        leftButton.setOnAction(e -> moveSnake(Direction.LEFT));
        rightButton.setOnAction(e -> moveSnake(Direction.RIGHT));

        // Add event for arrow key presses
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case UP:
                    moveSnake(Direction.UP);
                    break;
                case DOWN:
                    moveSnake(Direction.DOWN);
                    break;
                case LEFT:
                    moveSnake(Direction.LEFT);
                    break;
                case RIGHT:
                    moveSnake(Direction.RIGHT);
                    break;
            }
        });

        // Add a red pixel
        spawnRedPixel();

        // Add a label
        scoreLabel = new Label("Score: 0");
        root.getChildren().add(scoreLabel);

        // Add a Timeline to move the snake
        timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            moveSnake(direction);
            checkCollision();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        primaryStage.setScene(scene);
        primaryStage.setTitle("Snake Game");
        primaryStage.show();
    }

    private void moveSnake(Direction direction) {
        for (int i = snake.size() - 1; i > 0; i--) {
            snake.get(i).setX(snake.get(i - 1).getX());
            snake.get(i).setY(snake.get(i - 1).getY());
        }

        switch (direction) {
            case UP:
                snake.get(0).setY(snake.get(0).getY() - 20);
                break;
            case DOWN:
                snake.get(0).setY(snake.get(0).getY() + 20);
                break;
            case LEFT:
                snake.get(0).setX(snake.get(0).getX() - 20);
                break;
            case RIGHT:
                snake.get(0).setX(snake.get(0).getX() + 20);
                break;
        }
    }

    private void spawnRedPixel() {
        Random rand = new Random();
        double x, y;
        do {
            x = rand.nextInt((int) scene.getWidth() - 20);
            y = rand.nextInt((int) scene.getHeight() - 20);
        } while (isPixelOnSnake(x, y));

        redPixel = new Rectangle(x, y, 20, 20);
        redPixel.setFill(Color.RED);
        root.getChildren().add(redPixel);
    }

    private boolean isPixelOnSnake(double x, double y) {
        for (Rectangle segment : snake) {
            if (segment.getX() == x && segment.getY() == y) {
                return true;
            }
        }
        return false;
    }

    private void checkCollision() {
        // Check if the snake has eaten the red pixel
        if (redPixel != null && redPixel.getBoundsInParent().intersects(snake.get(0).getBoundsInParent())) {
            score++;
            scoreLabel.setText("Score: " + score);
            snake.add(new Rectangle(snake.get(snake.size() - 1).getX(), snake.get(snake.size() - 1).getY(), 20, 20));
            spawnRedPixel();
        }

        // Check if the snake has bitten itself
        for (int i = 1; i < snake.size(); i++) {
            if (snake.get(0).getBoundsInParent().intersects(snake.get(i).getBoundsInParent())) {
                gameOver();
            }
        }

        // Check if the snake has gone out of bounds
        if (snake.get(0).getX() < 0 || snake.get(0).getX() > scene.getWidth() - 20 ||
                snake.get(0).getY() < 0 || snake.get(0).getY() > scene.getHeight() - 20) {
            gameOver();
        }
    }

    private void gameOver() {
        timeline.stop();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Your score is: " + score);
        alert.setContentText("Press OK to exit.");
        alert.showAndWait();
        System.exit(0);
    }

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
}
