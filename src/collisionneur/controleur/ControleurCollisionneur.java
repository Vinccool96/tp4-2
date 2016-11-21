package collisionneur.controleur;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.paint.Color.BLUE;
import static javafx.scene.paint.Color.BROWN;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.PINK;
import static javafx.scene.paint.Color.RED;
import static javafx.scene.paint.Color.YELLOW;

import java.util.ListIterator;
import java.util.Random;

import collisionneur.modele.Calculs;
import collisionneur.modele.Particule;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ControleurCollisionneur extends Application {

	@FXML
	private Pane ballContainer;

	@FXML
	private BorderPane root;

	@FXML
	private Label lblNbrFormes;

	@FXML
	private Button buttonQuitter;

	@FXML
	private Button buttonReinitialiser;

	@FXML
	private Button buttonGenerer;

	@FXML
	private Slider vitesseIO;

	@FXML
	private Slider angleIO;

	@FXML
	private Slider rayonIO;

	@FXML
	private ColorPicker theChosenOne;
	private ObservableList<Ball> balls = FXCollections.observableArrayList();
	private boolean hasAnimationStarted = false;

	private static final int NUM_BALLS = 6;

	@FXML
	private void justDoIt() {

		createBalls();

	}

	@FXML
	private void restInPeace() {
		balls.clear();
		ballContainer.getChildren().clear();
	}

	@FXML
	private void iQuit() {
		Platform.exit();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/collisionneur/vue/VueCollisionneur.fxml"));

		root = loader.load();

		lblNbrFormes = (Label) ((VBox) ((GridPane) ((VBox) ((HBox) (root.getChildren().get(1))).getChildren().get(0))
				.getChildren().get(0)).getChildren().get(0)).getChildren().get(1);
		Scene scene = new Scene(root);

		ballContainer = (Pane) root.getChildren().get(0);

		constrainBallsOnResize(ballContainer);

		ballContainer.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getButton() == MouseButton.PRIMARY) {
					createBalls(valeurIO(rayonIO), valeurIO(vitesseIO), valeurIO(angleIO), event.getX(), event.getY(),
							theChosenOne.getValue());
				}
			}
		});

		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private double valeurIO(Slider io) {
		return io.getValue();
	}

	private void startAnimation(Pane ballContainer) {
		hasAnimationStarted = true;
		final LongProperty lastUpdateTime = new SimpleLongProperty(0);
		final AnimationTimer timer = new AnimationTimer() {
			@Override
			public void handle(long timestamp) {
				if (lastUpdateTime.get() > 0) {
					long elapsedTime = timestamp - lastUpdateTime.get();
					checkCollisions(ballContainer.getWidth(), ballContainer.getHeight());
					updateWorld(elapsedTime);
				}
				lastUpdateTime.set(timestamp);
			}

		};
		timer.start();
	}

	private void updateWorld(long elapsedTime) {
		double elapsedSeconds = elapsedTime / 1_000_000_000.0;
		for (Ball b : balls) {
			b.setCenterX(b.getCenterX() + elapsedSeconds * b.getXVelocity());
			b.setCenterY(b.getCenterY() + elapsedSeconds * b.getYVelocity());
		}
	}

	private void checkCollisions(double maxX, double maxY) {
		for (ListIterator<Ball> slowIt = balls.listIterator(); slowIt.hasNext();) {
			Ball b1 = slowIt.next();
			// check wall collisions:
			double xVel = b1.getXVelocity();
			double yVel = b1.getYVelocity();
			if ((b1.getCenterX() - b1.getRadius() <= 0 && xVel < 0)
					|| (b1.getCenterX() + b1.getRadius() >= maxX && xVel > 0)) {
				b1.setXVelocity(-xVel);
			}
			if ((b1.getCenterY() - b1.getRadius() <= 0 && yVel < 0)
					|| (b1.getCenterY() + b1.getRadius() >= maxY && yVel > 0)) {
				b1.setYVelocity(-yVel);
			}
			for (ListIterator<Ball> fastIt = balls.listIterator(slowIt.nextIndex()); fastIt.hasNext();) {
				Ball b2 = fastIt.next();
				// performance hack: both colliding(...) and bounce(...) need
				// deltaX and deltaY, so compute them once here:
				final double deltaX = b2.getCenterX() - b1.getCenterX();
				final double deltaY = b2.getCenterY() - b1.getCenterY();
				if (colliding(b1, b2, deltaX, deltaY)) {
					bounce(b1, b2, deltaX, deltaY);
				}
			}
		}
	}

	public boolean colliding(final Ball b1, final Ball b2, final double deltaX, final double deltaY) {
		// square of distance between balls is s^2 = (x2-x1)^2 + (y2-y1)^2
		// balls are "overlapping" if s^2 < (r1 + r2)^2
		// We also check that distance is decreasing, i.e.
		// d/dt(s^2) < 0:
		// 2(x2-x1)(x2'-x1') + 2(y2-y1)(y2'-y1') < 0

		final double radiusSum = b1.getRadius() + b2.getRadius();
		if (deltaX * deltaX + deltaY * deltaY <= radiusSum * radiusSum) {
			if (deltaX * (b2.getXVelocity() - b1.getXVelocity())
					+ deltaY * (b2.getYVelocity() - b1.getYVelocity()) < 0) {
				return true;
			}
		}
		return false;
	}

	private void bounce(final Ball b1, final Ball b2, final double deltaX, final double deltaY) {
		final double distance = sqrt(deltaX * deltaX + deltaY * deltaY);
		final double unitContactX = deltaX / distance;
		final double unitContactY = deltaY / distance;

		final double xVelocity1 = b1.getXVelocity();
		final double yVelocity1 = b1.getYVelocity();
		final double xVelocity2 = b2.getXVelocity();
		final double yVelocity2 = b2.getYVelocity();

		final double u1 = xVelocity1 * unitContactX + yVelocity1 * unitContactY; // velocity
																					// of
																					// ball
																					// 1
																					// parallel
																					// to
																					// contact
																					// vector
		final double u2 = xVelocity2 * unitContactX + yVelocity2 * unitContactY; // same
																					// for
																					// ball
																					// 2

		final double massSum = b1.getMass() + b2.getMass();
		final double massDiff = b1.getMass() - b2.getMass();

		final double v1 = (2 * b2.getMass() * u2 + u1 * massDiff) / massSum; // These
																				// equations
																				// are
																				// derived
																				// for
																				// one-dimensional
																				// collision
																				// by
		final double v2 = (2 * b1.getMass() * u1 - u2 * massDiff) / massSum; // solving
																				// equations
																				// for
																				// conservation
																				// of
																				// momentum
																				// and
																				// conservation
																				// of
																				// energy

		final double u1PerpX = xVelocity1 - u1 * unitContactX; // Components of
																// ball 1
																// velocity in
																// direction
																// perpendicular
		final double u1PerpY = yVelocity1 - u1 * unitContactY; // to contact
																// vector. This
																// doesn't
																// change with
																// collision
		final double u2PerpX = xVelocity2 - u2 * unitContactX; // Same for ball
																// 2....
		final double u2PerpY = yVelocity2 - u2 * unitContactY;

		b1.setXVelocity(v1 * unitContactX + u1PerpX);
		b1.setYVelocity(v1 * unitContactY + u1PerpY);
		b2.setXVelocity(v2 * unitContactX + u2PerpX);
		b2.setYVelocity(v2 * unitContactY + u2PerpY);

	}

	private void createBalls(double radius, double speed, double angle, double initialX, double initialY, Color color) {
		Ball ball = new Ball(initialX, initialY, radius, speed * cos(angle), speed * sin(angle));
		ball.getView().setFill(color);
		// ball.getView().setFill(i==0 ? RED : TRANSPARENT);
		balls.add(ball);
		ballContainer.getChildren().add(ball.getView());
		if (!hasAnimationStarted) {
			startAnimation(ballContainer);
		}

	}

	private void createBalls() {
		for (int i = 0; i < NUM_BALLS; i++) {
			Random random = new Random();
			double rayon = random.nextDouble() * (10 - 4) + (4);
			ajusterIO(rayonIO, rayon);
			double vitesse = random.nextDouble() * 10;
			ajusterIO(vitesseIO, vitesse);
			double angle = random.nextDouble() * 360;
			ajusterIO(angleIO, angle);

			double red = random.nextDouble();
			double green = random.nextDouble();
			double blue = random.nextDouble();

			Color color = new Color(red, green, blue, 1);

			ajusterTheChosenOne(theChosenOne, color);

			createBalls(rayon, vitesse, angle, ballContainer.getWidth() / 2, ballContainer.getHeight() / 2, color);

		}
	}

	private void ajusterIO(Slider io, double value) {
		io.setValue(value);
	}

	private void ajusterTheChosenOne(ColorPicker theChosenOne, Color color) {
		theChosenOne.setValue(color);
	}

	private void constrainBallsOnResize(final Pane ballContainer) {
		ballContainer.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (newValue.doubleValue() < oldValue.doubleValue()) {
					for (Ball b : balls) {
						double max = newValue.doubleValue() - b.getRadius();
						if (b.getCenterX() > max) {
							b.setCenterX(max);
						}
					}
				}
			}
		});

		ballContainer.heightProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (newValue.doubleValue() < oldValue.doubleValue()) {
					for (Ball b : balls) {
						double max = newValue.doubleValue() - b.getRadius();
						if (b.getCenterY() > max) {
							b.setCenterY(max);
						}
					}
				}
			}

		});
	}

	public static void main(String[] args) {
		launch(args);
	}

}