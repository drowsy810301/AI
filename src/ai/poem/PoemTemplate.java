package ai.poem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


import ai.GeneticAlgorithm.MyRandom;
import ai.exception.MakeSentenceException;
import ai.sentence.LineComposition;
import ai.sentence.SentenceMaker;
import ai.sentence.PoemSentence;
import ai.word.ChineseWord;
import ai.word.WordPile;

public class PoemTemplate implements Comparable<PoemTemplate>{
	
	private static final boolean DEBUG = false;
	
	private int[] detailScore = new int[COUNT_FITNESS_TYPE];
	
	// ===============================================================
	// TODO 新增分數種類時也要一並更改這個數值
	public final static int COUNT_FITNESS_TYPE = 5;
	// ===============================================================
	public final static int MAX_RHYTHM_SCORE = 200;
	public final static int MAX_TONE_SCORE = 100;
	public final static int MAX_ANTITHESIS_SCORE = 200;
	public final static int MAX_DIVERSITY_SCORE = 100;
	public final static int MAX_PATTERN_SCORE = 200;
	public final static int TOTAL_SCORE = MAX_RHYTHM_SCORE + MAX_TONE_SCORE + MAX_ANTITHESIS_SCORE + MAX_DIVERSITY_SCORE + MAX_PATTERN_SCORE;
	
	// 設定個分數的最低門檻，不足的項目分數會直接變成1分，確保詩在每個項目都有一定的品質
	public final static int MIN_RHYTHM_SCORE = 50;
	public final static int MIN_TONE_SCORE = 50;
	public final static int MIN_ANTITHESIS_SCORE = 50;
	public final static int MIN_DIVERSITY_SCORE = 50;
	public final static int MIN_PATTERN_SCORE = 0;
	
	private int col, row;
	private PoemSentence[] poem;
	private int fitnessScore;
	private boolean modified;
	private static MyRandom rand = new MyRandom();
	private int maxRhythmMatch, maxToneMatch, maxAntithesisMatch;
	private static ArrayList<ArrayList<Integer>> firstSentenceTags, secondSentenceTags;
	
	/**
	 * 創建一首新的詩，每首詩可以有不同的模板
	 * <注意>因為poem中的詞語在基因演算法中會被替換，所以每個PoemTemplate都要有一個poem的實體，
	 * 不可以單純複製reference，否則修改某個PoemTemplate的poem的某個詞的時候會影響到其他人
	 * 
	 * @param row
	 * @param col
	 * @param wordComposition
	 * @param poem 
	 */
	public PoemTemplate(int row,int col, PoemSentence[] poem){
		this.row = row;
		this.col = col;
		loadSentencePattern();
		/*錯誤的複製 : this.poem = poem;*/
		this.poem = new PoemSentence[row];
		for ( int i = 0 ; i < row ; i++){
			this.poem[i] = new PoemSentence(poem[i].getSentenceType(), poem[i].getWords(),poem[i].getLineComposition(),poem[i].getSentenceTag());
		}
		
		maxRhythmMatch = row/2;
		if (col == 5){
			maxToneMatch = 3*row;
		}
		else{
			maxToneMatch = 4*row;
		}
		
		maxAntithesisMatch = col*row/2;
		modified = true;
	}
	
	 public static PoemTemplate getCopy(PoemTemplate sourcePoem){
		return new PoemTemplate(sourcePoem.row, sourcePoem.col, sourcePoem.poem);
	}
	
	/**
     * 隨機產生每一聯使用相同斷詞方式的詩，會自動避免產生2句一模一樣的句子
     * @param row 詩有幾句
     * @param col 每句幾個字
     * @return PoemTemplate的object
	 * @throws Exception 
	 * @throws MakeSentenceException 
     */
    public static PoemTemplate getRandomPoem(int row,int col,WordPile wordPile,SentenceMaker maker) throws Exception{
    	HashMap<String, Boolean> repeatSentence = new HashMap<String, Boolean>();
    	PoemSentence[] poem = new PoemSentence[row];
    	int[] repeatSentenceTag = new int[100];
    	
    	for (int i = 0 ; i < row ; i+=2){
    		ArrayList<int[]> compositionList = new ArrayList<>(Arrays.asList(LineComposition.FIVE_LETTER_COMPOSITION));
    		boolean isDone = false;
    		while(!compositionList.isEmpty()){
    			int[] lineComposition = (int[]) rand.getRandomObject(compositionList);
    			isDone = tryToMakeASentence(maker,repeatSentenceTag,repeatSentence,poem,i,lineComposition);
    			if (!isDone)
    				continue;
    			isDone = tryToMakeASentence(maker,repeatSentenceTag,repeatSentence,poem,i+1,lineComposition);
        		if (isDone)
        			break;
    		}
    		if (!isDone){
    			throw new Exception("error : 造詩失敗，句型模板太少");
    		}
    	}
    	
    	return new PoemTemplate(row, col, poem);
    }
    
