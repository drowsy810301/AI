package Concept;

import java.util.ArrayList;
import java.net.MalformedURLException;
import java.net.URL;

public class Main {
	
	public static String topic;
	public static void main(String[] args) {
		
		URL url = null;
		String str = new String();
		String concept = new String("人");
		//topic = input.next();
		//System.out.println(topic);
		ArrayList<Double> start = new ArrayList<Double>();
		
		try {
			url = new URL("http://conceptnet5.media.mit.edu/data/5.3/c/zh/" + Crawler.encode(concept));
			//url = new URL("http://www.chinese-artists.net/poem/?query=" + Crawler.encode(concept));
		} catch (MalformedURLException e) {			
			e.printStackTrace();
		}
		str = Crawler.crawl(url);
		Json.parse(str);
		start = Json.get_weight();
		for(int i=0; i<Json.get_number(); i++){
			System.out.println(start.get(i));
		}
		System.out.println(System.getProperty("os.name"));
		//System.out.println(str);
		
		/*
		rhyme = Tone.get_rhyme(concept);
		for(int i=0; i<rhyme.size(); i++)
			System.out.println(rhyme.get(i));
		*/
		
	}	
	
}