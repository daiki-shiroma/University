package enshud.s3.checker;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;

import enshud.s2.parser.Parser;

public class Checker {
	String[] str= {};
	String[][] variable= new String[100][3];
	String scope="global";
	int index=0;
	int indexTemp=0;
	int errorType=0;
	public static void main(final String[] args) {
		new Checker().run("data/ts/normal10.ts");
	}

	public void run(final String inputFileName) {
		int flag=0;
		try {
			File f = new File(inputFileName);
			Scanner scanner = new Scanner(f);
			str = scanner.nextLine().split("\t");

			if (str[1].equals("SPROGRAM")) {
				str = scanner.nextLine().split("\t");

				if (str[1].equals("SIDENTIFIER")) {
					str = scanner.nextLine().split("\t");

					if (str[1].equals("SSEMICOLON")) {
						while(scanner.hasNextLine()) {

							str = scanner.nextLine().split("\t");

							if ((str[1].equals("SEND")) &&(!scope.equals("global")) ) scope="global";

							if (str[1].equals("SVAR")) {
								if(!SVAR(scanner)) break;
							}
							if (str[1].equals("SIDENTIFIER")) {
								if(!SIDENTIFIER(scanner,scope))break;
							}
							if (str[1].equals("SWRITELN")) {
								if(!SWRITELN(scanner))break;
							}
							if (str[1].equals("SWHILE")) {
								if(!SWHILE(scanner))break;
							}
							if (str[1].equals("SIF")) {
								if(!SIF(scanner))break;
							}
							if (str[1].equals("SPROCEDURE")) {
								if(!SPROCEDURE(scanner))break;
							}
							if (str[1].equals("SDOT")) {
								flag=1;
								grammerCorrect();
							}
						}
					}
				}
			}
			//for (int i=0; i<index; i++) System.out.println(variable[i][0]+" "+variable[i][1]+" "+variable[i][2]);
			if (flag!=1) grammerError(str,errorType); 
			scanner.close();
		} catch (IOException e) {
			System.err.println("File not found");
		}
	}

	public boolean SVAR(Scanner scanner) {
		str = scanner.nextLine().split("\t");
		 
		if (str[1].equals("SIDENTIFIER")) { 
			if(SIDENTIFIER(scanner,scope)) return true;
		}
		return false;	
	}

