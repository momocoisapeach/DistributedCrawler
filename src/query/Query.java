package query;

import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * The Class Query.
 */
public class Query {
	
	/** The base. */
	int base = 30;
	
	/** The window. */
	int window = 3;

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		Query q = new Query();
		String[] query = new String[] { "best", "buy" };
		ArrayList<Integer> best = new ArrayList<Integer>();
		ArrayList<Integer> buy = new ArrayList<Integer>();
		ArrayList<Integer> store = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> positions = new ArrayList<ArrayList<Integer>>();
		best.add(1);
		best.add(32);

		buy.add(3);
		buy.add(35);

		store.add(5);
		store.add(38);

		positions.add(best);
		positions.add(buy);
		positions.add(store);

		System.out.println("the score is " + q.orderValue(positions));
	}

	/**
	 * Order value.
	 *
	 * @param positions the positions
	 * @return the int
	 */
	public int orderValue(ArrayList<ArrayList<Integer>> positions) {
		int totalScore = 0;

		for (int i = 0; i < positions.size() - 1; i++) {
			int score = 0, j = 0, k = 0, dis;
			boolean firstTime = true;
			ArrayList<Integer> word1 = positions.get(i);
			ArrayList<Integer> word2 = positions.get(i + 1);

			// iterate through the two position lists, and both start from index
			// 0
			while (j < word1.size() && k < word2.size()) {
				System.out.println("word1 position = " + word1.get(j)
						+ "\nword2 position =" + word2.get(k) + "\nscore is "
						+ score + "\n");
				dis = word2.get(k) - word1.get(j);
				/*
				 * if it's "word2 ... word1" move the pointer of word2 to next
				 */
				if (dis <= 0) {
					System.out.println("word2 is before word1!!");
					k++;
				}
				/*
				 * if it's "word1 ... word2" and their distance is smaller than
				 * the window
				 */
				else if (dis > 0 && dis <= window) {
					System.out.println("this is what we want!!");
					if (firstTime) {
						System.out.println("and it's first time! ");
						score = base;
						firstTime = false;
					} else {
						System.out
								.println("although we already have this, but it's still good");
						score += 1;
					}
					j++;
				}
				/*
				 * if it's "word1 ... word2" but their distance is greater than
				 * window move the pointer of word1 to next
				 */
				else {
					System.out.println("word1 needs to move to next!");
					j++;

				}
			}
			totalScore += score;
		}
		return totalScore;
	}

	/**
	 * Un order value.
	 *
	 * @param positions the positions
	 * @return the double
	 */
	public double unOrderValue(ArrayList<ArrayList<Integer>> positions) {
		return 0.0;
	}

}
