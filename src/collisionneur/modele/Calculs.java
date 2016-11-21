package collisionneur.modele;

public class Calculs {
	
	public final static int RGB_MIN = 0;
	public final static int RGB_MAX = 255;

	public static double aleatoire(int min, int max) {
		return (min + (Math.random() * ((max - min) + 1)));
	}
	
}