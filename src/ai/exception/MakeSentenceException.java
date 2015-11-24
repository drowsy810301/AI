package ai.exception;

public class MakeSentenceException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5622193619489341012L;
	String message;
	
	public MakeSentenceException(int[] lineComposition) {
		StringBuilder sb = new StringBuilder();
		sb.append("無法用 [");
		for ( int element : lineComposition){
			sb.append(" "+String.valueOf(element));
		}
		sb.append(" ] 來造句");
		message = sb.toString();
	}
	
	public MakeSentenceException(String[] sentenceTemplate){
		StringBuilder sb = new StringBuilder();
		sb.append("無法造(");
		for (String str : sentenceTemplate){
			sb.append(" "+str);
		}
		sb.append(" )的句型");
		message = sb.toString();
	}
	
	public MakeSentenceException(int[] lineComposition,String[] sentenceTemplate) {
		StringBuilder sb = new StringBuilder();
		sb.append("無法用 [");
		for ( int element : lineComposition){
			sb.append(" "+String.valueOf(element));
		}
		sb.append(" ] 來造(");
		for (String str : sentenceTemplate){
			sb.append(" "+str);
		}
		sb.append(" )的句型");
		message = sb.toString();
	}
	
	public String getMessage(){
		return message;
	}
}
