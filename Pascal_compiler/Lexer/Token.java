package enshud.s1.lexer;
import java.util.Objects;


public class Token{
	int id; 
	String type; 
	String[][] reserved_word= {
			{"and","SAND"},{"array","SARRAY"},{"begin","SBEGIN"},{"boolean","SBOOLEAN"},{"char","SCHAR"},{"div","SDIVD"},{"do","SDO"},
			{"else","SELSE"},{"end","SEND"},{"false","SFALSE"},{"if","SIF"},{"integer","SINTEGER"},{"mod","SMOD"},{"not","SNOT"},
			{"of","SOF"},{"or","SOR"},{"procedure","SPROCEDURE"},{"program","SPROGRAM"},{"readln","SREADLN"},{"then","STHEN"},{"true","STRUE"},
			{"var","SVAR"},{"while","SWHILE"},{"writeln","SWRITELN"},{"=","SEQUAL"},{"<>","SNOTEQUAL"},{"<","SLESS"},{"<=","SLESSEQUAL"},{">=","SGREATEQUAL"}
			,{">","SGREAT"},{"+","SPLUS"},{"-","SMINUS"},{"*","SSTAR"},{"(","SLPAREN"},{")","SRPAREN"},{"[","SLBRACKET"},{"]","SRBRACKET"},{";","SSEMICOLON"}
			,{":","SCOLON"},{"..","SRANGE"},{":=","SASSIGN"},{",","SCOMMA"},{".","SDOT"}};

	public  Token tokenMethod(String str) {
		Token tokenModel = new Token();

		boolean isNumeric = true;
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isDigit(str.charAt(i))) {
				isNumeric = false;
			}
		}

		if (isNumeric) {
			tokenModel.type="SCONSTANT";
			tokenModel.id=44;
		}

		else {
			int i=0;
			while(i<43) {
				if (Objects.equals(str, reserved_word[i][0])) break;
				i++;
			}
			if (i==43) {
				tokenModel.type="SIDENTIFIER";
			}
			else tokenModel.type=reserved_word[i][1];
			tokenModel.id=i;
		}
		return tokenModel;
	}
}