    private static boolean tryToMakeASentence(SentenceMaker maker,int[] repeatSentenceTag, HashMap<String, Boolean> repeatSentence,PoemSentence[] poem, int rowIndex, int[] lineComposition){
    	int maxTry = 100;
    	while (maxTry > 0){
			try {
				poem[rowIndex] = maker.makeSentence(lineComposition);
				if (repeatSentence.containsKey(poem[rowIndex].encode()) || repeatSentenceTag[poem[rowIndex].getSentenceTag()] >= 2){
					maxTry -= 1;
				}
				else{
					repeatSentence.put(poem[rowIndex].encode(),true);
					repeatSentenceTag[poem[rowIndex].getSentenceTag()] += 1;
					return true;
				}
			} catch (MakeSentenceException e) {
				return false;
			}
		}
    	return false;
    }
    
	public PoemSentence[] getPoem() {
		modified = true;
		return poem;
	}
	
	public int getRow(){
		return this.row;
	}
	
	public int getCol(){
		return this.col;
	}
	
	/**
	 * 當呼叫 getPoem() 系統會認為使用者更改過詩的內容，因此要重新計算 "適應分數"
	 * 否則就直接回傳上次計算完的結果
	 * @return 適應分數
	 */
	public int getFitnessScore() {
		if (modified){
			modified = false;
			fitnessFunction();
		}
		return fitnessScore;
	}
	
	private void fitnessFunction(){

		detailScore[0] = getRhythmScore();
		detailScore[1] = getToneScore();
		detailScore[2] = getAntithesisScore();
		detailScore[3] = getDiversityScore();
		detailScore[4] = getPatternScore();
		fitnessScore = 0;
		for (int score : detailScore)
			fitnessScore += score;

		if (DEBUG) System.out.println(this.printScore());
	}
	
	/**
	 * 確保同一首詩不會有相同(或類似)的2句
	 * 若一個詞出現2次以上 => 0 分
	 * @return
	 */
	private int getDiversityScore(){
		final int MAX_WORD_REPEATED_TIME = 2;
		HashMap<String, Integer> repeatedWord = new HashMap<String, Integer>();
		HashMap<String, Boolean> repeatedSentence = new HashMap<String,Boolean>();
		
		for (PoemSentence sentence : poem){
			if (repeatedSentence.containsKey(sentence.encode())){
				return 0 ;
			}
			else{
				repeatedSentence.put(sentence.encode(), true);
			}
		}
		
		for (int i = 0 ; i < row ; i++){
			for (int j = 0 ; j < poem[i].getLength() ; j++){
				String word = poem[i].getWords()[j].getWord();
				if (repeatedWord.containsKey(word)){
					int times = repeatedWord.get(word);
					if (times >= MAX_WORD_REPEATED_TIME){
						return 0;
					}
					else{
						repeatedWord.put(word, times + 1);
					}
				}
				else{
					repeatedWord.put(word, 1);
				}
			}
		}
		
		return MAX_DIVERSITY_SCORE;
	}
	/**
	 * 計算相鄰兩句詞性相同的詞的字數有幾個
	 * @return
	 */
	private int getAntithesisScore(){
		int countAntithesis = 0;
		for ( int i = 0 ; i < row ; i += 2){
			for ( int j = 0 ; j < poem[i].getLength() ; j++){
				if ( (poem[i].getWords()[j].getWordType() & poem[i+1].getWords()[j].getWordType() )> 0){
					countAntithesis += poem[i].getWords()[j].getLength();
					if (DEBUG) System.out.println(poem[i].getWords()[j].getWord()+" = "+poem[i+1].getWords()[j].getWord());
				}
			}
		}
		if (DEBUG) System.out.printf(">>對偶的字共有  %d / %d 個\n",countAntithesis,maxAntithesisMatch);
		int score = countAntithesis*MAX_ANTITHESIS_SCORE/maxAntithesisMatch;
		if (score < MIN_ANTITHESIS_SCORE)
			return 0;
		else
			return score;
	}
	
