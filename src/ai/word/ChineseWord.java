package ai.word;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ai.exception.RelationConvertException;

public class ChineseWord {

	public static final int SINGLE = 8, NOUN = 1, ADJ = 2, VERB = 4, EMPTY = 0;;
	
	private int length;
	private String word;
	private char[] bopomofo;
	private int[] tone;
	private int wordType;
	private Relation relation;
	private int startOrEnd;
	private String surfaceText="123";
	
	/**
	 * 	建立一個詞，儲存詞性和每個字的平仄、韻腳
	 * 	@param word: 中文字詞
	 * 	@param letterBopomofo: 每個字的注音
	 * 	@param wordType: 代表詞性的數字
	 * 
	 * 	名詞: 1, 形容詞: 2, 動詞: 4
	 * 	因為詞性是從concept net 的 relation 推測的，所以一個詞可能會有多種詞性
	 * 	用二進位表示詞性需要3bit(van) 某個bit是1就表示具有該詞性 
	 * 	例如: 詞性 = 5,van = 101, 表示是動詞和名詞
	 */
	public ChineseWord(String word, String[] letterBopomofo, int wordType, Relation relation, int startOrEnd, String surfaceText){
		String str;
		
		this.length = word.length();
		this.bopomofo = new char[this.length];
		this.tone = new int[this.length];
		this.word = word;
		this.wordType = wordType;
		this.relation = relation;
		this.startOrEnd = startOrEnd;
		this.surfaceText = surfaceText;
		for (int i = 0 ; i < this.length ; i++){
			str = letterBopomofo[i];
			
			if (str.charAt(str.length()-1) =='˙'){
				this.tone[i] = 0;
				this.bopomofo[i] = str.charAt(str.length()-2);
			}
			else if (str.charAt(str.length()-1) == 'ˊ'){
				this.tone[i] = 0;
				this.bopomofo[i] = str.charAt(str.length()-2);
			}
			else if (str.charAt(str.length()-1) == 'ˇ'){
				this.tone[i] = 1;
				this.bopomofo[i] = str.charAt(str.length()-2);
			}
			else if (str.charAt(str.length()-1) == 'ˋ'){
				this.tone[i] = 1;
				this.bopomofo[i] = str.charAt(str.length()-2);
			}
			else{
				this.tone[i] = 0;
				this.bopomofo[i] = str.charAt(str.length()-1);
			}
		}
	}
	
	public ChineseWord(String word, char[] bopomofo, int[] tone, int wordType, int length, Relation relation, int startOrEnd, String surfaceText) {
		this.bopomofo = bopomofo;
		this.length = length;
		this.tone = tone;
		this.word = word;
		this.wordType = wordType;
		this.relation = relation;
		this.startOrEnd = startOrEnd;
		this.surfaceText = surfaceText;
	}
	
	public int getStartOrEnd(){
		return this.startOrEnd;
	}
	
	public Relation getRelation(){
		return this.relation;
	}
	
	public int getLength() {
		return length;
	}

	public String getWord() {
		return word;
	}
	public char[] getBopomofo() {
		return bopomofo;
	}
	public void setBopomofo(char[] bopomofo) {
		this.bopomofo = bopomofo;
	}
	public int[] getTone() {
		return tone;
	}
	public void setTone(int[] tone) {
		this.tone = tone;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public void setWordType(int wordType) {
		this.wordType = wordType;
	}
	
	public char getCharAt(int index) {
		if (index >= length){
			System.err.println("error : index out of bound");
			System.exit(1);
			return '?';
		}
		else{
			return word.charAt(index);
		}
	}
	
	/*取得平仄*/
	public int getToneAt(int index) {
		if (index >= length){
			System.err.println("error : index out of bound");
			System.exit(1);
			return -1;
		}
		else{
			return tone[index];
		}
	}
	
	/*取得韻腳*/
	public char getRhythm(){
		return bopomofo[length-1];
	}
	/*取得詞性*/
	public int getWordType() {
		return wordType;
	}
	
	/**
	 * 把中文的詞性轉換成數字
	 * @param wordType 
	 * @return 
	 */
	public static int convertWordType(String wordType){
		int value = EMPTY;
		if (wordType.indexOf("名") != -1){
			value += ChineseWord.NOUN;
		}
		if (wordType.indexOf("形") != -1){
			value += ChineseWord.ADJ;	
		}
		if (wordType.indexOf("動") != -1){
			value += ChineseWord.VERB;
		}
		if (wordType.indexOf("單") != -1){
			value += ChineseWord.SINGLE;
		}
		
		return value;
	}
	
	public static String getReadableWordType(int wordType){
		String type = new String();
		if ((wordType & NOUN )> 0)
			type += "名";
		if ((wordType & ADJ )> 0)
			type += "形";
		if ((wordType & VERB )> 0)
			type += "動";
		if ((wordType & SINGLE )> 0)
			type += "單";
		return type;
	}
	
	public void printWord(){
		System.out.println(word+" <"+getReadableWordType(wordType)+">");
		for (int i = 0; i < this.length; i++) {
			System.out.println(word.charAt(i)+", "+bopomofo[i]+", "+String.valueOf(tone[i]));
		}
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(word+" <"+getReadableWordType(wordType)+">\n");
		for (int i = 0; i < this.length; i++) {
			sb.append(word.charAt(i)+", "+bopomofo[i]+", "+String.valueOf(tone[i])+"\n");
		}
		return sb.toString();
	}

	public String getSurfaceText() {
		return surfaceText;
	}
	
	/*
	 * 從JSON Array中讀取 資料
	 */
	public static ChineseWord[] loadWords(JSONArray arr) throws JSONException, RelationConvertException{
		ChineseWord[] wordList = new ChineseWord[arr.length()];
		if ( arr != null){
			for ( int i = 0 ; i < arr.length() ; i++){
				JSONObject item;
				item = arr.getJSONObject(i);
				int relationIndex = Integer.parseInt(item.optJSONObject("relation").optString("index"));
				wordList[i] = new ChineseWord(item.optString("word"),getBopomofo(item.optJSONArray("bopomofo")), 
						getTone(item.optJSONArray("tone")),item.optInt("wordType"),item.optInt("length"),Relation.getRelation(relationIndex),item.optInt("startOrEnd"),item.optString("surfaceText"));				
			}
		}
		return wordList;
	}
	
	private static int[] getTone(JSONArray arr){
		int[] tone = new int[arr.length()];
		for (int i = 0 ; i < arr.length() ; i++){
			tone[i] = arr.optInt(i);
		}
		
		return tone;
	}
	private static char[] getBopomofo(JSONArray arr){
		char[] bopomofo = new char[arr.length()];
		for (int i = 0 ; i < arr.length() ; i++){
			bopomofo[i] = arr.optString(i).charAt(0);
		}
		return bopomofo;
	}
}
