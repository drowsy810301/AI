package ai.word;

import ai.exception.RelationConvertException;

public enum Relation {
	TOPIC(-3,"/r/topic","",""),
	PADDING(-2,"/r/padding","",""),
	IsA(0,"/r/IsA","名","名"),
	PartOf(1,"/r/PartOf","名","名"),
	HasProperty(2,"/r/HasProperty","名","形"),
	UsedFor(3,"/r/UsedFor","名","動"),
	CapableOf(4,"/r/CapableOf","名","動"),			// S能做的事情有E
	  CapableOf0(5,"/r/CapableOf0","名","動"), 		// S會E
	AtLocation(6,"/r/AtLocation","名","名"),
	Causes(7,"/r/Causes","名動","形動"), 				// S之後可能會發生的事情是E
	  Causes0(8,"/r/Causes0","名動","形動"),			// 因為S所以E
	  Causes1(9,"/r/Causes1","名動","形動"),			// S可能會帶來E
	  Causes2(10,"/r/Causes2","名動","形動"), 		// S可能會引起E
	  Causes3(11,"/r/Causes3","名動","形動"),			// S會讓你E
	HasSubevent(12,"/r/HasSubevent","動","動"),		// 在S你會E / S的時候你會E
	  HasSubevent0(13,"/r/HasSubevent0","動","動"),	// E 可能代表 S 
	  HasSubevent1(14,"/r/HasSubevent1","動","動"),	// S 的時候可以 E
	HasFirstSubevent(15,"/r/HasFirstSubevent","動","動"),
	MotivatedByGoal(16,"/r/MotivatedByGoal","動","名形動"),
	  MotivatedByGoal0(17,"/r/MotivatedByGoal0","動","名形動"),
	  MotivatedByGoal1(18,"/r/MotivatedByGoal1","動","名形動"),
	  MotivatedByGoal2(19,"/r/MotivatedByGoal2","動","名形動"),
	Desires(20,"/r/Desires","名","名動"),
	  NotDesires(21,"/r/NotDesires","名","名動"),			// S 不想要/痛恨/懼怕 E
	MadeOf(22,"/r/MadeOf","名","名"),
	  MadeOf0(23,"/r/MadeOf0","名","名"),				// S由E組成
	CausesDesire(24,"/r/CausesDesire","名","動"),
	SymbolOf(25,"/r/SymbolOf","名","名");
	
	public static final int START = 0;
	public static final int END = 1;
	// TODO 要扣掉 <0  的部分
	public static final int TOTAL_RELATION = Relation.values().length-2; 
	
	private int index;
	private String str;
	int startWordType,endWordType;
	
	private Relation(int index, String str, String startWordType, String endWordType) {
		this.index = index;
		this.str = str;
		
		this.startWordType = ChineseWord.convertWordType(startWordType);
		this.endWordType =  ChineseWord.convertWordType(endWordType);
	}
	
	public int getIndex(){
		return this.index;
	}
	
	public String toString(){
		return this.str;
	}
	
	public static int getWordType(Relation relation,int startOrEnd){
		if (startOrEnd == START){
			return relation.startWordType;
		}
		else if (startOrEnd == END){
			return relation.endWordType;
		}
		else{
			System.err.println("error : the second parameter can either be 0 (START) or 1 (END)");
			System.exit(1);
			return -1;
		}
	}
	
	static public Relation getRelation(String str) throws RelationConvertException{
		for (Relation relation : Relation.values()){
			if ( relation.toString().equals(str))
				return relation;
		}
		throw new RelationConvertException(str);
	}
	
	static public Relation getRelation(int index) throws RelationConvertException{
		for (Relation relation : Relation.values()){
			if ( relation.getIndex() == index)
				return relation;
		}
		throw new RelationConvertException(index);
	}
}
