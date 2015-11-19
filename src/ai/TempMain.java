package ai;

import org.json.JSONException;
import org.json.JSONObject;

import ai.GeneticAlgorithm.GeneticAlgorithm;
import ai.net.ConceptNetCrawler;
import ai.sentence.SentenceMaker;
import ai.word.ChineseWord;
import ai.word.WordPile;

public class TempMain {
	
	public static void main(String[] argv){
		String[] topic = {"電腦","哭","狗","食物","朋友","手","錢","跑步","老師","水"};
		String[][] topicWord = {{},
								{"哭泣"},
								{"小狗"},
								{},
								{},
								{"雙手"},
								{"金錢"},
								{"跑"},
								{},
								{"清水","自來水"}};
		int wordType[] ={ChineseWord.NOUN,ChineseWord.VERB,ChineseWord.NOUN,ChineseWord.NOUN,ChineseWord.NOUN,ChineseWord.NOUN,ChineseWord.NOUN,ChineseWord.VERB,ChineseWord.NOUN,ChineseWord.NOUN};
		String file[] ={"computer3","cry3","dog3","food3","friend3","hand3","money3","run3","teacher3","water3"};
		JSONObject obj = null;
		
		// 調整 for loop 的迴圈範圍可以用不同的主題作詩
		// 演化趨勢圖片會儲存在 pic/ 資料夾下
		for (int k = 0 ; k < 1 ; k++){
			try {
				obj = new JSONObject(MainClass.ReadFile("topic/"+file[k]+".json"));
			} catch (JSONException e2) {
				e2.printStackTrace();
				System.exit(1);
			}
			WordPile wordPile = new WordPile(topic[k], wordType[k]);
			for (String str : topicWord[k])
				wordPile.addTopicWord(str);
			ConceptNetCrawler wordSource= new ConceptNetCrawler(topic[k],wordType[k]);
			wordPile.addWords(wordSource.getWordList_ChineseSource(topic[k],obj));
			SentenceMaker maker = new SentenceMaker(wordPile);
			System.out.println("主題 : "+topic[k]);
			wordPile.printWordPileStatistic();
			//wordPile.printBopomofo();
			maker.printAvailableSentenceStatistic();
			try {
				new GeneticAlgorithm(8, 5, wordPile, maker).evolve();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
