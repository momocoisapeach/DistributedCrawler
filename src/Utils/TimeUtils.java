package Utils;

import java.util.Date;
import java.util.Random;

public class TimeUtils {
	
	public static Random rand;
	
	/**
	 * return a date with a random variation of +- millis from current time
	 * @return
	 */
	public static Date randomDate(int millis) {
		return new Date(new Date().getTime() + (long) rand.nextInt(millis * 2) - millis);
	}
}