	/**
	 * 分別用平起式 / 仄起式 模板去檢驗符合的字數，再取較高者
	 * 句尾的字如果押韻要用平聲，不押韻要用仄聲
	 * @return
	 */
	private int getToneScore(){
		
		int countMatchTone;
		int countMatchRhythmTone = 0;
		
		if (DEBUG) System.out.println("===平仄===");
		//分別用平起式 / 仄起式 模板去檢驗符合的字數，再取較高者
		countMatchTone = Math.max(getMatchToneCount(0), getMatchToneCount(2));
		
		/*處理韻腳的平仄*/
		if (DEBUG) System.out.println("===韻腳平仄===");
		if (poem[0].getWords()[poem[0].getLength()-1].getRhythm() == poem[1].getWords()[poem[1].getLength()-1].getRhythm()){
			if (getToneAt(1*col) == 0){
				countMatchRhythmTone += 1; /*首句押韻用平聲*/
				if (DEBUG) System.out.printf("第1句 : 平 (%c,%c)\n",getCharAt(col),getCharAt(2*col));
			}
		}
		else{
			if (getToneAt(1*col) == 1){
				countMatchRhythmTone += 1; /*首句不押韻用仄聲*/
				if (DEBUG) System.out.println("第1句 : 仄");
			}
		}
		if (getToneAt(2*col) == 0){
			countMatchRhythmTone += 1;
			if (DEBUG) System.out.println("第2句 : 平");
		}
		for (int i = 4 ; i <= row ; i += 2){
			if ( getToneAt((i-1)*col) == 1){
				countMatchRhythmTone += 1;
				if (DEBUG) System.out.println("第"+(i-1)+"句 : 仄");
			}
			if ( getToneAt(i*col) == 0){
				countMatchRhythmTone += 1;
				if (DEBUG) System.out.println("第"+i+"句 : 平");
			}
		}
		if (DEBUG)System.out.printf(">>符合平仄的字有 (%d + %d(韻腳相關)) / %d 個\n",countMatchTone,countMatchRhythmTone,maxToneMatch);
		int score =  (countMatchTone+countMatchRhythmTone)*MAX_TONE_SCORE/maxToneMatch;
		if (score < MIN_TONE_SCORE)
			return 0;
		else
			return score;
	}
	/**
	 * 套用 平起式 或 仄起式 的模板來計算符合平仄的字數
	 * @param type 0 : 平起式, 2 : 仄起式
	 * @return 符合平仄的字數
	 */
	private int getMatchToneCount(int type){
		final int[][] standardTone = new int[][]{{0,1,0},{1,0,1},{1,0,1},{0,1,0}};
		int countMatchTone = 0;
		
		if (type == 0){
			if (DEBUG) System.out.println("平起式");
		}
		else if (type == 2){
			if (DEBUG)System.out.println("仄起式");
		}
		else{
			System.err.println("error : invalid tone type");
		}
		
		for ( int i = 0 ; i < row ; i ++){
			for ( int j = 2, k = 0 ; j <= col ; j+=2, k++){
				int charIndex = i*col+j;
				if (DEBUG) System.out.printf("%c(%d)",getCharAt(charIndex),getToneAt(charIndex));
				if (getToneAt(charIndex) == standardTone[type][k]){
					countMatchTone += 1;
					if (DEBUG) System.out.print("O ");
				}
				else{
					if (DEBUG) System.out.print("X ");
				}
			}
			if (DEBUG)  System.out.println();
			type = (type+1)%4;
		}
		return countMatchTone;
	}
	
	/**
	 * 取得整首詩中的某個字
	 * @param index 從"1"開始算
	 * @return 
	 */
	private char getCharAt(int index){
		if ( index > row*col){
			System.err.println("error : index out of bound");
			System.exit(1);
		}
		if (index <=0){
			System.err.println("error : Index 從 1 開始算");
			System.exit(1);
		}
		index -= 1;
		int atRow = index/col;
		index = index-atRow*col+1;
		int cumulativeSum = 0;
		for ( ChineseWord word : poem[atRow].getWords()){
			int len = word.getLength();
			if (cumulativeSum + len >= index){
				return word.getCharAt(index-cumulativeSum-1);
			}
			cumulativeSum += len;
		}
		return '?';
	}
	/**
	 * 取得整首詩中某個字的平仄
	 * @param index 從 "1" 開始算
	 * @return 0:平, 1:仄
	 */
	private int getToneAt(int index){
		if ( index > row*col){
			System.err.println("error : index out of bound");
			System.exit(1);
		}
		if (index <=0){
			System.err.println("error : Index 從 1 開始算");
			System.exit(1);
		}
		index -= 1;
		int atRow = index/col;
		index = index-atRow*col+1;
		int cumulativeSum = 0;
		for ( ChineseWord word : poem[atRow].getWords()){
			int len = word.getLength();
			if (cumulativeSum + len >= index){
				return word.getToneAt(index-cumulativeSum-1);
			}
			cumulativeSum += len;
		}
		return -1;
	}
	/**
	 * 統計最多的韻腳，看它佔了4分之幾
	 * @return
	 */
	private int getRhythmScore(){
		HashMap<Character,Integer> recordRhythm = new HashMap<Character,Integer>();
		int maxCountSameRhytm = 0;
		char mostRhythm='?';
		for (int  i = 1 ; i < row ; i += 2){
			char rhythm = poem[i].getWords()[poem[i].getLength()-1].getRhythm();
			int temp;
			if (recordRhythm.containsKey(rhythm)){
				temp = recordRhythm.get(rhythm)+1;
			}
			else{
				temp = 1;
			}
			recordRhythm.put(rhythm,temp);
			if ( maxCountSameRhytm < temp){
				maxCountSameRhytm = temp;
				mostRhythm = rhythm;
			}
		}
		for (int i = 1 ; i < row ; i+= 2){
			if (DEBUG) System.out.print(" ("+poem[i].getWords()[poem[i].getLength()-1].getWord()+","+poem[i].getWords()[poem[i].getLength()-1].getRhythm()+")");
		}
		if (DEBUG) System.out.println();
		if (DEBUG) System.out.printf(">>最多的韻腳是 \"%c\"，共有 %d / %d 個\n",mostRhythm,maxCountSameRhytm,maxRhythmMatch);
		int score = maxCountSameRhytm*MAX_RHYTHM_SCORE/maxRhythmMatch;
		if (score < MIN_RHYTHM_SCORE)
			return 0;
		else
			return score;
	}

