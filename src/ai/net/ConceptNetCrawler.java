package ai.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

import ai.exception.BopomofoException;
import ai.exception.RelationConvertException;
import ai.userInterface.MyUtility;
import ai.word.ChineseWord;
import ai.word.Relation;

public class ConceptNetCrawler {
	
	private static final int LIMIT = 9999;
	private static final int MAX_TRANSLATION = 1000;
	public final static int START = 0, END = 1, NODES = 2;
	private String topic;
	private int wordType;
	private JProgressBar loadingBar = null;
	private int progress;
	
	public ConceptNetCrawler(String topic, int wordType) {
		this.topic = topic;
		this.wordType = wordType;
	}
	
	public ChineseWord[] getWordList_ChineseSource(){
		ArrayList<ChineseWord> relativeWord = new ArrayList<ChineseWord>();
		relativeWord.addAll(Arrays.asList(getWordList_ChineseSource(topic,NODES)));
		
		/*try {
			String url = new String("http://conceptnet5.media.mit.edu/data/5.2/search?limit="+LIMIT+"&rel=/r/IsA&nodes=/c/zh_TW/"+URLEncoder.encode(topic,"UTF-8"));
		
			System.out.println(url);
			JSONObject obj = readJsonFromURL(url);
			
			
			JSONArray arr = obj.optJSONArray("edges");
			for (int i = 0 ; i < arr.length() ; i++){
				String[] tmp = arr.optJSONObject(i).optString("end").split("/");
				ChineseWord[] wordPile = getWordList_ChineseSource(tmp[tmp.length-1],START);
				relativeWord.addAll(Arrays.asList(wordPile));
			}
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}*/
		
		ChineseWord[] wordList = new ChineseWord[relativeWord.size()];
		return  relativeWord.toArray(wordList);
	}
	
