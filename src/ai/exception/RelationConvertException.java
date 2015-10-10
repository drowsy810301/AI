package ai.exception;

public class RelationConvertException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8554505723468694746L;
	String input;
	public RelationConvertException(String input) {
		this.input = input;
	}
	
	public RelationConvertException(int index) {
		this.input = "編號"+String.valueOf(index);
	}
	
	public String getMessage() {
		return input+" 沒有對應到任何 relation";
	}
}