	private int getPatternScore(){
		int countPattern = 0;
		int[] recordTag = new int[100];
		HashMap<Integer, Boolean> recordPattern = new HashMap<>();
		for (int i = 0 ; i < row ; i+=2){
			int tag1 =  poem[i].getSentenceTag();
			int tag2 =  poem[i+1].getSentenceTag();
			
			if (matchSentencePattern(tag1, tag2)){
				int patternEncode = tag1 * 1000 + tag2;
				if (!recordPattern.containsKey(patternEncode)){
					if (patternEncode == 16008 || patternEncode == 8016){
							recordPattern.put(16008, true);
							recordPattern.put(8016, true);
					}
					else {
						recordPattern.put(patternEncode, true);
					}	
					countPattern += 1;
				}
			}
			
			if (recordTag[tag1] >= 2){
				return 0;
			}
			else{
				recordTag[tag1] += 1;
			}
			
			if (recordTag[tag2] >= 2){
				return 0;
			}
			else{
				recordTag[tag2] += 1;
			}
		}
		
		return MAX_PATTERN_SCORE*(countPattern)/4;
	}
	
	public String printScore(){
		this.getFitnessScore();
		return String.format("押韻: %d/%d, 平仄: %d/%d, 對仗:%d/%d\n多樣性:%d/%d, 句型:%d/%d",detailScore[0],MAX_RHYTHM_SCORE,detailScore[1],MAX_TONE_SCORE,detailScore[2],MAX_ANTITHESIS_SCORE,detailScore[3],MAX_DIVERSITY_SCORE,detailScore[4],MAX_PATTERN_SCORE);
	}
	
	public int[] getDetailScore(){
		getFitnessScore();
		return detailScore;

	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0 ; i < row ; i+=2){
			sb.append(poem[i].toString()+"("+poem[i].getSentenceTag()+")"+"，"+poem[i+1].toString()+"("+poem[i+1].getSentenceTag()+")"+"\n");
		}	
		return sb.toString();
	}

	@Override
	public int compareTo(PoemTemplate other) {
		int score1 = this.getFitnessScore();
		int score2 = other.getFitnessScore();
		if (score1 > score2)
			return -1;
		else if (score1 < score2)
			return 1;
		else {
			return 0;
		}
	}
	
	private void loadSentencePattern(){
		final String fileName = "sentencePattern.data";
		
		if (firstSentenceTags != null && secondSentenceTags != null)
			return;
		
		firstSentenceTags = new ArrayList<>();
		secondSentenceTags= new ArrayList<>();
		ArrayList<Integer> tagList;
		try {
			BufferedReader bufRead = new BufferedReader(new FileReader(fileName));
			while (true) {
				String line = bufRead.readLine();
				if (line == null)
					break;
				if (line.length() == 0 || line.charAt(0) == '#')
					continue;
				String firstTag = line.split(" +")[0];
				String secondTag = line.split(" +")[1];
				tagList = new ArrayList<>();
				for (String tag : firstTag.split("/")){
					tagList.add(Integer.valueOf(tag));
				}
				firstSentenceTags.add(tagList);
				tagList = new ArrayList<>();
				for (String tag : secondTag.split("/")){
					tagList.add(Integer.valueOf(tag));
				}
				secondSentenceTags.add(tagList);
			}
			bufRead.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public boolean matchSentencePattern(int tag1,int tag2){
		for (int i = 0 ; i < firstSentenceTags.size() ; i++){
			if (firstSentenceTags.get(i).contains(tag1) && secondSentenceTags.get(i).contains(tag2))
				return true;
		}
		return false;
	}
}
