package thequestforjavafx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Main extends Application {

	//tangentkodning
	private HashMap<KeyCode, Boolean> keys = new HashMap<KeyCode, Boolean>();

	//objekt i spelet
	private ArrayList<Node> platforms = new ArrayList<Node>();
	private ArrayList<Node> coins = new ArrayList<Node>();

	//Grupper för olika objekt
	private Pane appRoot = new Pane();
	private Pane gameRoot = new Pane();
	private Pane uiRoot = new Pane();

	//player
	private Node player;
	private int playerVelocityY = 0;
	private boolean canJump = true;

	//pixelvärden
	private int levelWidth;

	private boolean dialogEvent = false, running = true;

	private void initContent() {
		Rectangle bg = new Rectangle(1280, 720);

		levelWidth = LevelData.LEVEL1[0].length() * 60;
		
		//level data parse loop
		for (int i = 0; i < LevelData.LEVEL1.length; i++) {
			String line = LevelData.LEVEL1[i];
			for (int j = 0; j < line.length(); j++) {
				switch (line.charAt(j)) {
					case '0':
						break;
					case '1':
						Node platform = createEntity(j*60, i*60, 60,60, Color.GREEN);
						platforms.add(platform);
						break;
					case '2':
					Node coin = createEntity(j*60, i*60, 60, 60, Color.GOLD);
					coins.add(coin);
					break;
				}
			}
		}

		player = createEntity(0, 600, 40, 40, Color.BLUE);

		player.translateXProperty().addListener((obs, old, newValue) -> {
			int offset = newValue.intValue();

			if (offset > 640 && offset < levelWidth - 640) {
				gameRoot.setLayoutX(-(offset - 640));
			}
		});

		appRoot.getChildren().addAll(bg, gameRoot, uiRoot);
	}

	private void update() {
		//check for keyboard input
		if (isPressed(KeyCode.UP) && player.getTranslateY() >= 5) {
			jumpPlayer();
		}
		if (isPressed(KeyCode.LEFT) && player.getTranslateX() >= 5) {
			movePlayerX(-5);
		}
		if (isPressed(KeyCode.RIGHT) && player.getTranslateX() + 40 <= levelWidth - 5) {
			movePlayerX(5);
		}

		//apply gravity to player
		if (playerVelocityY < 16) {
			playerVelocityY++;
		}

		//move the player
		movePlayerY(playerVelocityY);

		//coins
		for (Node coin : coins) {
			if (player.getBoundsInParent().intersects(coin.getBoundsInParent())) {
				coin.getProperties().put("alive", false);
				//dialogEvent = true;
				//running = false;
			}
		}

		for (Iterator<Node> it = coins.iterator(); it.hasNext(); ) {
			Node coin = it.next();
			if (!(Boolean)coin.getProperties().get("alive")) {
				it.remove();
				gameRoot.getChildren().remove(coin);
			}
		}
	}

	private void movePlayerX(int value) {
		boolean movingRight = value > 0;

		for (int i = 0; i < Math.abs(value); i++) {
			for (Node platform : platforms) {
				if (player.getBoundsInParent().intersects(platform.getBoundsInParent())) {
					if (movingRight) {
						if (player.getTranslateX() + 40 == platform.getTranslateX()) {
							return;
						}
					}
					else {
						if (player.getTranslateX() == platform.getTranslateX() + 60) {
							return;
						}
					}
				}
			}
			player.setTranslateX(player.getTranslateX() + (movingRight ? 1 : -1));
		}
	}

	private void movePlayerY(int value) {
		boolean movingDown = value > 0;

		for (int i = 0; i < Math.abs(value); i++) {
			for (Node platform : platforms) {
				if (player.getBoundsInParent().intersects(platform.getBoundsInParent())) {
					if (movingDown) {
						if (player.getTranslateY() + 40 == platform.getTranslateY()) {
							player.setTranslateY(player.getTranslateY() - 1);
							canJump = true;
							playerVelocityY = 0;
							return;
						}
					}
					else {
						if (player.getTranslateY() == platform.getTranslateY() + 60) {
							return;
						}
					}
				}
			}
			player.setTranslateY(player.getTranslateY() + (movingDown ? 1 : -1));
		}
	}

	private void jumpPlayer() {
		if (canJump) {
			playerVelocityY = -20;
			canJump = false;
		}
	}

	private Node createEntity(int x, int y, int w, int h, Color color) {
		Rectangle entity = new Rectangle(w, h);
		entity.setTranslateX(x);
		entity.setTranslateY(y);
		entity.setFill(color);
		entity.getProperties().put("alive", true);

		gameRoot.getChildren().add(entity);
		return entity;
	}

	private boolean isPressed(KeyCode key) {
		return keys.getOrDefault(key, false);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		initContent();

		Scene scene = new Scene(appRoot);
		scene.setOnKeyPressed(event -> keys.put(event.getCode(), true));
		scene.setOnKeyReleased(event -> keys.put(event.getCode(), false));
		primaryStage.setTitle("Adams Java Platformer");
		primaryStage.setScene(scene);
		primaryStage.show();

		AnimationTimer timer = new AnimationTimer() {
			public void handle(long now) {
				if (running) {
					update();
				}

				if (dialogEvent) {
					dialogEvent = false;
					keys.keySet().forEach(key -> keys.put(key, false));

					GameDialog dialog = new GameDialog();
					dialog.setOnCloseRequest(event -> {
						if (dialog.isCorrect()) {
							System.out.println("Correct");
						}
						else {
						System.out.println("Wrong");
						}

						running = true;
					});
					dialog.open();
				}
			}
		};
		timer.start();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
