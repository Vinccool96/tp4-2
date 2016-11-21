package collisionneur.modele;

import static java.lang.Math.sqrt;

import java.util.ListIterator;

import javafx.animation.AnimationTimer;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;

public class CollisionThread implements Runnable {
	private ObservableList<Particule> particules;
	private Pane ballContainer;

	public void startAnimation(final Pane ballContainer) {
		final LongProperty lastUpdateTime = new SimpleLongProperty(0);
		final AnimationTimer timer = new AnimationTimer() {

			@Override
			public void handle(long now) {
				if (lastUpdateTime.get() > 0) {
					long elapsedTime = now - lastUpdateTime.get();
					checkCollisions(ballContainer.getWidth(), ballContainer.getHeight());
					updateWorld(elapsedTime);
				}
				lastUpdateTime.set(now);
			}

		};
		timer.start();
	}

	private void updateWorld(long elapsedTime) {
		double elapsedSeconds = elapsedTime / 1_000_000_000.0;
		for (Particule b : particules) {
			b.setCentreX(b.getCentreX() + elapsedSeconds * b.getVitesseX());
			b.setCentreY(b.getCentreY() + elapsedSeconds * b.getVitesseY());
		}
	}

	public void checkCollisions(double maxX, double maxY) {
		for (ListIterator<Particule> slowIt = particules.listIterator(); slowIt.hasNext();) {
			Particule b1 = slowIt.next();
			// check wall collisions:
			double xVel = b1.getVitesseX();
			double yVel = b1.getVitesseY();
			if ((b1.getCentreX() - b1.getRayon() <= 0 && xVel < 0)
					|| (b1.getCentreX() + b1.getRayon() >= maxX && xVel > 0)) {
				b1.setVitesseX(-xVel);
			}
			if ((b1.getCentreY() - b1.getRayon() <= 0 && yVel < 0)
					|| (b1.getCentreY() + b1.getRayon() >= maxY && yVel > 0)) {
				b1.setVitesseY(-yVel);
			}
			for (ListIterator<Particule> fastIt = particules.listIterator(slowIt.nextIndex()); fastIt.hasNext();) {
				Particule b2 = fastIt.next();
				// performance hack: both colliding(...) and bounce(...) need
				// deltaX and deltaY, so compute them once here:
				final double deltaX = b2.getCentreX() - b1.getCentreX();
				final double deltaY = b2.getCentreY() - b1.getCentreY();
				if (colliding(b1, b2, deltaX, deltaY)) {
					bounce(b1, b2, deltaX, deltaY);
				}
			}
		}

	}

	public boolean colliding(final Particule b1, final Particule b2, final double deltaX, final double deltaY) {
		// square of distance between balls is s^2 = (x2-x1)^2 + (y2-y1)^2
		// balls are "overlapping" if s^2 < (r1 + r2)^2
		// We also check that distance is decreasing, i.e.
		// d/dt(s^2) < 0:
		// 2(x2-x1)(x2'-x1') + 2(y2-y1)(y2'-y1') < 0

		final double radiusSum = b1.getRayon() + b2.getRayon();
		if (deltaX * deltaX + deltaY * deltaY <= radiusSum * radiusSum) {
			if (deltaX * (b2.getVitesseX() - b1.getVitesseX()) + deltaY * (b2.getVitesseY() - b1.getVitesseY()) < 0) {
				return true;
			}
		}
		return false;
	}

	private void bounce(final Particule b1, final Particule b2, final double deltaX, final double deltaY) {
		final double distance = sqrt(deltaX * deltaX + deltaY * deltaY);
		final double unitContactX = deltaX / distance;
		final double unitContactY = deltaY / distance;

		final double xVelocity1 = b1.getVitesseX();
		final double yVelocity1 = b1.getVitesseY();
		final double xVelocity2 = b2.getVitesseX();
		final double yVelocity2 = b2.getVitesseY();

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

		final double massSum = b1.getRayon() * 5 + b2.getRayon() * 5;
		final double massDiff = b1.getRayon() * 5 - b2.getRayon() * 5;

		final double v1 = (2 * b2.getRayon() * 5 * u2 + u1 * massDiff) / massSum; // These
																					// equations
																					// are
																					// derived
																					// for
																					// one-dimensional
																					// collision
																					// by
		final double v2 = (2 * b1.getRayon() * 5 * u1 - u2 * massDiff) / massSum; // solving
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

		b1.setVitesseX(v1 * unitContactX + u1PerpX);
		b1.setVitesseY(v1 * unitContactY + u1PerpY);
		b2.setVitesseX(v2 * unitContactX + u2PerpX);
		b2.setVitesseY(v2 * unitContactY + u2PerpY);

	}

	public CollisionThread(Pane pane, ObservableList<Particule> particules) {
		ballContainer = pane;
		this.particules = particules;
		run();
	}

	@Override
	public void run() {
		startAnimation(ballContainer);

	}

}