	public boolean SIDENTIFIER(Scanner scanner, String scope) {
		int flag=0;
		String temp;
		temp=str[0];
		variable[index][1]=temp;
		//System.out.println(str[0]);
		str = scanner.nextLine().split("\t");
		//System.out.println(str[0]);
		if (str[1].equals("SCOMMA")) { 
			while(true) {
				if (flag==0) {
					if (str[1].equals("SCOMMA")) {
						if (!temp.equals(",")) {
							variable[index][1]=temp;
							index++;
						}
						str = scanner.nextLine().split("\t");
						temp=str[0];				
						flag=1;
					}
					else return false;
				}
				if (flag==1) {
					if (str[1].equals("SIDENTIFIER")) {
						if (!temp.equals(",")) {
							variable[index][1]=temp;
							index++;
						}
						str = scanner.nextLine().split("\t");
						temp=str[0];
						flag=0;
					}
					else return false;
				}
				if (flag==0 && str[1].equals("SCOLON")) {
					break;
				}
			}
		}

		if(str[1].equals("SLPAREN") || str[1].equals("SLBRACKET") ) { 
			if (!Brackets(scanner)) return false;
			else {
				if (str[1].equals("SSEMICOLON"))return true;
				else str = scanner.nextLine().split("\t"); 
			}
		}


		if (str[1].equals("SCOLON")) { 
			if (!temp.equals(":")) {
				if(varDuplicationCheck(temp)) {
					variable[index][1]=temp;
					index++;
				}
				else {
					errorType=1;
					return false;
				}
			}
			temp=str[0];

			str = scanner.nextLine().split("\t");

			if (str[1].equals("SINTEGER") || str[1].equals("SCHAR") || str[1].equals("SBOOLEAN") ) {
				temp=str[0];

				for (int i=indexTemp; i<index; i++) {
					variable[i][2]=temp;
					if(scope.equals("global")) variable[i][0]="global";
					else variable[i][0]=scope;
				}
				scope="global";
				//for (int i=0; i<index; i++) System.out.println(variable[i][0]+" "+variable[i][1]+" "+variable[i][2]);
				indexTemp=index;
				str = scanner.nextLine().split("\t");

				if (str[1].equals("SSEMICOLON")) return true;
			}
			else if(str[1].equals("SARRAY") ) {
				str = scanner.nextLine().split("\t");

				if(str[1].equals("SLBRACKET") ) {
					str = scanner.nextLine().split("\t");

					if(str[1].equals("SCONSTANT") ) {
						str = scanner.nextLine().split("\t");

						if(str[1].equals("SRANGE") ) {
							str = scanner.nextLine().split("\t");

							if(str[1].equals("SCONSTANT") ) {
								str = scanner.nextLine().split("\t");

								if(str[1].equals("SRBRACKET") ) {
									str = scanner.nextLine().split("\t");

									if(str[1].equals("SOF") ) {
										str = scanner.nextLine().split("\t");

										if (str[1].equals("SINTEGER") || str[1].equals("SCHAR") || str[1].equals("SBOOLEAN") ) {
											temp=str[0];
											for (int i=indexTemp; i<index; i++) {
												variable[i][2]=temp;
												if(scope.equals("global")) variable[i][0]="global";
												else variable[i][0]=scope;
											}
											scope="global";
												//for (int i=0; i<index; i++) System.out.println(variable[i][0]+" "+variable[i][1]+" "+variable[i][2]);
											indexTemp=index;
											str = scanner.nextLine().split("\t");
											if (str[1].equals("SSEMICOLON")) return true;
										}
									}
								}
							}
						}
					}
				}
			}
		}

		else if (str[1].equals("SASSIGN")) {

			str = scanner.nextLine().split("\t");
			String substitution=str[0];
			String substitutionType=str[1];

			if (str[1].equals("SBOOLEAN")|| str[1].equals("STRUE") ||str[1].equals("SFALSE")||str[1].equals("SSTRING") ) {
				if (!temp.equals(":=")) {
					if(varTypeCheck(temp,substitution,substitutionType)) {}
					else {
						errorType=1;
						return false;
					}
				}
				temp=str[0];
				str = scanner.nextLine().split("\t");
				if (str[1].equals("SSEMICOLON"))return true;
			}
			else if (calculation(scanner)) return true;
		}

		else if (str[1].equals("SSEMICOLON")) return true;

		return false;	
	}

	public boolean calculation(Scanner scanner) {
		int flag=0;
		while(scanner.hasNextLine()) {

			if (str[1].equals("SLPAREN")||(str[1].equals("SLBRACKET"))) {
				while(true) {
					flag=1;
					if(Brackets(scanner)) {
						str = scanner.nextLine().split("\t");  
					}
					else return false;

					if (str[1].equals("SLPAREN")||(str[1].equals("SLBRACKET"))) continue;
					else break;
				}
			}

			if ((flag==1)&&(str[1].equals("SSEMICOLON")|| (str[1].equals("SRPAREN"))||(str[1].equals("SRBRACKET")))) return true; 

			if (str[1].equals("SCONSTANT") || str[1].equals("SIDENTIFIER") || str[1].equals("SSTRING") || str[1].equals("STRUE")) {

				str = scanner.nextLine().split("\t"); 

				if (str[1].equals("SSEMICOLON")|| (str[1].equals("SRPAREN"))||(str[1].equals("SRBRACKET")))  return true; 
			}

			else if (str[1].equals("SPLUS") || str[1].equals("SMINUS") 
					|| str[1].equals("SSTAR") || str[1].equals("SDIVD")||str[1].equals("SMOD")) {
				str = scanner.nextLine().split("\t"); 

				if (str[1].equals("SSEMICOLON")|| (str[1].equals("SRPAREN"))||(str[1].equals("SRBRACKET"))) return true;
			}
			else break;
		}
		if (str[1].equals("SEQUAL") || str[1].equals("SNOTEQUAL") || 
				str[1].equals("SLESS") || str[1].equals("SLESSEQUAL")||
				str[1].equals("SGREATEQUAL") || str[1].equals("SGREAT")) return true;

		if (str[1].equals("STHEN") || str[1].equals("SDO"))	return true; 

		if (str[1].equals("STRUE") || str[1].equals("SFALSE") || str[1].equals("SAND") 
				|| str[1].equals("SOR") || str[1].equals("SNOT")) return true;

		return false;
	}

