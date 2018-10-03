package SCM_TA_V1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class changeRepresentation {

	public static void txtToCSV() throws FileNotFoundException{
		
		Scanner sc;
		PrintWriter pw;
		String[] items;
		String[] items_name = null;
		String[] fileName;
		String fileNum=""; 
		String runNum;
		int i_fileNUmber=0;
		StringBuilder sb1=new StringBuilder();
		StringBuilder sb2=new StringBuilder();
		for(File fileEntry:new File(System.getProperty("user.dir")+"//results//results").listFiles()){
			sc=new Scanner(new File(fileEntry.toURI()));
			fileName=fileEntry.getName().split("_|\\.");
			runNum=fileName[1];
			fileNum=fileName[2];
			int i=0;
			while(sc.hasNextLine()){
				if(i%11!=0){
					items=sc.nextLine().split(": ");
					System.out.println(i);
					sb2.append(items[1]+",");
					if(i_fileNUmber==0)
						sb1.append(items_name[0]+items[0]+",");
				}
				else{
					if(i_fileNUmber==0)
						items_name=sc.nextLine().split(": ");
					else
						sc.nextLine();
				}
				i++;
			}
			if(i_fileNUmber==0){
				sb1.setLength(sb1.length()-1);
				sb1.append("\n");
			}
			sb2.setLength(sb2.length()-1);
			sb2.append("\n");
			i_fileNUmber++;
		}
		pw=new PrintWriter(new File(System.getProperty("user.dir")+"//results//R1//analysis_"+fileNum+".csv"));
		pw.write(sb1.toString());
		pw.write(sb2.toString());
		pw.close();
		System.out.println("it's done");
		System.out.println(i_fileNUmber);
	}
}