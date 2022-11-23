package enshud.s2.parser;
import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Parser {
	String[] str= {};

	public static void main(final String[] args) {
		new Parser().run("data/ts/normal12.ts");
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

							if (str[1].equals("SVAR")) {
								if(!SVAR(scanner)) break;
							}
							if (str[1].equals("SIDENTIFIER")) {
								if(!SIDENTIFIER(scanner))break;
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
			if (flag!=1) grammerError(str); 
			scanner.close();
		} catch (IOException e) {
			System.err.println("File not found");
		}
	}

	public boolean SVAR(Scanner scanner) {
		str = scanner.nextLine().split("\t");
		if (str[1].equals("SIDENTIFIER")) { 
			if(SIDENTIFIER(scanner)) return true;
		}
		return false;	
	}

	public boolean SIDENTIFIER(Scanner scanner) {
		int flag=0;
		str = scanner.nextLine().split("\t");

		if (str[1].equals("SCOMMA")) { 
			while(true) {
				if (flag==0) {
					if (str[1].equals("SCOMMA")) {
						str = scanner.nextLine().split("\t");
						flag=1;
					}
					else return false;
				}
				if (flag==1) {
					if (str[1].equals("SIDENTIFIER")) {
						str = scanner.nextLine().split("\t");
						flag=0;
					}
					else return false;
				}
				if (flag==0 && str[1].equals("SCOLON")) break;
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
			str = scanner.nextLine().split("\t");

			if (str[1].equals("SINTEGER") || str[1].equals("SCHAR") || str[1].equals("SBOOLEAN") ) { 
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

			if (str[1].equals("SBOOLEAN")|| str[1].equals("STRUE") ||str[1].equals("SFALSE")||str[1].equals("SSTRING") ) {
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
		int flag=0;
		int something=0;
		str = scanner.nextLine().split("\t");
		while(true) {
			if (str[1].equals("SLPAREN")||(str[1].equals("SLBRACKET"))) {
				something=1;
				if (Brackets(scanner)) str = scanner.nextLine().split("\t");
				else break;
			}

			if (str[1].equals("SIDENTIFIER")||str[1].equals("SCONSTANT")) { 
				something=1;
				str = scanner.nextLine().split("\t");

				if (str[1].equals("SCOMMA")) { 
					while(true) {
						if (flag==0) {
							if (str[1].equals("SCOMMA")) {
								str = scanner.nextLine().split("\t");
								flag=1;
							}
							else return false;
						}
						if (flag==1) {
							if (!calculation(scanner)) {
								if(str[1].equals("SCOLON")||str[1].equals("SCOMMA")) {}
								else break;
							}
							else {str = scanner.nextLine().split("\t");}
							flag=0;
						}
						if (flag==0 && str[1].equals("SCOLON")) break;

						if (flag==0 && (str[1].equals("SRPAREN") || str[1].equals("SRBRACKET"))) break;

						if (flag==0 && str[1].equals("SSEMICOLON")) break;
					}
				}

				if (str[1].equals("SCOLON")) { 
					str = scanner.nextLine().split("\t");
					if (str[1].equals("SINTEGER") || str[1].equals("SCHAR") ||str[1].equals("SBOOLEAN")) str = scanner.nextLine().split("\t");
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
					brflag++; //add
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
						if (!SIDENTIFIER(scanner)) return false; 
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
						if (!SIDENTIFIER(scanner)) return false; 
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
								if (!SIDENTIFIER(scanner)) return false; 
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

	public void grammerCorrect() {
		System.out.println("OK");
	}

	public void grammerError(String[] str) {
		System.err.println("Syntax error: line " + str[3]);
	}
}
