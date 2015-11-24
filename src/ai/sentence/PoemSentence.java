package ai.sentence;

import ai.word.ChineseWord;
import ai.word.Relation;

public class PoemSentence {
	
	private int sentenceType;
	private int sentenceTag;
	private int[] lineComposition;
	private ChineseWord[] words;
	private int lineCompositionEncode;
	
	public PoemSentence(int sentenceType,ChineseWord[] sentence, int[] lineComposition,int sentenceTag){
		this.lineComposition = lineComposition;
		lineCompositionEncode = 0;
		for (int element : lineComposition){
			lineCompositionEncode = lineCompositionEncode*10 + element;
		}
		this.sentenceTag = sentenceTag;
		this.sentenceType = sentenceType;
		/*不可以用this.words = sentence 這種方式複製*/
		this.words = new ChineseWord[sentence.length];
		for (int i = 0 ; i < sentence.length ; i++){
			words[i] = sentence[i];
		}	
	}
	
	/**
	 * 
	 * @return 斷句方式的編碼，用於基因演算的的交配階段，確保相鄰兩句的斷句方式相同
	 */
	public int getLineCompositionEncode() {
		return this.lineCompositionEncode;
	}
	
	public int[] getLineComposition(){
		return this.lineComposition;
	}
	
	public ChineseWord[] getWords() {
		return words;
	}
	
	public int getSentenceType() {
		return this.sentenceType;
	}
	
	public int getLength() {
		return words.length;
	}
	
	public int getSentenceTag() {
		return sentenceTag;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (ChineseWord word : words){
			sb.append(word.getWord()+" ");
		}
		return sb.toString();
	}
	/**
	 * 
	 * @return 將句子中除了 padding 跟 TOPIC 之外的詞連起來，用於判斷類似的句子是否重複出現於同一首詩中
	 */
	public String encode() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0 ; i < words.length ; i++){
			if(words[i].getRelation() == Relation.PADDING || words[i].getRelation() == Relation.TOPIC)
				continue;
			sb.append(words[i].getWord());
		}
		return sb.toString();
	}
}
