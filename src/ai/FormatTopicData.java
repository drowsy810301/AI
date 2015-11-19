package ai;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JOptionPane;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ai.net.ConceptNetCrawler;
import ai.word.ChineseWord;

public class FormatTopicData {
	
	public static void main(String[] argv){
		String[] topic = {"電腦","哭","狗","食物","朋友","手","錢","跑步","老師","水"};
		int wordType[] ={ChineseWord.NOUN,ChineseWord.VERB,ChineseWord.NOUN,ChineseWord.NOUN,ChineseWord.NOUN,ChineseWord.NOUN,ChineseWord.NOUN,ChineseWord.VERB,ChineseWord.NOUN,ChineseWord.NOUN};
		String file[] ={"computer3","cry3","dog3","food3","friend3","hand3","money3","run3","teacher3","water3"};
		JSONObject obj = null;
		
		// 調整 for loop 的迴圈範圍可以用不同的主題作詩
		// 演化趨勢圖片會儲存在 pic/ 資料夾下
		for (int k = 0 ; k < 10 ; k++){
			System.out.println(topic[k]);
			try {
				obj = new JSONObject(MainClass.ReadFile("topic/"+file[k]+".json"));
			} catch (JSONException e2) {
				e2.printStackTrace();
				System.exit(1);
			}
			
		
			ConceptNetCrawler wordSource= new ConceptNetCrawler(topic[k],wordType[k]);
			ChineseWord[] wordList = wordSource.getWordList_ChineseSource(topic[k],obj);
			saveToJsonFile(topic[k], wordType[k], wordList, file[k]+".json");
			
		}
	}
	
	private static void saveToJsonFile(String topic, int topicWordType, ChineseWord[] wordPile,String fileName){
		JSONObject root = new JSONObject();
		
		try {
			root.put("topic", topic);
			root.put("topicWordType", topicWordType);
			JSONArray wordArray = new JSONArray();
			for (ChineseWord word : wordPile){
				wordArray.put(new JSONObject(word));
			}
			root.put("wordList", wordArray);
			File saveFile = new File(fileName);
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(saveFile));
			out.write(root.toString().getBytes());
			out.close();
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		JOptionPane.showMessageDialog(null, topic + "存檔失敗","錯誤",JOptionPane.ERROR_MESSAGE);
	}
}
