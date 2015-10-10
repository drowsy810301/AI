package ai.exception;

import ai.word.Relation;

public class RelationWordException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4154851293944225959L;
	private String message;
	public RelationWordException(int relation, int length) {
		try {
			message = "找不到 rel = "+Relation.getRelation(relation).toString()+", 長度 = "+length+" 的詞\n";
		} catch (RelationConvertException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public String getMessage(){
		return message;
	}
	
	
}
