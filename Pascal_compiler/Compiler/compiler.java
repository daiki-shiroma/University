package enshud.s4.compiler;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;

import enshud.casl.CaslSimulator;

public class Compiler {
	String[] str= {};
	String[][] variable= new String[100][6];
	String scope="global";
	int index=0;
	int indexTemp=0;
	int errorType=0;
	int booleanType=0;
	
	String eachprocess="";
	StringBuilder sb = new StringBuilder();
	StringBuilder subRoutineBuff = new StringBuilder();
	int subRoutineBuffCount=0;
	
	
	public static void main(final String[] args) {
		// Compilerを実行してcasを生成する
		new Compiler().run("data/ts/normal02.ts", "tmp/out.cas");

		// 上記casを，CASLアセンブラ & COMETシミュレータで実行する
		CaslSimulator.run("tmp/out.cas", "tmp/out.ans");
	}

	/**
	 * TODO
	 * 
	 * 開発対象となるCompiler実行メソッド．
	 * 以下の仕様を満たすこと．
	 * 
	 * 仕様:
	 * 第一引数で指定されたtsファイルを読み込み，CASL IIプログラムにコンパイルする．
	 * コンパイル結果のCASL IIプログラムは第二引数で指定されたcasファイルに書き出すこと．
	 * 構文的もしくは意味的なエラーを発見した場合は標準エラーにエラーメッセージを出力すること．
	 * （エラーメッセージの内容はChecker.run()の出力に準じるものとする．）
	 * 入力ファイルが見つからない場合は標準エラーに"File not found"と出力して終了すること．
	 * 
	 * @param inputFileName 入力tsファイル名
	 * @param outputFileName 出力casファイル名
	 */
	public void run(final String inputFileName, final String outputFileName) {
		int flag=0;
		try {
			File f = new File(inputFileName);
			
			File fileExist = new File("tmp/out.cas");
			if(fileExist. exists()) fileExist.delete();
			
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
								sb.append("\t"+"RET"+"\n");
								flag=1;
								grammerCorrect();
							}
						}
					}
				}
			}
			if (flag!=1) grammerError(str,errorType); 
			scanner.close();
		} catch (IOException e) {
			System.err.println("File not found");
		}
		
		
		///compiler

			
		try {
			// System.out.println("make out.cass is success");	
            // FileWriterクラスのオブジェクトを生成する
            FileWriter file = new FileWriter("tmp/out.cas");
            // PrintWriterクラスのオブジェクトを生成する
            PrintWriter pw = new PrintWriter(new BufferedWriter(file));
            
            //add introduction
            pw.print("CASL\t");
            pw.print("START\t");
            pw.print("BEGIN");
            pw.print("\n");
            
            pw.print("BEGIN\t");
            pw.print("LAD\t");
            pw.print("GR6,\t");
            pw.print("0\n");
            
            pw.print("     \t");
            pw.print("LAD\t");
            pw.print("GR7,\t");
            pw.print("LIBBUF\n");
            //add introduction
            
            //add each cas file process 
            // System.out.print(sb);
             //sbの型変換
             pw.print(sb);
           //add each cas file process 
             
             
             
             // addvarlist
             int countVarlist=0;
             for (int i=0; variable[i][0]!=null; i++) {
            	 System.out.print(variable[i][1]);
            	 System.out.print(variable[i][2]);
            	 System.out.print(variable[i][3]);
            	 System.out.print(variable[i][4]);
            	 System.out.println(variable[i][5]);
  
            	 if (variable[i][5]!=null) {
            		 countVarlist= countVarlist +Integer.parseInt(variable[i][5]) - Integer.parseInt(variable[i][4]) + 1 ;
            	 }
            	 else  countVarlist++;	 
             }
               
             pw.print("VAR\t");
             pw.print("DS\t");
             pw.println(countVarlist);
             
             // addvarlist            
            
             
             // addsubroutine
             pw.print(subRoutineBuff);
             // addsubroutine
            
           
            pw.print("LIBBUF\t");
            pw.print("DS\t");
            pw.print("256\n");
            
            pw.print("\t"+"END"+"\n");
            
        	
            
            // add lib.cas
            try {
    			File f = new File("data/cas/lib.cas");
    			Scanner scanner = new Scanner(f);
    			scanner.useDelimiter("\n");	
    			while (scanner.hasNext()){
    		        String str = scanner.next();
    		        pw.println(str);
    		      }
    			scanner.close();
    			pw.close();
    		} catch (IOException e) {
    			System.err.println("lib.cas not found");
    		}
            
            //add lib.cas
            
            pw.close();
        } catch (IOException e) {
        	System.err.println("cannot make cas file");
        }

		// TODO

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
		int arrayflag=0;
		String temp;
		temp=str[0];
		variable[index][1]=temp;

		if(arrayCheck(temp))arrayflag=1;

		str = scanner.nextLine().split("\t");

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
				arrayflag=0;
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
				indexTemp=index;
				str = scanner.nextLine().split("\t");
				if (str[1].equals("SSEMICOLON")) return true;
			}
			
			else if(str[1].equals("SARRAY") ) {
				str = scanner.nextLine().split("\t");

				if(str[1].equals("SLBRACKET") ) {
					str = scanner.nextLine().split("\t");

					if(str[1].equals("SCONSTANT") ) {
						
						String arraytopnum=str[0];
						
						str = scanner.nextLine().split("\t");

						if(str[1].equals("SRANGE") ) {
							str = scanner.nextLine().split("\t");

							if(str[1].equals("SCONSTANT") ) {
								
								String arraybotomnum=str[0];
								
								str = scanner.nextLine().split("\t");

								if(str[1].equals("SRBRACKET") ) {
									str = scanner.nextLine().split("\t");

									if(str[1].equals("SOF") ) {
										str = scanner.nextLine().split("\t");

										if (str[1].equals("SINTEGER") || str[1].equals("SCHAR") || str[1].equals("SBOOLEAN") ) {
											temp=str[0];
											for (int i=indexTemp; i<index; i++) {
												variable[i][5]=arraybotomnum;
												variable[i][4]=arraytopnum;
												variable[i][3]="array";
												variable[i][2]=temp;
												if(scope.equals("global")) variable[i][0]="global";
												else variable[i][0]=scope;
											}
											scope="global";
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
			if (arrayflag==1) {
				errorType=1;
				return false;
			}
			str = scanner.nextLine().split("\t");
			String substitution=str[0];
			String substitutionType=str[1];			
			if (str[1].equals("SBOOLEAN")|| str[1].equals("STRUE") ||str[1].equals("SFALSE")||str[1].equals("SSTRING") ) {
				if (!temp.equals(":=")) {
					if(substitutionTypeCheck(temp,substitution,substitutionType)) {}
					else {
						errorType=1;
						return false;
					}
				}
				temp=str[0];
				str = scanner.nextLine().split("\t");
				if (str[1].equals("SSEMICOLON"))return true;
			}
			else {
				if (str[1].equals("SIDENTIFIER") ) {
					String varType=varTypeCheck(temp);
					if ((varType!=null)&&(varType.equals("integer")||(varType.equals("char")))) {
						if(substitutionTypeCheck(temp,substitution,varType)) {}
						else {
							errorType=1;
							return false;
						}
					}
				}
				if (calculation(scanner)) return true;
			}
		}

		else if (str[1].equals("SSEMICOLON")) {
			if (procedureCheck(temp))return true;
			else errorType=1;
		}
		return false;	
	}

	public boolean calculation(Scanner scanner) {

		int flag=0;
		int integerType=0;
		int charType=0;
		int stringType=0;
		int booleanType=0;
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

			if (str[1].equals("SCONSTANT")) {
				integerType=1;
				if ((integerType+charType+stringType+booleanType)>1) {
					errorType=1;
					break;
				}

				str = scanner.nextLine().split("\t"); 

				if (str[1].equals("SSEMICOLON")|| (str[1].equals("SRPAREN"))||(str[1].equals("SRBRACKET")))  return true; 
			}

			else if (str[1].equals("SIDENTIFIER")) {

				String temp=varTypeCheck(str[0]);
				if (temp!=null) {
					if (temp.equals("integer"))integerType=1;
					else if(temp.equals("char"))charType=1;
					//else if(temp.equals("string"))stringType=1;
					else if(temp.equals("boolean")) {
						booleanType=1;
						this.booleanType=1;
					}
				}

				if ((integerType+charType+stringType+booleanType)>1) {
					errorType=1;
					break;
				}

				str = scanner.nextLine().split("\t"); 

				if (str[1].equals("SSEMICOLON")|| (str[1].equals("SRPAREN"))||(str[1].equals("SRBRACKET")))  return true; 
			}

			else if (str[1].equals("SSTRING")) {
				stringType=1;
				if ((integerType+charType+stringType+booleanType)>1) {
					errorType=1;
					break;
				}
				str = scanner.nextLine().split("\t"); 
				if (str[1].equals("SSEMICOLON")|| (str[1].equals("SRPAREN"))||(str[1].equals("SRBRACKET")))  return true; 
			}

			else if (str[1].equals("STRUE")) {
				booleanType=1;
				if ((integerType+charType+stringType+booleanType)>1) {
					errorType=1;
					break;
				}
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

		if (str[1].equals("STHEN") || str[1].equals("SDO"))	{
			this.booleanType=booleanType;
			return true; 
		}

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
						
						if (flag==1) {
							if ((!temp.equals(","))) {
								variable[index][1]=temp;
								index++;
							}

							if (!calculation(scanner)) {
								if(str[1].equals("SCOLON")||str[1].equals("SCOMMA")) {}
								else break;
							}
							else str = scanner.nextLine().split("\t");
							flag=0;
							temp=str[0];
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

			if (str[1].equals("STHEN") || str[1].equals("SDO")) return true;

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
		int loopcount=0;
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
				loopcount++;
			}

			else if (str[1].equals("STRUE") || str[1].equals("SFALSE")) {
				str = scanner.nextLine().split("\t");
			}

			if (str[1].equals("STRUE") || str[1].equals("SFALSE")) str = scanner.nextLine().split("\t");

			if(brflag>0 && (str[1].equals("SRPAREN"))) {
				str = scanner.nextLine().split("\t");
				brflag--;
				loopcount++; 
			}

			if (str[1].equals("SAND") || str[1].equals("SOR")) loopcount++; 

			if ((brflag==0)&&(str[1].equals("SDO")|| str[1].equals("STHEN"))) {
				if(loopcount==0 && brflag==0) {
					if (booleanType==1) {
						booleanType=0;
						return true;
					}
					else {
						errorType=1;
						break;
					}
				}
				else return true;	
			}
			loopcount++; 
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
					
					if (str[1].equals("SSTRING")) {
						
						subRoutineBuff.append("CHAR"+subRoutineBuffCount+"\t");
						subRoutineBuff.append("DC"+"\t");
						subRoutineBuff.append(str[0]+"\n");
						subRoutineBuffCount++;
						
						int countStringlength=str[0].length()-2;
						sb.append("\t"+"LD"+"\t");
						sb.append("GR1,"+"\t");
						sb.append("="+countStringlength+"\n");
						
						sb.append("\t"+"PUSH"+"\t");
						sb.append("0,"+"\t");
						sb.append("GR1"+"\n");
						
						sb.append("\t"+"LAD"+"\t");
						sb.append("GR2,"+"\t");
						subRoutineBuffCount--;
						sb.append("CHAR"+subRoutineBuffCount+"\n");
						subRoutineBuffCount++;
						
						sb.append("\t"+"PUSH"+"\t");
						sb.append("0,"+"\t");
						sb.append("GR2"+"\n");
						
						sb.append("\t"+"POP"+"\t");
						sb.append("GR2"+"\n");
						
						sb.append("\t"+"POP"+"\t");
						sb.append("GR1"+"\n");
						
						sb.append("\t"+"CALL"+"\t");
						sb.append("WRTSTR"+"\n");
						
						sb.append("\t"+"CALL"+"\t");
						sb.append("WRTLN"+"\n");
						
						
					}
					
					
					
					//::::
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

	public boolean substitutionTypeCheck(String varName, String substitution,String substitutionType) {
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
					if (variable[i][2].equals("integer")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public String varTypeCheck(String varName) {
		for (int i=0; i<100;i++) {
			if((variable[i][0]!=null)&&(variable[i][0].equals(scope))) {
				if (variable[i][1].equals(varName)) {
					return variable[i][2];
				}
			}
		}
		return null;
	}

	public boolean procedureCheck(String varName) {
		for (int i=0; i<100;i++) {
			if((variable[i][0]!=null)&&((variable[i][0].equals(varName))||(variable[i][1].equals(varName)))) return true;
		}
		return false;
	}

	public boolean arrayCheck(String varName) {
		for (int i=0; i<100;i++) {
			if((variable[i][0]!=null)&&(variable[i][0].equals(scope))) {
				if (variable[i][1].equals(varName)) {
					if ((variable[i][3]!=null)&&(variable[i][3].equals("array")))
						return true;
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
