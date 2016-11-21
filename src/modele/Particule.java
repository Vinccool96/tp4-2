package modele;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Particule {

	public static final int MIN_VITESSE = 0;
	public static final int MAX_VITESSE = 10;
	public static final int MIN_DIRECTION = 0;
	public static final int MAX_DIRECTION = 360;
	public static final int MIN_RAYON = 4;
	public static final int MAX_RAYON = 10;
	public static final int MIN_X = 0;
	public static final int MIN_Y = 0;
	// TODO établir la position maximum en X
	public static final int MAX_X = 100;
	// TODO établir la position maximum en Y
	public static final int MAX_Y = 100;
	private double positionY;
	private double positionX;
	// Le rayon agit aussi comme la masse pour les calculs
	private double rayon;
	private double direction;
	private Color couleur;
	private double vitesse;
	private double vitesseX;
	private double vitesseY;
	private Circle circleFX;

	public Particule(double tempVitesse, double tempDirection, double tempRayon, Color pCouleur, double tempPositionX,
			double tempPositionY) {

		if (verifierPosY(tempPositionY) && verifierPosX(tempPositionX) && verifierRayon(tempRayon)
				&& verifierDirection(tempDirection) && verifierVitesse(tempVitesse)) {
			setCouleur(pCouleur);
			setDirection(tempDirection);
			setPositionX(tempPositionX);
			setPositionY(tempPositionY);
			setRayon(tempRayon);
			setVitesse(tempVitesse);
			setCircleFX();
		}

	}

	private boolean verifierPosY(double tempPositionY) {
		boolean result = false;
		if (tempPositionY >= MIN_Y && tempPositionY <= MAX_Y) {
			result = true;
		}
		return result;
	}

	public double getPositionY() {
		return positionY;
	}

	public void setPositionY(double tempPositionY) {
		if (verifierPosY(tempPositionY)) {
			this.positionY = tempPositionY;
		}
	}

	private boolean verifierPosX(double tempPositionX) {
		boolean result = false;
		if (tempPositionX >= MIN_X && tempPositionX <= MAX_X) {
			result = true;
		}
		return result;
	}

	public double getPositionX() {
		return positionX;
	}

	public void setPositionX(double tempPositionX) {
		if (verifierPosX(tempPositionX)) {
			this.positionX = tempPositionX;
		}

	}

	private boolean verifierRayon(double tempRayon) {
		boolean result = false;
		if (tempRayon >= MIN_RAYON && tempRayon <= MAX_RAYON) {
			result = true;
		}
		return result;
	}

	public double getRayon() {
		return rayon;
	}

	public void setRayon(double tempRayon) {
		if (verifierRayon(tempRayon)) {
			this.rayon = tempRayon;
		}
	}

	private boolean verifierDirection(double tempDirection) {
		boolean result = false;
		if (tempDirection >= MIN_DIRECTION && tempDirection <= MAX_DIRECTION) {
			result = true;
		}
		return result;
	}

	public double getDirection() {
		return direction;
	}

	public void setDirection(double tempDirection) {
		if (verifierDirection(tempDirection)) {
			this.direction = tempDirection;
		}
	}

	public Color getCouleur() {
		return couleur;
	}

	public void setCouleur(Color couleur) {
		this.couleur = couleur;
	}

	private boolean verifierVitesse(double tempVitesse) {
		boolean result = false;
		if (tempVitesse >= MIN_VITESSE && tempVitesse <= MAX_VITESSE) {
			result = true;
		}
		return result;
	}

	public double getVitesse() {
		return vitesse;
	}

	public double getVitesseX() {
		return vitesseX;
	}

	public double getVitesseY() {
		return vitesseY;
	}

	public void setVitesse(double tempVitesse) {
		if (verifierVitesse(tempVitesse)) {
			this.vitesse = tempVitesse;
			setVitesseX();
			setVitesseY();
		}
	}

	private void setVitesseX() {
		vitesseX = Math.cos(getVitesse() * Math.PI / 180);
	}

	private void setVitesseY() {
		vitesseY = Math.sin(getVitesse() * Math.PI / 180);
	}

	public void setVitesseX(double vitesse) {

	}

	public void setVitesseY(double vitesse) {

	}

	public Circle getCircleFX() {
		return circleFX;
	}

	public void setCircleFX() {
		circleFX = new Circle(getRayon());
		circleFX.setFill(getCouleur());
	}

	public double getCentreX() {
		return circleFX.getCenterX();
	}

	public double getCentreY() {
		return circleFX.getCenterY();
	}

	public void setCentreX(double centreX) {
		circleFX.setCenterX(centreX);
	}

	public void setCentreY(double centreY) {
		circleFX.setCenterY(centreY);
	}

}