	public boolean Brackets(Scanner scanner) { 
		int functionArgument=0;
		int flag=0;
		int something=0;
		int SLbracketflag=0;
		String temp;
		if(str[1].equals("SLBRACKET"))SLbracketflag=1;

		str = scanner.nextLine().split("\t");
		
		while(true) {
			if (str[1].equals("SLPAREN")||(str[1].equals("SLBRACKET"))) {
				something=1;
				if (Brackets(scanner)) str = scanner.nextLine().split("\t");
				else break;
			}
			

			if (str[1].equals("SIDENTIFIER")||str[1].equals("SCONSTANT")) { 
				
				if ((SLbracketflag==1)&&(str[1].equals("SIDENTIFIER"))&&(!checkSCONSTANT(str[0]))) {
					errorType=1;
					break;
				}
				something=1;
				temp=str[0];
				str = scanner.nextLine().split("\t");
				
				if (str[1].equals("SCOMMA")) { 
					functionArgument=1;
					while(true) {
						if (flag==0) {
							if (str[1].equals("SCOMMA")) {
								
								if (!temp.equals(",")) {
									variable[index][1]=temp;
									index++;
								}
								str = scanner.nextLine().split("\t");
								temp=str[0];		
								
								flag=1;
							}
							else return false;
						}
						//for (int i=0; i<index; i++) System.out.println(variable[i][0]+" "+variable[i][1]+" "+variable[i][2]);
						if (flag==1) {
							if ((!temp.equals(","))) {
								variable[index][1]=temp;
								index++;
							}
							
							if (!calculation(scanner)) {
								if(str[1].equals("SCOLON")||str[1].equals("SCOMMA")) {}
								else break;
							}
							else {str = scanner.nextLine().split("\t");}
							flag=0;
							temp=str[0];
							//System.out.println(str[0]);
						}
						if (flag==0 && str[1].equals("SCOLON")) break;

						if (flag==0 && (str[1].equals("SRPAREN") || str[1].equals("SRBRACKET"))) break;

						if (flag==0 && str[1].equals("SSEMICOLON")) break;
					}
				}

				if (str[1].equals("SCOLON")) { 
					functionArgument=1;
					str = scanner.nextLine().split("\t");
					if (str[1].equals("SINTEGER") || str[1].equals("SCHAR") ||str[1].equals("SBOOLEAN")) {
						temp=str[0];
						for (int i=indexTemp; i<index; i++) {
							variable[i][2]=temp;
							if(scope.equals("global")) variable[i][0]="global";
							else variable[i][0]=scope;
						}
						if(functionArgument==0)scope="global";
						indexTemp=index;
						str = scanner.nextLine().split("\t");
					}
					else break;	
				}
			} 

			if (str[1].equals("SPLUS") || str[1].equals("SMINUS") || str[1].equals("SSTAR") || str[1].equals("SDIVD") ) {
				something=1;
				if (calculation(scanner)) {
					if(str[1].equals("SSEMICOLON") ) {
						str = scanner.nextLine().split("\t");
					}
					else {}
				}
				else break;
			}

			if(str[1].equals("SNOT")) return true;

			if (something==0) break;

			if (str[1].equals("STHEN") || str[1].equals("SDO")) 	return true;

			if (str[1].equals("SSEMICOLON")) return true;

			if (str[1].equals("STRUE") || str[1].equals("SFALSE") || str[1].equals("SAND") 
					|| str[1].equals("SOR") ) return true;

			if (str[1].equals("SRPAREN") || str[1].equals("SRBRACKET")) return true;

			if (str[1].equals("SEQUAL") || str[1].equals("SNOTEQUAL") || 
					str[1].equals("SLESS") || str[1].equals("SLESSEQUAL")||
					str[1].equals("SGREATEQUAL") || str[1].equals("SGREAT")) return true;

		}
		return false;
	}

