package SCM_TA_V2;

import java.util.ArrayList;

public class State {
	
	String name;
	Integer id;
	ArrayList<String> actionSet=new ArrayList<String>();
	
	public State(String name, Integer id){
		this.name=name;
		this.id=id;
	}
	
	public void setAction(String s){
		actionSet.add(s);
	}
	
}
