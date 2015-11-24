package Score;
import java.io.File;
import java.util.ArrayList;


public class ScoreCalculator {
	int poem_id;
	public ScoreCalculator()
	{
		poem_id=0;
		deleteFile("CKIPClient/CKIPClient/test-utf-8/in"); 
		deleteFile("CKIPClient/CKIPClient/test-utf-8/out");
	}
	public void deleteFile(String dir)
	{
		File directory = new File(dir);
		File[] files = directory.listFiles();
		for (File file : files)
		{
		   if (!file.delete())
		   {
		       System.out.println("Failed to delete " + file);
		   }
		} 
	}
	
	public float cal(ArrayList<ArrayList<String>> poem)
	{
		ArrayList<String> input = new ArrayList<String>();
		CKIPconnecter c = new CKIPconnecter();
		int score=0;
		for(int i=0; i<4; i++)
		{
			for(int j=0; j<poem.get(i).size(); j++)
			{
				for(int k=0; k<poem.get(i).get(j).length(); k++)
				{
					input.add(new String(new String(String.valueOf(poem.get(i).get(j).charAt(k)))));
				}
					
			}
		}
		ArrayList<String> result = c.process(input, poem_id);
		int count=0;;
		//System.out.println("result: " + result.size());
		if(result.size()==0) return (float) -1.0;
		for(int i=0; i<5; i++)
		{
			if(result.get(i).compareTo(result.get(i+5))==0) score++;
			count++;
		}
		for(int i=10; i<15; i++)
		{
			if(result.get(i).compareTo(result.get(i+5))==0) score++;
			count++;
		}
		poem_id++;
		return (float) score/count;
	}
	public static void main(String[] argv)
	{
		/*CKIPconnecter c = new CKIPconnecter();
		int t=0;
		c.test(t);*/
		ArrayList<ArrayList<String>> poem = new ArrayList<ArrayList<String>>();
		for(int i=0;i<4;i++) poem.add(new ArrayList<String>());
		poem.get(0).add("白日依山盡");
		poem.get(1).add("黃河入海流");
		poem.get(2).add("欲窮千里目");
		poem.get(3).add("更上一層樓");
		/*for(int i=0; i<4; i++)
		{
			for(int j=0; j<poem.get(i).size(); j++)
			{
				//System.out.println(poem.get(i).get(j));
				for(int k=0; k<poem.get(i).get(j).length(); k++)
				{
					System.out.println(poem.get(i).get(j).charAt(k));
				}
			}
		}*/
		ScoreCalculator z=new ScoreCalculator();
		System.out.println("score: " + z.cal(poem));
	}
}

