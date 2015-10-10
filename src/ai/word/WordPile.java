package ai.word;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ai.exception.BopomofoException;
import ai.exception.RelationConvertException;
import ai.exception.RelationWordException;
import ai.exception.TopicWordException;
import ai.net.BopomofoCrawler;

public class WordPile {
	
	/* relationPile 把詞依照 relation 分類。第一層 index 依照 relation 分類，
	 * 第二層 index 依照詞是在 star/end 分類，第三層 index 依照詞的長度分類  */
	private ArrayList<ArrayList<ArrayList<ArrayList<ChineseWord>>>>  relationPile = new ArrayList<ArrayList<ArrayList<ArrayList<ChineseWord>>>>();
	private ArrayList<ArrayList<ChineseWord>> topicWord = new ArrayList<ArrayList<ChineseWord>>();
	private ArrayList<ChineseWord> allWords;
	private int totalWordCount;
	private ChineseWord topic;
	private int topicWordType;
	private Random rand = new Random();
	private HashMap<String, Boolean> isRecord;
	
	public WordPile(String topic, int wordType) {
		initLsit();
		allWords = new ArrayList<ChineseWord>();
		totalWordCount = 0;
		isRecord = new HashMap<String, Boolean>();
		SetTopic(topic, wordType);
	}
	private void SetTopic(String topic, int wordType){
		try {
			this.topicWordType = wordType;
			this.topic = new ChineseWord(topic, BopomofoCrawler.getBopomofo(topic), wordType, Relation.TOPIC,Relation.START,"主題：" + topic);
			topicWord.get(this.topic.getLength()).add(this.topic);
		} catch (BopomofoException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public ChineseWord getTopic(){
		return topic;
	}
	
	public ChineseWord getTopicWord(int length) throws TopicWordException{
		if (length > 3 || topicWord.get(length).isEmpty()){
			throw new TopicWordException(length);
		}
		else{
			int index = rand.nextInt(topicWord.get(length).size());
			return topicWord.get(length).get(index);
		}
	}
	
	public void addTopicWord(String topic){
		try {
			ChineseWord word = new ChineseWord(topic, BopomofoCrawler.getBopomofo(topic), topicWordType, Relation.TOPIC,Relation.START,"topic : "+topic);
			topicWord.get(word.getLength()).add(word);
		} catch (BopomofoException e) {
			e.printStackTrace();
		}
	}
	
	public void addTopicWord(ChineseWord word){
		topicWord.get(word.getLength()).add(word);
	}
	
	public void addWords(List<ChineseWord> wordList){
		for (int i = 0 ; i < wordList.size() ; i++){
			allWords.add(wordList.get(i));
		}
		
		for (ChineseWord word : wordList){
			if(!isRecord.containsKey(word.getWord())){
				isRecord.put(word.getWord(), true);
				Relation relation = word.getRelation();
				if (relation.getIndex() >= 0){
					relationPile.get(relation.getIndex()).get(word.getStartOrEnd()).get(word.getLength()).add(word);
				}
				else if (relation == Relation.TOPIC){
					addTopicWord(word);
				}
				totalWordCount += 1;
			}
			else {
				//System.err.println(word.getWord()+"已經出現過了");
			}
		}
		System.out.printf("目前共有 %d 個詞\n",totalWordCount);
	}
	
	public void addWords(ChineseWord[] wordList){
		addWords(Arrays.asList(wordList));
	}
	
	
	
	public int getTotalWordCount() {
		return totalWordCount;
	}
	
	/**
	 * nounWord 儲存名詞，第 i 個 list 儲存長度是 i+1 的詞
	 * adjWord 是名詞，verbWord 存動詞，結構和 nounWord 相同
	 * 
	 * relationPile 則是把詞依照 relation 分類
	 * 第一層 index 依照 relation 分類
	 * 第二層 index 依照詞是在 star/end 分類
	 * 第三層 index 依照詞的長度分類 
	 */
	private void initLsit(){
		for ( int i = 0 ; i < Relation.TOTAL_RELATION ; i++){
			relationPile.add(new ArrayList<ArrayList<ArrayList<ChineseWord>>>());
			relationPile.get(i).add(new ArrayList<ArrayList<ChineseWord>>());
			relationPile.get(i).add(new ArrayList<ArrayList<ChineseWord>>());
			relationPile.get(i).get(0).add(new ArrayList<ChineseWord>());
			relationPile.get(i).get(0).add(new ArrayList<ChineseWord>());
			relationPile.get(i).get(0).add(new ArrayList<ChineseWord>());
			relationPile.get(i).get(0).add(new ArrayList<ChineseWord>());
			relationPile.get(i).get(1).add(new ArrayList<ChineseWord>());
			relationPile.get(i).get(1).add(new ArrayList<ChineseWord>());
			relationPile.get(i).get(1).add(new ArrayList<ChineseWord>());
			relationPile.get(i).get(1).add(new ArrayList<ChineseWord>());	
		}
		for (int i = 0 ; i <= 3 ; i++)
			topicWord.add(new ArrayList<ChineseWord>());
	}
	
	public String getJSONString(){
		JSONObject json = new JSONObject();
		JSONArray arr = new JSONArray();
		for ( ChineseWord word : allWords){
			JSONObject obj = new JSONObject(word);
			arr.put(obj);
		}
		
		try {
			json.put("totalWord", totalWordCount);
			json.put("wordPile",arr);
		} catch (JSONException e) {
			e.printStackTrace();
			return "{}";
		}
		return json.toString();
	}
	
	
	/**
	 * 從 relationPile 中隨機取出一個符合條件的詞，若是沒有符合的詞則會回傳null
	 * 
	 * @param relation 參見ai.word.Relation
	 * @param startOrEnd start : 0 / end : 1
	 * @param length 詞的長度
	 * @return 
	 * @throws RelationWordException 
	 */
	public ChineseWord getRlationWord(Relation relation,int startOrEnd, int length) throws RelationWordException{
		ArrayList<ChineseWord> list = relationPile.get(relation.getIndex()).get(startOrEnd).get(length);
		
		if (list.size() == 0){
			throw new RelationWordException(relation.getIndex(),length);
		}
		else{
			int index = rand.nextInt(list.size());
			return list.get(index);
		}
		
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("詞庫中共有 %d 個詞\n",totalWordCount));
		for (int i = 0 ; i < Relation.TOTAL_RELATION ; i++){
			try {
				sb.append(Relation.getRelation(i).toString()+"\n");
				sb.append("start : ");
				for ( int j = 1 ; j <= 3 ;j++){
					sb.append(relationPile.get(i).get(0).get(j).size());
					if ( j < 3)
						sb.append(" ,");
				}
				sb.append('\n');
				sb.append("end : ");
				for ( int j = 1 ; j <= 3 ;j++){
					sb.append(relationPile.get(i).get(1).get(j).size());
					if ( j < 3)
						sb.append(" ,");
				}
				sb.append('\n');
				sb.append('\n');
			} catch (RelationConvertException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}
		}
		return sb.toString();
	}
	
	public void printWordPileStatistic(){
		System.out.println("=== 詞庫統計 ===");
		System.out.println("詞庫共有"+totalWordCount+"個詞");
		for (int i = 0 ; i < Relation.TOTAL_RELATION ; i++){
			try {
				ArrayList<ArrayList<ChineseWord>> temp;
				System.out.println(i+". "+Relation.getRelation(i).toString());
				temp = relationPile.get(i).get(Relation.START);
				System.out.printf("start : %d %d %d\n",temp.get(1).size(),temp.get(2).size(),temp.get(3).size());
				temp = relationPile.get(i).get(Relation.END);
				System.out.printf("start : %d %d %d\n",temp.get(1).size(),temp.get(2).size(),temp.get(3).size());
				System.out.println();
			} catch (RelationConvertException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	public void printBopomofo(){
		HashMap<Character, Integer> rhythm = new HashMap<>();
		for (ChineseWord word: allWords){
			char r = word.getRhythm();
			if (rhythm.containsKey(r)){
				rhythm.put(r,rhythm.get(r)+1);
			}
			else{
				rhythm.put(r,1);
			}
		}
		System.out.println("=== 韻腳統計 ===");
		for (Character c : rhythm.keySet()){
			System.out.println(c+" : "+rhythm.get(c));
		}
	}
	public ArrayList<ChineseWord> getAllWords() {
		return allWords;
	}
}
