package enshud.s4.compiler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Makecaslfile {
	
	public void MakeCaslFile(StringBuilder sb, StringBuilder sbsub, 
			StringBuilder subRoutineBuff, String variable[][] ) {
		int countVarlist = 0;
		
		try {
			FileWriter file = new FileWriter("tmp/out.cas");
			PrintWriter pw = new PrintWriter(new BufferedWriter(file));
			sbsub.setLength(0);	
			sbsub.append("CASL\t" +"START\t" +"BEGIN\n");	
			sbsub.append("BEGIN\t" +"LAD\t" +"GR6,\t"+"0\n");	
			sbsub.append("\t" +"LAD\t" +"GR7,\t"+"LIBBUF\n");	
			pw.print(sbsub);

			
			for (int i = 0; variable[i][0] != null; i++)
				countVarlist = i + 1;

			sb.append("VAR\t" +"DS\t" +countVarlist+"\n");	
			pw.print(sb);

			subRoutineBuff.append("LIBBUF\t" +"DS\t" +"256\n");	
			subRoutineBuff.append("\t" +"END" +"\n");
			pw.print(subRoutineBuff);

			addLibcasFile(pw);
			
			pw.close();
			
		} catch (IOException e) {
			System.err.println("cannot make cas file");
		}
			
	}
	
	
	
	public void addLibcasFile(PrintWriter pw) {
		try {
			File f = new File("data/cas/lib.cas");
			Scanner scanner = new Scanner(f);
			scanner.useDelimiter("\n");
			while (scanner.hasNext()) {
				String str = scanner.next();
				pw.println(str);
			}
			scanner.close();
			pw.close();
		} catch (IOException e) {
			System.err.println("lib.cas not found");
		}
	}
	
}