	public boolean conditionalExpression(Scanner scanner) {
		int brflag=0;
		while(true) {
			str = scanner.nextLine().split("\t");

			if(str[1].equals("SLPAREN") || str[1].equals("SLBRACKET") ) {
				str = scanner.nextLine().split("\t");
				brflag++;
			}
			if (str[1].equals("SNOT")) str = scanner.nextLine().split("\t");

			if (!calculation(scanner)) break;
			if (str[1].equals("SEQUAL") || str[1].equals("SNOTEQUAL") || 
					str[1].equals("SLESS") || str[1].equals("SLESSEQUAL")||
					str[1].equals("SGREATEQUAL") || str[1].equals("SGREAT")) {
				str = scanner.nextLine().split("\t");

				if (str[1].equals("SDO")||str[1].equals("STHEN"))break;

				if(str[1].equals("SLPAREN") || str[1].equals("SLBRACKET") ) {
					str = scanner.nextLine().split("\t");
				}

				if (str[1].equals("SNOT")) str = scanner.nextLine().split("\t");

				if (!calculation(scanner)) 	break;
			}

			if (str[1].equals("STRUE") || str[1].equals("SFALSE")) str = scanner.nextLine().split("\t");

			if(brflag>0 && (str[1].equals("SRPAREN"))) {
				str = scanner.nextLine().split("\t");
				brflag--;
			}

			if (str[1].equals("SAND") || str[1].equals("SOR")) {}

			if ((brflag==0)&&(str[1].equals("SDO")|| str[1].equals("STHEN")))  return true;

		}
		return false;
	}

	public boolean SWHILE(Scanner scanner) {
		if (conditionalExpression(scanner)) {
			str = scanner.nextLine().split("\t");

			if (str[1].equals("SBEGIN")) {
				while(scanner.hasNextLine()) {

					if ((str[1].equals("SWHILE"))||(str[1].equals("SIF"))||(str[1].equals("SWRITELN"))){}
					else	str = scanner.nextLine().split("\t");

					if(str[1].equals("SWHILE")) {
						if (!SWHILE(scanner)) return false; 
					}

					if(str[1].equals("SIF")) {
						if (!SIF(scanner)) return false; 
					}

					if(str[1].equals("SWRITELN")) {
						if (!SWRITELN(scanner)) return false; 
					}

					if (str[1].equals("SIDENTIFIER")) {
						if (!SIDENTIFIER(scanner,scope)) return false; 
					}

					if (str[1].equals("SEND")) {
						str = scanner.nextLine().split("\t");
						break; 
					}
				}

				if (str[1].equals("SSEMICOLON")) return true; 
			}
		}
		return false;
	}

	public boolean SIF(Scanner scanner) {

		if (conditionalExpression(scanner)) {
			str = scanner.nextLine().split("\t");

			if (str[1].equals("SBEGIN")) {
				while(scanner.hasNextLine()) {
					str = scanner.nextLine().split("\t");

					if (str[1].equals("SIF")) {
						if (!SIF(scanner)) return false; 
					}

					if (str[1].equals("SWHILE")) {
						if (!SWHILE(scanner)) return false; 
					}

					if (str[1].equals("SWRITELN")) {
						if (!SWRITELN(scanner)) return false; 
					}

					if (str[1].equals("SIDENTIFIER")) {
						if (!SIDENTIFIER(scanner,scope)) return false; 
					}

					if (str[1].equals("SEND")) {
						str = scanner.nextLine().split("\t");
						break; 
					}
				}

				if (str[1].equals("SSEMICOLON")) return true; 
				else if(str[1].equals("SELSE")) {
					str = scanner.nextLine().split("\t");

					if (str[1].equals("SBEGIN")) {
						while(scanner.hasNextLine()) {
							str = scanner.nextLine().split("\t");

							if (str[1].equals("SIF")) {
								if (!SIF(scanner)) return false; 
							}

							if (str[1].equals("SWHILE")) {
								if (!SWHILE(scanner)) return false; 
							}

							if (str[1].equals("SWRITELN")) {
								if (!SWRITELN(scanner)) return false; 
							}

							if (str[1].equals("SIDENTIFIER")) {
								if (!SIDENTIFIER(scanner,scope)) return false; 
							}

							if (str[1].equals("SEND")) {
								str = scanner.nextLine().split("\t");
								break; 
							}
						}
						if (str[1].equals("SSEMICOLON")) return true; 
					}
				}
			}
		}
		return false;
	}

