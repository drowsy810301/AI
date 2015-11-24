package ai.GeneticAlgorithm;

import java.util.ArrayList;
import java.util.Random;

public class MyRandom extends Random{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6121186178511649937L;
	int bound;
	ArrayList<Integer> indexList;
	public MyRandom() {
		super();
		bound = 0;
		indexList = new ArrayList<Integer>();
	}
	/**
     * 	回傳 0 - (bound-1) 之間的隨機整數
     *	如果 bound <= 0，則會傳上次0 - bound中還沒被選到的隨機整數
     * @param bound 現住回傳的隨機數字的範圍
     * @return 0 ~ (bound-1) 之間的隨機整數
     */
	public int getNoRepeatInt(int bound){
		if ( bound > 0){
			indexList.clear();
			for ( int i = 0 ; i < bound ; i++)
				indexList.add(i);
		}
		if (indexList.isEmpty())
			return 0;
		int index = this.nextInt(indexList.size());
		int temp = indexList.get(index);
		indexList.remove(index);
		return temp;
	}
	
	public Object getRandomObject(ArrayList<?> list) {
		if (list.isEmpty())
			return null;
		int index = this.nextInt(list.size());
		Object obj = list.get(index);
		list.remove(index);
		return obj;
	}
}
