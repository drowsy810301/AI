package ai.database;

import java.util.ArrayList;
import java.util.Arrays;

import ai.GeneticAlgorithm.GeneticAlgorithm;
import ai.sentence.SentenceMaker;
import ai.word.ChineseWord;
import ai.word.WordPile;

public class DBtest {
	public static void main(String[] argv){
		String topic=new String("電腦");
		ArrayList<ChineseWord> wordArrayList;
		Database db = new Database("ConceptNet.db");
		ChineseWord[] wordList = db.select_by_topic(topic, 1);
		wordArrayList = new ArrayList<>(Arrays.asList(wordList));
		WordPile wordPile = new WordPile(topic,1);
		wordPile.addWords(wordArrayList);
		SentenceMaker maker = new SentenceMaker(wordPile);
		GeneticAlgorithm ga = new GeneticAlgorithm(8, 7, wordPile, maker);
		try {
			ga.evolve();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("finish");
	}
}
