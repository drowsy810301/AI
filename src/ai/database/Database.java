package ai.database;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;


import ai.exception.BopomofoException;
import ai.exception.RelationConvertException;
import ai.net.BopomofoCrawler;
import ai.word.ChineseWord;
import ai.word.Relation;

public class Database {
	private static final int LIMIT = 9999;
	private Connection c;
	private Statement stmt;
	private int wordType;
	private String path;
	public Database(String path)
	{
		this.path=path;
	}
	
	//尋找以topic為Start或End的Relation並回傳
	public ChineseWord[] select_by_topic(String topic, int wordType)
	{
		ResultSet rs=null;
		this.wordType=wordType;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:"+path);
			stmt = c.createStatement();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			rs = stmt.executeQuery("SELECT * FROM Relation WHERE Start = \"" + topic + "\" OR End = \"" + topic + "\";");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return getWordList_ChineseSource(topic,rs);
	}
	private ChineseWord[] getWordList_ChineseSource(String topic,ResultSet rs){
		ChineseWord[] tempWordList = new ChineseWord[LIMIT];
		int wordType,count = 0;
		Relation relation;
		HashMap<String, Boolean> isRecorded = new HashMap<String,Boolean>();
		String word;
		int startOrEnd;
		int cc=0;
		try{
			while (rs.next()) {cc++;
				try {
					relation = Relation.getRelation("/r/" + rs.getString("Relation"));
				
				String startWord = rs.getString("Start");
				String endWord = rs.getString("End");
				//System.out.println(startWord+ " " + endWord);
				String extraData;
				/* TODO 修正某些relation*/
				String surfaceText = rs.getString("SurfaceText");
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
					extraData = rs.getString("surfaceText");
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
						try {//System.out.println(word);
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
					// TODO Auto-generated catch block
					System.err.println(e1.getMessage());
					continue;	
				}
			} 
			
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		System.out.println("從 concpt net 上獲取了 "+count+" 個詞" +cc);
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
}