	/**
	 * 	利用concept net的API尋找和某個中文主題相關的詞，並且利用rel來推測詞的詞性
	 * 	@param input : 主題
	 * 	@param position : START(0), END(1), NODES(other)
	 * 	@return 相關詞的陣列(ChineseWord[])
	 * 	
	 * 	並非所有從concept net上獲得的詞語都可以轉換成詞庫
	 * 	有的詞會找不到對應的注音，有的詞的字多於3個 或是 某個詞無法定義其詞性，則該筆資料會被忽略
	 * 	程式會用Hash的放式避免同一個詞語重複被收入詞庫
	 * 
	 * 	處理json的library要另外去下載，請參考 http://www.ewdna.com/2008/10/jsonjar.html
	 */
	private ChineseWord[] getWordList_ChineseSource(String input,int position) {
		String pos;
		if (position == START)
			pos = "start";
		else if(position == END)
			pos = "end";
		else {
			pos = "nodes";
		}
		try {
			String url = new String("http://conceptnet5.media.mit.edu/data/5.2/search?limit="+LIMIT+"&"+pos+"=/c/zh_TW/"+URLEncoder.encode(input,"UTF-8"));
			System.out.println(url);
			JSONObject obj = readJsonFromURL(url);
			return getWordList_ChineseSource(input,obj);
		} catch (UnsupportedEncodingException e) {
			System.err.println('\"'+input+"\" 無法用UTF-8編碼");
			e.printStackTrace();
			return new ChineseWord[0];
		}
	}
	public ChineseWord[] getWordList_ChineseSource(String topic,JSONObject obj){
		ChineseWord[] tempWordList = new ChineseWord[LIMIT];
		int wordType,count = 0;
		Relation relation;
		HashMap<String, Boolean> isRecorded = new HashMap<String,Boolean>();
		String word;
		int startOrEnd;
				
		try {
			JSONArray array = obj.getJSONArray("edges");
			if (loadingBar != null){
				progress = 0;
				SwingUtilities.invokeLater(new Runnable() {
				
					@Override
					public void run() {
						loadingBar.setValue(loadingBar.getMinimum());
						loadingBar.setStringPainted(true);
						loadingBar.setIndeterminate(false);
					}
				});
			}
			for (int i=0; i<array.length(); i++){
				int nowProgress = (i+1)*100/array.length();
				if (loadingBar != null && nowProgress > progress){
					progress = nowProgress;
					SwingUtilities.invokeLater(new MyUtility.ProgressBarSetting(loadingBar,progress));
				}
				JSONObject jsonObj  = ((JSONObject)array.getJSONObject(i));
				try {
					relation = Relation.getRelation(jsonObj.getString("rel"));
					String startWord = jsonObj.getString("start").split("/")[3];
					String endWord = jsonObj.getString("end").split("/")[3];
					String extraData;
					/* TODO 修正某些relation*/
					String surfaceText = jsonObj.getString("surfaceText");
					relation = fixRelation(relation, surfaceText);
					
					if (startWord.equals(topic)){
						word = endWord;
						startOrEnd = Relation.END;
					}
					else if (endWord.equals(topic)){
						word = startWord;
						startOrEnd = Relation.START;
					}
					else{
						//System.err.println("warning : ConceptNet gives a json object without corresponding start wrod / end word ( "+startWord+" , "+endWord+" )");
						continue;
					}
					
					/* TODO 把特定的介係詞加入word尾端*/
					final String[] detailLocation = {"下","外","裡"};
					if(relation == Relation.AtLocation){
						extraData = jsonObj.getString("surfaceText");
						extraData = extraData.substring(extraData.length()-2, extraData.length()-1);
						for (String temp : detailLocation){
							if (extraData.equals(temp) && !word.substring(word.length()-1, word.length()).equals(temp)){
								word = word+temp;
							}
						}
					}
					
					if (word.length() <= 3){
						if (!isRecorded.containsKey(word)){
								isRecorded.put(word, true);
							wordType = Relation.getWordType(relation,startOrEnd);
							try {
								tempWordList[count] =  new ChineseWord(word, BopomofoCrawler.getBopomofo(word), wordType, relation, startOrEnd,surfaceText);
								count += 1;
							} catch (BopomofoException e) {
									System.err.println(e.getMessage());
								continue;	
							}
						}
						else{
							//System.out.println(word + " 已經出現過");
						}
					}
					
				} catch (RelationConvertException e1) {
					System.err.println(e1.getMessage());
					continue;
				}
				
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		System.out.println("從 concpt net 上獲取了 "+count+" 個詞");
		return Arrays.copyOf(tempWordList, count);
	}
	
	private Relation fixRelation(Relation relation,String surfaceText) {
		//NotDesire 較特殊
		final String[] negativeDesire = {"不想要","痛恨","懼怕"};
		
		final String[] detailCauses = {"所以","帶來","引起","讓你"};
		final String[] detailCapableOf = {"會"};
		final String[] detailHasSubevent = {"代表","可以"};
		final String[] detailMotivatedByGoal = {"因為你","是為了","時候會"};
		final String[] detailMadeOf = {"組成"};
		String[] keywords;
		HashMap<Relation, String[]> detailData = new HashMap<Relation, String[]>();
		
		detailData.put(Relation.Causes, detailCauses);
		detailData.put(Relation.CapableOf, detailCapableOf);
		detailData.put(Relation.HasSubevent, detailHasSubevent);
		detailData.put(Relation.MotivatedByGoal, detailMotivatedByGoal);
		detailData.put(Relation.MadeOf,detailMadeOf);
		
		surfaceText = surfaceText.replaceAll("\\[\\[.*?\\]\\]", "");
		switch (relation){
		case Desires:
			for (String temp : negativeDesire){
				if (surfaceText.indexOf(temp) != -1){
					return Relation.NotDesires;
				}
			}
			return relation;
		
		case Causes:
		case CapableOf :
		case HasSubevent :
		case MadeOf :
			keywords = detailData.get(relation);
			for (int i = 0 ; i < keywords.length ; i++){
				if (surfaceText.indexOf(keywords[i]) != -1){
					try {
						return Relation.getRelation(relation.toString()+i);
					} catch (RelationConvertException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			}
			return relation;
		case MotivatedByGoal :
			keywords = detailData.get(relation);
			for (int i = 0 ; i < keywords.length ; i++){
				if (surfaceText.indexOf(keywords[i]) != -1){
					if (!keywords[i].equals("時候會") || keywords[i].equals("時候會") && (wordType&ChineseWord.VERB) > 0){
						try {
							return Relation.getRelation(relation.toString()+i);
						} catch (RelationConvertException e) {
							e.printStackTrace();
							System.exit(1);
						}
					}
				}
			}
		default:
			return relation;
		}
	}
	
	/**
	 * 先把主題翻成英文再去concept net上找相關詞，之後再把相關詞翻譯成中文
	 * @return 相關詞的陣列(ChineseWord[])
	 * 
	 * 	並非所有從concept net上獲得的詞語都可以轉換成詞庫
	 * 	有的詞會找不到對應的注音，有的詞的字多於3個 或是 某個詞無法定義其詞性，則該筆資料會被忽略
	 * 	程式會用Hash的放式避免同一個詞語重複被收入詞庫
	 * 
	 * 翻譯是利用 Microsoft translator API 進行的，請參考 : https://msdn.microsoft.com/en-us/library/hh454949.aspx
	 * Java 的翻譯接口 API 是由第三方撰寫，請參考 : https://code.google.com/p/microsoft-translator-java-api/
	 *	
	 *	[重要] 如果相關詞由多個英文單字組成，單字之間會以底線區隔，進行翻譯之前要把底線換成空白
	 *	[重要] 中文翻成英文時第一個字母會是大寫，要把它轉換成小寫才可以順利在concept net上取得資料
	 */
	public ChineseWord[] getWordList_EnlishSource(){
		String englishTopic;
		HashMap<String, Boolean> isRecorded = new HashMap<String,Boolean>();
		Translate.setClientId(MicrosoftTranslatorKey.ID);
		Translate.setClientSecret(MicrosoftTranslatorKey.SECRET);
		try {
			englishTopic = Translate.execute(topic, Language.CHINESE_TRADITIONAL, Language.ENGLISH).toLowerCase();
		} catch (Exception e1) {
			englishTopic = "";
			e1.printStackTrace();
		}
		
		String url = new String("http://conceptnet5.media.mit.edu/data/5.2/search?limit="+LIMIT+"&nodes=/c/en/"+englishTopic);
		System.out.println(url);
		JSONObject obj = readJsonFromURL(url);
		
		try {
			JSONArray array = obj.getJSONArray("edges");
			String[] englishInput = new String[LIMIT];
			SemiChineseWord[] semiWordData = new SemiChineseWord[LIMIT];
			int countTranlation = 0;
			
			for (int i=0; i<array.length(); i++){
				JSONObject jsonObj  = ((JSONObject)array.getJSONObject(i));
				
				try {
					String word;
					String surfaceText = jsonObj.getString("surfaceText");
					int startOrEnd;
					Relation relation = Relation.getRelation(jsonObj.getString("rel"));
					int wordType;
					String startWord = jsonObj.get("start").toString().substring(6);
					String endWord = jsonObj.getString("end").toString().substring(6);
					
					startWord = startWord.replace('_', ' ');
					endWord = endWord.replace('_', ' ');
					if (startWord.equals(englishTopic)){
						word = endWord;
						startOrEnd = Relation.END;
					}
					else if (endWord.equals(englishTopic)){
						word = startWord;
						startOrEnd = Relation.START;
					}
					else{
						//System.err.println("warning : ConceptNet gives a json object without corresponding start wrod / end word ( "+startWord+" , "+endWord+" )");
						continue;
					}
					wordType = Relation.getWordType(relation,startOrEnd);
					
					if (word.split(" ").length <= 3){
						if (!isRecorded.containsKey(word)){
							isRecorded.put(word, true);
							englishInput[countTranlation] = word;
							semiWordData[countTranlation] = new SemiChineseWord(wordType, relation, startOrEnd,surfaceText);
							countTranlation += 1;
						}
						else{
							//System.out.println(word + " 已經出現過");
						}
					}	
				} catch (RelationConvertException e) {
					System.err.println(e.getMessage());
					//e.printStackTrace();
					continue;
				}
					
			}
			
			/*限制一次翻譯詞語數上限，避免太快把每個月的翻譯配額用完*/
			if (countTranlation > MAX_TRANSLATION)
				countTranlation = MAX_TRANSLATION;
			
			String[] chineseOutput = new String[countTranlation],tmp;
			/*把大量的翻譯分成小份的分批執行，不然會有Error*/
			final int slice = 200;
			try {
				int offset = countTranlation % slice;
				if (offset > 0){
					tmp = Translate.execute(Arrays.copyOf(englishInput, offset),Language.ENGLISH,Language.CHINESE_TRADITIONAL);
					System.arraycopy(tmp, 0, chineseOutput, 0, offset);
				}
				for (int i = offset ; i < countTranlation ; i += slice){
					tmp = Translate.execute(Arrays.copyOfRange(englishInput, i, i+slice), Language.ENGLISH,Language.CHINESE_TRADITIONAL);
					System.arraycopy(tmp, 0, chineseOutput, i, slice);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			System.out.println("共翻譯了 "+chineseOutput.length+" 個詞");
			ChineseWord[] tempWordList = new ChineseWord[LIMIT]; 
			int count = 0;
			for (int i = 0 ; i < countTranlation && i < chineseOutput.length ; i++){
				if ( englishInput[i].equals(chineseOutput[i]) || chineseOutput[i].length() > 3)
					continue;
				String word = chineseOutput[i];
				if (!isRecorded.containsKey(word)){
					try {
						SemiChineseWord data = semiWordData[i];
						tempWordList[count] =  new ChineseWord(word, BopomofoCrawler.getBopomofo(word), data.wordType, data.relation, data.startOrEnd, data.surfaceText);
						count += 1;
					} catch (BopomofoException e) {
						System.err.println(e.getMessage());
						continue;
					}
				}
				else{
					//System.out.println(word + " 已經出現過");
				}
			}
			
			return Arrays.copyOf(tempWordList, count);
		} catch (JSONException e) {
			e.printStackTrace();
			return new ChineseWord[0];
		}
	}
	
	/**
	 * 
	 * 	@param  url: json檔案的url (String)
	 * 	@return JSONObject
	 */
	public static JSONObject readJsonFromURL(String url){
		JSONObject json = null;
		InputStream is = null;
		try {
			int c;
			is = new URL(url).openStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,Charset.forName("UTF-8")));
			StringBuilder strBuilder = new StringBuilder();
			while( (c = reader.read()) != -1){
				strBuilder.append((char)c);
			}
			String jsonStr = strBuilder.toString();
			try {
				json = new JSONObject(jsonStr);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (is != null){
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return json;
	}
	
	public void  setLoadingBar(JProgressBar loadingBar) {
		this.loadingBar = loadingBar;
	}
	
	/**
	 * 儲存除了 word 和 bopomofo 以外其他 ChineseWord的必要元素
	 */
	private class SemiChineseWord{
		int wordType;
		Relation relation;
		int startOrEnd;
		String surfaceText;
		
		public SemiChineseWord(int wordType, Relation relation, int startOrEnd, String surfaceText) {
			this.wordType = wordType;
			this.relation = relation;
			this.startOrEnd = startOrEnd;
			this.surfaceText = surfaceText;
		}
	}
}