	public boolean SPROCEDURE(Scanner scanner) {
		str = scanner.nextLine().split("\t");
		scope=str[0];

		if (str[1].equals("SIDENTIFIER")) {
			str = scanner.nextLine().split("\t");

			if (str[1].equals("SLPAREN")) {
				
				if (Brackets(scanner)) str = scanner.nextLine().split("\t");
				else return false;
			}
			if (str[1].equals("SSEMICOLON")) {
				return true;
			}
		}
		return false;
	}

	public boolean SWRITELN(Scanner scanner) {
		str = scanner.nextLine().split("\t");
		if (str[1].equals("SLPAREN")) {
			str = scanner.nextLine().split("\t");

			while(scanner.hasNextLine()) {
				if (str[1].equals("SIDENTIFIER") || str[1].equals("SSTRING")) {
					str = scanner.nextLine().split("\t");

					if (str[1].equals("SLPAREN") || str[1].equals("SLBRACKET") ) {
						if (!Brackets(scanner)) return false;
						else 	str = scanner.nextLine().split("\t");
					}
					if (str[1].equals("SCOMMA")) {
						str = scanner.nextLine().split("\t");
					}
					if (str[1].equals("SRPAREN")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean varDuplicationCheck(String varName) {
		for (int i=0; i<100;i++) {
			if((variable[i][0]!=null)&&(variable[i][0].equals(scope))) {
				if (variable[i][1].equals(varName)) {
					return false;
				}
			}

		}
		return true;
	}

	public boolean varTypeCheck(String varName, String substitution,String substitutionType) {
		//System.out.println(varName);
		//System.out.println(substitution);
		String varType=null;
		for (int i=0; i<100;i++) {
			if((variable[i][0]!=null)&&(variable[i][0].equals(scope))) {

				if (variable[i][1].equals(varName)) {

					if (str[1].equals("SIDENTIFIER")) {

						for (int j=0; j<100;j++) {
							if((variable[j][0]!=null)&&(variable[j][0].equals(scope))) {
								if (variable[j][1].equals(substitution)) {
									varType=variable[j][2];
									break;
								}
							}
						}
					}

					if (varType==null) varType=substitutionType;

					if (variable[i][2].equals(varType)) {			
						return true;
					}
					else if ((variable[i][2].equals("integer")) &&(varType.equals("SCONSTANT")) ) {
						return true;
					}
					else if ((variable[i][2].equals("char")) &&(varType.equals("SSTRING")) ) {
						return true;
					}
					else if ((variable[i][2].equals("boolean")) &&(varType.equals("STRUE") || varType.equals("SFALSE")) ) {
						return true;
					}
					else break;
				}
			}

		}
		return false;
	}
	
	
	public boolean checkSCONSTANT(String varName) {
		for (int i=0; i<100;i++) {
			
			if((variable[i][0]!=null)&&(variable[i][0].equals(scope))) {
				if (variable[i][1].equals(varName)) {
					//System.out.println(variable[i][1]);
					//System.out.println(variable[i][2]);
					if (variable[i][2].equals("integer")) {
						return true;
					}
				}
			}

		}
		return false;
	}

	public void grammerCorrect() {
		System.out.println("OK");
	}

	public void grammerError(String[] str, int errorType) {	
		if (errorType==1)System.err.println("Semantic error: line " + str[3]);
		else System.err.println("Syntax error: line " + str[3]);
	}

}
