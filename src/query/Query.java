package query;

import java.util.ArrayList;

public class Query {

	public static void main(String[] args) {
		String[] query = new String[]{"best","buy"};
		ArrayList<Integer> best = new ArrayList<Integer>();
		ArrayList<Integer> buy = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> positions = new ArrayList<ArrayList<Integer>>();
		best.add(1);
		
		best.add(18);
		
		buy.add(3);
		buy.add(35);
		
		positions.add(best);
		positions.add(buy);

	}
	
	public int orderValue(ArrayList<ArrayList<Integer>> positions){
		int count = 0;
		for(int i = 0; i < positions.size()-1; i++){
			ArrayList<Integer> word1 = positions.get(i);
			ArrayList<Integer> word2 = positions.get(i+1);
		}
		return 0;
	}
	
	public double unOrderValue(ArrayList<ArrayList<Integer>> positions){
		return 0.0;
	}

}
