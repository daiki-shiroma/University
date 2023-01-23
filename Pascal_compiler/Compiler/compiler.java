package enshud.s4.compiler;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import enshud.casl.CaslSimulator;

public class Compiler {
	String[] str = {};
	String[][] variable = new String[1000][6]; // scope name type array arrayindex value
	String scope = "global";
	String procname;
	String eachprocess = "";
	StringBuilder sb = new StringBuilder();
	StringBuilder sbsub = new StringBuilder();
	StringBuilder sbForProc = new StringBuilder();
	StringBuilder subRoutineBuff = new StringBuilder();
	StringBuilder sbForScope= new StringBuilder();
	StringBuilder sbTemp= new StringBuilder();
	int index = 0;
	int indexTemp = 0;
	int errorType = 0;
	int booleanType = 0;
	int arrayreferFlag = 0;
	int arrayreferFlagRight=0;
	int subRoutineBuffCount = 0;
	int [] arraypointstack = {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
	int formervarStackPoint=0;
	int whilecount = -1;
	int ifcount = -1;
	int comparisonOperator = 0;
	int sassignFlag = 0;
	int notflag = 0;
	int procCounter = 0;
	int procflag = 0;
	int ifFlag = 0;
	int whileFlag = 0;
	int sassignForPop = 0;
	int addrOfArgument = 0;
	int argmentflag=0;
	int procintro=0;
	int procToBracket=0;
	int conditionalFlag=0;
	int varFlag=0;
	int minusFlag = 0;
	int bracketTocalculate=0;
	int bracketcouter=0;
	int comparisonOperatorFlag=0;	
	int andCounter=0;	
	int specialCoditonalFlag=0;	
	List<String> Operatorlist = new ArrayList<String>();

	public static void main(final String[] args) {
		// Compilerを実行してcasを生成する
		new Compiler().run("data/ts/normal16.ts", "tmp/out.cas");

		// 上記casを，CASLアセンブラ & COMETシミュレータで実行する
		CaslSimulator.run("tmp/out.cas", "tmp/out.ans");
	}

	public void run(final String inputFileName, final String outputFileName) {
		int flag = 0;
		try {
			File f = new File(inputFileName);
			File fileExist = new File("tmp/out.cas");
			if (fileExist.exists())
				fileExist.delete();

			Scanner scanner = new Scanner(f);
			str = scanner.nextLine().split("\t");

			if (str[1].equals("SPROGRAM")) {
				str = scanner.nextLine().split("\t");

				if (str[1].equals("SIDENTIFIER")) {
					str = scanner.nextLine().split("\t");

					if (str[1].equals("SSEMICOLON")) {

						while (scanner.hasNextLine()) {

							str = scanner.nextLine().split("\t");

							if ((str[1].equals("SEND")) && (!scope.equals("global"))) {
								sbForProc.append("\t" + "RET" + "\n");
								scope = "global";
							}

							if (str[1].equals("SVAR")) {
								if (!SVAR(scanner))
									break;
							}
							if (str[1].equals("SIDENTIFIER")) {
								if (!SIDENTIFIER(scanner, scope))
									break;
							}
							if (str[1].equals("SWRITELN")) {
								if (!SWRITELN(scanner))
									break;
							}
							if (str[1].equals("SWHILE")) {
								if (!SWHILE(scanner))
									break;
							}
							if (str[1].equals("SIF")) {
								if (!SIF(scanner))
									break;
							}
							if (str[1].equals("SPROCEDURE")) {
								if (!SPROCEDURE(scanner))
									break;
								procintro=0;
							}
							if (str[1].equals("SDOT")) {
								sb.append("\t" + "RET" + "\n");
								sb.append(sbForProc);
								flag = 1;
								grammerCorrect();
							}
						}
					}
				}
			}
			if (flag != 1)
				grammerError(str, errorType);
			scanner.close();
		} catch (IOException e) {
			System.err.println("File not found");
		}

		try {
			FileWriter file = new FileWriter("tmp/out.cas");
			PrintWriter pw = new PrintWriter(new BufferedWriter(file));
			sbsub.setLength(0);	
			sbsub.append("CASL\t" +"START\t" +"BEGIN\n");	
			sbsub.append("BEGIN\t" +"LAD\t" +"GR6,\t"+"0\n");	
			sbsub.append("\t" +"LAD\t" +"GR7,\t"+"LIBBUF\n");	
			pw.print(sbsub);

			int countVarlist = 0;
			for (int i = 0; variable[i][0] != null; i++)
				countVarlist = i + 1;

			sb.append("VAR\t" +"DS\t" +countVarlist+"\n");	
			pw.print(sb);

			subRoutineBuff.append("LIBBUF\t" +"DS\t" +"256\n");	
			subRoutineBuff.append("\t" +"END" +"\n");
			pw.print(subRoutineBuff);

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
			pw.close();
		} catch (IOException e) {
			System.err.println("cannot make cas file");
		}
	}

	public boolean SVAR(Scanner scanner) {
		varFlag=1;
		str = scanner.nextLine().split("\t");
		if (str[1].equals("SIDENTIFIER")) {
			if (SIDENTIFIER(scanner, scope)) {
				varFlag=0;
				return true;
			}
		}
		return false;
	}

	public boolean SIDENTIFIER(Scanner scanner, String scope) {
		int flag = 0;
		int arrayflag = 0;
		int j=0;
		String temp, initTemp;
		temp = str[0];
		variable[index][1] = temp;
		initTemp = temp;

		if (procedureCheck(temp)) {
			procflag = 1;
			procname = temp;
			argmentflag=1; 
		}

		if (arrayCheck(temp)) {
			arrayflag = 1;
			arrayreferFlag = 1;
		}

		for (int i = 0; variable[i][0] != null; i++) {
			if (scope.equals(variable[i][0]) & str[0].equals(variable[i][1])) {
				if (arrayflag != 1) {
					if (varFlag==0) {
						if(comparisonOperatorFlag==1){	
							sb.append("\t" + "LD" + "\t"+"GR2," + "\t"+"=" + i + "\n");	
							sb.append("\t" + "LD" + "\t"+"GR1," + "\t"+"VAR," + "\t"+"GR2" + "\n");	
							sb.append("\t" + "PUSH"+"\t" + "0," + "\t"+"GR1" + "\n");	
						}	
						else {
							sbsub.append("\t" + "LD" + "\t"+"GR2," + "\t"+"=" + i + "\n");
							sbsub.append("\t" + "POP" + "\t"+"GR1" + "\n");
							sbsub.append("\t" + "ST" + "\t"+"GR1," + "\t"+"VAR," + "\t"+"GR2" + "\n");
						}
					}
				} else {
					if (i>0) i--;
					arrayPointer(i, "push");
				}	
				break;
			}
			else if (!scope.equals("global") & variable[i][0].equals("global")
					& str[0].equals(variable[i][1]) && variableSameName(str[0]) == 1) {

				if (arrayflag != 1) {
					if (varFlag==0) {
						if(comparisonOperatorFlag==1){	
							sb.append("\t" + "LD" + "\t"+"GR2," + "\t"+"=" + i + "\n");	
							sb.append("\t" + "LD" + "\t"+"GR1," + "\t"+"VAR," + "\t"+"GR2" + "\n");	
							sb.append("\t" + "PUSH" + "\t"+"0," + "\t"+"GR1" + "\n");	
						}	
						else {
							sbsub.append("\t" + "LD" + "\t"+"GR2," + "\t"+"=" + i + "\n");
							sbsub.append("\t" + "POP" + "\t"+"GR1" + "\n");
							sbsub.append("\t" + "ST" + "\t"+"GR1," + "\t"+"VAR," + "\t"+"GR2" + "\n");
						}
					}
				} else {
					if (i>0) i--;
					arrayPointer(i, "push");
				}
				break;
			}
		}

		str = scanner.nextLine().split("\t");

		if (str[1].equals("SCOMMA")) {
			while (true) {
				if (flag == 0) {
					if (str[1].equals("SCOMMA")) {
						if (!temp.equals(",")) {
							variable[index][1] = temp;
							index++;
						}
						str = scanner.nextLine().split("\t");
						temp = str[0];
						flag = 1;
					} else
						return false;
				}
				if (flag == 1) {
					if (str[1].equals("SIDENTIFIER")) {
						if (!temp.equals(",")) {
							variable[index][1] = temp;
							index++;
						}
						str = scanner.nextLine().split("\t");
						temp = str[0];
						flag = 0;
					} else
						return false;
				}
				if (flag == 0 && str[1].equals("SCOLON")) 
					break;
			}
		}

		if (str[1].equals("SLPAREN") || str[1].equals("SLBRACKET")) {
			sassignForPop = 1;
			if(procflag==1)procToBracket=1;

			if (!Brackets(scanner))	
				return false;
			else {
				procToBracket=0;
				arrayflag = 0;
				arrayreferFlag = 0;
				sassignForPop = 0;
				if (str[1].equals("SSEMICOLON")) {
					if (argmentflag==1){
						if (procintro==0) {
							sbForScope.append("\t" + "CALL" + "\t"+"PROC"+whichProcedureCall(procname)+"\n");	
							selectStringbuilder();
						}
					}
					procflag=0;
					argmentflag=0;
					scope = "global";
					return true;
				} else
					str = scanner.nextLine().split("\t");
			}
		}

		if (str[1].equals("SCOLON")) {
			if (!temp.equals(":")) {
				if (varDuplicationCheck(temp)) {
					variable[index][1] = temp;
					index++;
				} else {
					errorType = 1;
					return false;
				}
			}
			temp = str[0];
			str = scanner.nextLine().split("\t");

			if (str[1].equals("SINTEGER") || str[1].equals("SCHAR") || str[1].equals("SBOOLEAN")) {
				temp = str[0];
				for (int i = indexTemp; i < index; i++) {
					variable[i][2] = temp;
					if (scope.equals("global"))
						variable[i][0] = "global";
					else
						variable[i][0] = scope;
				}
				scope = "global";
				indexTemp = index;

				str = scanner.nextLine().split("\t");
				if (str[1].equals("SSEMICOLON")) 	
					return true;
			}
			else if (str[1].equals("SARRAY")) {
				str = scanner.nextLine().split("\t");

				if (str[1].equals("SLBRACKET")) {
					str = scanner.nextLine().split("\t");

					if (str[1].equals("SCONSTANT")) {

						String arraytopnum = str[0]; 

						str = scanner.nextLine().split("\t");

						if (str[1].equals("SRANGE")) {
							str = scanner.nextLine().split("\t");

							if (str[1].equals("SCONSTANT")) {

								String arraybotomnum = str[0]; 

								str = scanner.nextLine().split("\t");

								if (str[1].equals("SRBRACKET")) {
									str = scanner.nextLine().split("\t");

									if (str[1].equals("SOF")) {
										str = scanner.nextLine().split("\t");

										if (str[1].equals("SINTEGER") || str[1].equals("SCHAR")
												|| str[1].equals("SBOOLEAN")) {
											temp = str[0];

											index = index + Integer.parseInt(arraybotomnum)
											- Integer.parseInt(arraytopnum);

											j = Integer.parseInt(arraytopnum);

											for (int i = indexTemp; i < index; i++) {
												variable[i][4] = String.valueOf(j);
												variable[i][3] = "array";
												variable[i][2] = temp;
												variable[i][1] = initTemp;
												if (scope.equals("global"))
													variable[i][0] = "global";
												else
													variable[i][0] = scope;
												j++;
											}
											scope = "global";
											indexTemp = index;
											str = scanner.nextLine().split("\t");
											if (str[1].equals("SSEMICOLON")) 
												return true;
										}
									}
								}
							}
						}
					}
				}
			}
			else return false;
		}

		else if (str[1].equals("SASSIGN")) { 
			sassignFlag = 1;
			if (arrayflag == 1) {
				errorType = 1;
				return false;
			}
			str = scanner.nextLine().split("\t");

			String substitution = str[0];
			String substitutionType = str[1];
			if (str[1].equals("SBOOLEAN") || str[1].equals("STRUE") || str[1].equals("SFALSE")
					|| str[1].equals("SSTRING")) {

				if (str[1].equals("STRUE")) {
					sbForScope.append("\t" + "PUSH" + "\t"+"#0000" + "\n");
				}
				if (str[1].equals("SFALSE")) {
					sbForScope.append("\t" + "PUSH" + "\t"+"#FFFF" + "\n");
				}
				if (str[1].equals("SSTRING")) {
					sbForScope.append("\t" + "LD" + "\t"+"GR1, " + "\t"+"=" + str[0] + "\n");
					sbForScope.append("\t" + "PUSH" + "\t"+"0, " + "\t"+"GR1" + "\n");
				}
				selectStringbuilder();

				if (!temp.equals(":=")) {
					if (substitutionTypeCheck(temp, substitution, substitutionType)) {} 
					else {
						errorType = 1;
						return false;
					}
				}
				temp = str[0];
				str = scanner.nextLine().split("\t");

				if (str[1].equals("SSEMICOLON")) {
					inputsbsubTosbForScope();
					sassignFlag=0;
					return true;
				}
			} 
			else {
				if (str[1].equals("SIDENTIFIER")) {
					String varType = varTypeCheck(temp);
					if ((varType != null) && (varType.equals("integer") || (varType.equals("char")))) {
						if (temp.equals(substitution)) {}
						else if (substitutionTypeCheck(temp, substitution, varType)) {} 
						else {
							errorType = 1;
							return false;
						}
					}
				}

				if (calculation(scanner)) {
					if (comparisonOperator>0) comparisonOperatorFlag=1;
					if (comparisonOperatorFlag==0) 
						inputsbsubTosbForScope();
					sassignFlag=0;
					return true;
				}
			}
		}

		else if (str[1].equals("SSEMICOLON")) {
			if (procedureCheck(temp)) {
				sb.append("\t" + "CALL" + "\t"+"PROC"+whichProcedureCall(procname)+"\n");
				scope = "global"; 
				return true;
			}
			else if (comparisonOperatorFlag==1) {	
				selectComparisonOperator();	
				inputsbsubTosbForScope();
				sassignFlag=0;	
				comparisonOperatorFlag=0;	
				return true;	
			}
			else errorType = 1;
		}
		return false;
	}

	public boolean calculation(Scanner scanner) {
		int count=0;
		int flag = 0;
		int integerType = 0;
		int charType = 0;
		int stringType = 0;
		int booleanType = 0;
		int operatortype = 0;
		int formerOperatortype=0;
		int formerMinusFlag=0;	
		int minusToBracket=0;	
		int loopCounter=0;
		int arrayreferFlagRightNotOperate=0;
		int arrayreferFlagRightNotOperateFlag=0;
		if (str[1].equals("SMINUS"))minusToBracket=1;
		if (bracketTocalculate==0)minusFlag=0;

		while (scanner.hasNextLine()) {
			loopCounter++;
			arrayreferFlagRightNotOperateFlag=0;

			if (str[1].equals("SLPAREN") || (str[1].equals("SLBRACKET"))) {
				if (conditionalFlag==1)Operatorlist.add(str[0].toUpperCase());
				while (true) {
					flag = 1;	
					if (minusToBracket==1 && loopCounter==2) 
						push0ForMinus(minusFlag);	

					formerMinusFlag=minusFlag;	
					minusFlag=0;
					if (Brackets(scanner)) {	
						if((str[1].equals("SRBRACKET")))	
							arrayreferFlagRight=0;

						minusFlag=formerMinusFlag;

						str = scanner.nextLine().split("\t");		

						if(arrayreferFlagRightNotOperate==1 && count>1 ) {
							arrayreferFlagRightNotOperateFlag=1;
							if (operatortype>=3) {
								changeResult(operatortype, minusFlag); 
							}
							else {
								changeResult(formerOperatortype, minusFlag); 
								formerOperatortype=operatortype;
							}
							arrayreferFlagRightNotOperate=0;
						}
					} else
						return false;

					minusFlag = 2;
					if (str[1].equals("SLPAREN") || (str[1].equals("SLBRACKET"))) {
						if (conditionalFlag==1)Operatorlist.add(str[0].toUpperCase());
						continue;
					}
					else
						break;
				}
			}

			if ((flag == 1)
					&& (str[1].equals("SSEMICOLON") || (str[1].equals("SRPAREN")) || (str[1].equals("SRBRACKET")))) {

				if (str[1].equals("SSEMICOLON") || (str[1].equals("SRPAREN"))){
					if (str[1].equals("SRPAREN")) {
						if (formerOperatortype!=0 & formerOperatortype!=operatortype) {	
							changeResult(operatortype, minusFlag);	
						}	
					}
					if (formerOperatortype==0) changeResult(operatortype, minusFlag); 
					else changeResult(formerOperatortype, minusFlag); 
					arrayreferFlagRight=0;
				}
				if (str[1].equals("SRBRACKET")) 	
					arrayreferFlagRight=0;
				return true;
			}

			if (str[1].equals("SCONSTANT")) {

				if (operatortype==0) 
					changeResult(operatortype, minusFlag); 

				else if (operatortype==formerOperatortype & operatortype > 0) 
					changeResult(0, minusFlag); 

				else if (operatortype>=3) 
					changeResult(operatortype, minusFlag); 

				integerType = 1;
				if ((integerType + charType + stringType + booleanType) > 1) {
					errorType = 1;
					break;
				}

				str = scanner.nextLine().split("\t");

				minusFlag = 2;
				if (str[1].equals("SSEMICOLON") || (str[1].equals("SRPAREN")) || (str[1].equals("SRBRACKET"))) {
					if (formerOperatortype==0) 
						changeResult(operatortype, minusFlag); 
					else 
						changeResult(formerOperatortype, minusFlag); 

					minusFlag=0;
					return true;
				}
			}

			else if (str[1].equals("SIDENTIFIER")) {

				if (sassignFlag==1 || conditionalFlag==1) {
					if (arrayCheck(str[0]) && arrayreferFlagRight==0) {
						push0ForMinus(minusFlag);
						arrayreferFlagRight = 1;
						arrayPointerPush();
					}
				}

				if (arrayreferFlagRight==0) {

					if (operatortype==0) 
						changeResult(operatortype, minusFlag); 

					else if (operatortype==formerOperatortype & operatortype > 0) 	
						changeResult(0, minusFlag); 

					else if (operatortype==3 || operatortype== 4) 
						changeResult(operatortype, minusFlag); 
				}
				else arrayreferFlagRightNotOperate=1;

				String temp = varTypeCheck(str[0]);
				if (temp != null) {
					if (temp.equals("integer"))
						integerType = 1;
					else if (temp.equals("char"))
						charType = 1;
					else if (temp.equals("boolean")) {
						booleanType = 1;
						this.booleanType = 1;
					}
				}

				if ((integerType + charType + stringType + booleanType) > 1) {
					errorType = 1;
					break;
				}
				minusFlag = 2;
				str = scanner.nextLine().split("\t");

				if (str[1].equals("SSEMICOLON") || (str[1].equals("SRPAREN")) || (str[1].equals("SRBRACKET"))) {
					if (conditionalFlag==1) {	
						if (Operatorlist.size()>0) {	
							specialCoditonalFlag=1;	
							selectLogicalOperator(Operatorlist.get(Operatorlist.size()-1),-1);	
							if (Operatorlist.size()>0)Operatorlist.remove(Operatorlist.size()-1);	
							if (Operatorlist.size()>0)Operatorlist.remove(Operatorlist.size()-1);	
							if (Operatorlist.size()>0) {	
								if (Operatorlist.get(Operatorlist.size()-1).equals("NOT")) {	
									notOperator(-1);	
									if (Operatorlist.size()>0)Operatorlist.remove(Operatorlist.size()-1);	
								}	
							}	
						}	
					}	

					if (formerOperatortype==0) 
						changeResult(operatortype, minusFlag); 
					else 
						changeResult(formerOperatortype, minusFlag); 
					minusFlag=0;
					return true;
				}
			}

			else if (str[1].equals("SSTRING")) {
				if (conditionalFlag==1) 
					changeResult(0, minusFlag);
				stringType = 1;
				if ((integerType + charType + stringType + booleanType) > 1) {
					errorType = 1;
					break;
				}
				minusFlag = 2;
				str = scanner.nextLine().split("\t");

				if (str[1].equals("SSEMICOLON") || (str[1].equals("SRPAREN")) || (str[1].equals("SRBRACKET"))) {
					if (conditionalFlag==1) {	
						if (Operatorlist.size()>0) {	
							specialCoditonalFlag=1;	
							selectLogicalOperator(Operatorlist.get(Operatorlist.size()-1),-1);	
							if (Operatorlist.size()>0)Operatorlist.remove(Operatorlist.size()-1);	
							if (Operatorlist.size()>0) {	
								if (Operatorlist.get(Operatorlist.size()-1).equals("NOT")) {	
									comparisonOperatorFlag=1;	
									selectComparisonOperator();	
									comparisonOperatorFlag=0;	
									notOperator(-1);	
								}	
								if (Operatorlist.size()>0)Operatorlist.remove(Operatorlist.size()-1);	
							}		
							if (Operatorlist.size()>0) {	
								if (Operatorlist.get(Operatorlist.size()-1).equals("NOT")) {	
									notOperator(-1);	
									if (Operatorlist.size()>0)Operatorlist.remove(Operatorlist.size()-1);	
								}	
							}	
						}	
					}	
					minusFlag=0;
					return true;
				}
			}

			else if (str[1].equals("STRUE")) {
				sb.append("\t" + "PUSH" + "\t"+"#0000" + "\n");
				booleanType = 1;
				if ((integerType + charType + stringType + booleanType) > 1) {
					errorType = 1;
					break;
				}
				minusFlag = 2;
				str = scanner.nextLine().split("\t");
				if (str[1].equals("SSEMICOLON") || (str[1].equals("SRPAREN")) || (str[1].equals("SRBRACKET"))) {
					minusFlag=0;
					return true;
				}
			}

			else if (str[1].equals("SPLUS") || str[1].equals("SMINUS") || str[1].equals("SSTAR")
					|| str[1].equals("SDIVD") || str[1].equals("SMOD")) {

				count++;

				if (str[1].equals("SPLUS")|| (str[1].equals("SMINUS"))) {
					if (str[1].equals("SPLUS")) 
						operatortype = 1;

					if (str[1].equals("SMINUS")) 
						operatortype = 2;
					if (minusFlag == 0) minusFlag = 1;

					if (formerOperatortype>0 && arrayreferFlagRightNotOperateFlag==0) changeResult(formerOperatortype,minusFlag); 
					formerOperatortype=operatortype;
				}

				if (str[1].equals("SSTAR") || (str[1].equals("SDIVD")) ||(str[1].equals("SMOD"))) {
					if (str[1].equals("SSTAR")) 
						operatortype = 3;

					if (str[1].equals("SDIVD")) 
						operatortype = 4;

					if (str[1].equals("SMOD")) 
						operatortype = 5;

					if (formerOperatortype==0) formerOperatortype=operatortype;
					else if (formerOperatortype>2 && arrayreferFlagRightNotOperateFlag==0) {
						changeResult(formerOperatortype,minusFlag); 
						formerOperatortype=operatortype;
					}
				}

				str = scanner.nextLine().split("\t");

				if (str[1].equals("SSEMICOLON") || (str[1].equals("SRPAREN")) || (str[1].equals("SRBRACKET"))) {

					if (str[1].equals("SSEMICOLON")) {
						sbsub.append("\t" + "POP" + "\t"+"GR1" + "\n");
						sbsub.append("\t" + "ST" + "\t"+"GR1," + "\t"+"VAR," + "\t"+"GR2" + "\n");
					}
					minusFlag=0;
					return true;
				}
			} else
				break;
		}
		// =, !=, <, <=, >=, >
		if (str[1].equals("SEQUAL") || str[1].equals("SNOTEQUAL") || str[1].equals("SLESS")
				|| str[1].equals("SLESSEQUAL") || str[1].equals("SGREATEQUAL") || str[1].equals("SGREAT")) {

			changeResult(operatortype, minusFlag); 

			inputcomparisonOperator();

			if (notflag == 1) {
				sbForScope.append("\t" + "POP" + "\t"+"GR1" + "\n");
				sbForScope.append("\t" + "XOR" + "\t"+"GR1," + "\t"+"=#FFFF" + "\n");
				sbForScope.append("\t" + "PUSH" + "\t"+"0," + "\t"+"GR1" + "\n");
				selectStringbuilder();
			}

			notflag = 0;
			arrayreferFlagRight=0;
			return true;

		}

		if (str[1].equals("STHEN") || str[1].equals("SDO")) {
			changeResult(operatortype, minusFlag); 
			this.booleanType = booleanType;
			arrayreferFlagRight=0;
			return true;
		}

		if (str[1].equals("STRUE") || str[1].equals("SFALSE") || str[1].equals("SAND") || str[1].equals("SOR")
				|| str[1].equals("SNOT")) {

			if (conditionalFlag==1 &(str[1].equals("SAND") || str[1].equals("SOR")|| str[1].equals("SNOT")))
				Operatorlist.add(str[0].toUpperCase());

			if (str[1].equals("STRUE")) {
				sbForScope.append("\t" + "PUSH" + "\t"+"#0000" + "\n");
				selectStringbuilder();
			}

			if (str[1].equals("SFALSE")) {
				sbForScope.append("\t" + "PUSH" + "\t"+"#FFFF" + "\n");
				selectStringbuilder();
			}
			arrayreferFlagRight=0;
			return true;
		}
		return false;
	}

	public boolean Brackets(Scanner scanner) {
		int functionArgument = 0;
		int flag = 0;
		int something = 0;
		int SLbracketflag = 0;
		int multiArgument=0;
		int firstArgument=0;
		String initArgment="";
		String temp;

		if (str[1].equals("SLBRACKET"))
			SLbracketflag = 1; // [

		str = scanner.nextLine().split("\t");
		while (true) {

			if (str[1].equals("SLPAREN") || (str[1].equals("SLBRACKET"))) {// SLBRACKET=[
				something = 1;

				if (str[1].equals("SLBRACKET")) 
					bracketcouter++;

				if (Brackets(scanner)) 
					str = scanner.nextLine().split("\t");
				else break;		
			}

			if (str[1].equals("SIDENTIFIER") || str[1].equals("SCONSTANT")) {
				minusFlag=2;

				if ((SLbracketflag == 1) && (str[1].equals("SIDENTIFIER")) && (!checkSCONSTANT(str[0]))) {
					errorType = 1;
					break;
				}
				something = 1;
				temp = str[0];
				initArgment=str[0];

				if (procflag == 1 && procintro==0) {
					addrOfArgument = 0;
					if (str[1].equals("SCONSTANT")) {
						sbForScope.append("\t" + "PUSH" + "\t"+Integer.parseInt(str[0]) + "\n");
						selectStringbuilder();
					}

					if (str[1].equals("SIDENTIFIER")) {

						if (!arrayCheck(str[0])) {
							for (int i = 0; variable[i][0] != null; i++) {
								if (scope.equals(variable[i][0]) & str[0].equals(variable[i][1])) {
									sbForScope.append("\t" + "LD" + "\t"+"GR2,\t" + "=" + i + "\n");
									sbForScope.append("\t" + "LD" + "\t"+"GR1," + "\t"+"VAR,\t"+"GR2\n");
									sbForScope.append("\t" + "PUSH" + "\t"+"0,\t"+"GR1\n");
									selectStringbuilder();
									break; 
								}
							}
						}	
					}
					for (int i = 0; variable[i][0] != null; i++) {
						if (procname.equals(variable[i][0])) {
							addrOfArgument = i; 
							firstArgument  = i;
							break;
						}
					}
				}

				else if (arrayreferFlag == 1) {
					if (str[1].equals("SCONSTANT")) {
						sbTemp.append("\t" + "PUSH" + "\t"+Integer.parseInt(str[0]) + "\n");
						selectsbForScopeORsbsub();
					}

					if (str[1].equals("SIDENTIFIER")) {

						if (!arrayCheck(str[0])) {
							for (int i = 0; variable[i][0] != null; i++) {
								if (scope.equals(variable[i][0]) & str[0].equals(variable[i][1])) {
									sbTemp.append("\t" + "LD" + "\t"+"GR2,\t" + "=" + i + "\n");
									sbTemp.append("\t" + "LD" + "\t"+"GR1," + "\t"+"VAR,\t"+"GR2\n");
									sbTemp.append("\t" + "PUSH" + "\t"+"0,\t"+"GR1\n");
									selectsbForScopeORsbsub();
									break;
								}
							}
						}
						else 
							arrayPointerPush();
					}
				}

				else 
					changeResult(0,0);

				str = scanner.nextLine().split("\t"); 

				if (str[1].equals("SCOMMA")) {
					multiArgument=1;
					functionArgument = 1;
					if (procflag == 1 && procintro==0) argmentflag=1; 
					while (true) {
						if (flag == 0) {
							if (str[1].equals("SCOMMA")) {
								addrOfArgument++;

								if(procintro==1) {
									if (!temp.equals(",")) { 
										variable[index][0] = scope; 
										variable[index][1] = temp;
										index++;
									}
								}
								str = scanner.nextLine().split("\t");
								temp = str[0];
								flag = 1;
							} else
								return false;
						}

						if (flag == 1) {
							if(procintro==1) {
								if (!temp.equals(",")) { 
									variable[index][0] = scope; 
									variable[index][1] = temp;
									index++;
								}
							}
							if (!calculation(scanner)) {
								if (str[1].equals("SCOLON") || str[1].equals("SCOMMA")) {
								} else
									break;
							} else
								str = scanner.nextLine().split("\t");

							flag = 0;
							temp = str[0];
						}
						if (flag == 0 && str[1].equals("SCOLON"))
							break;

						if (flag == 0 && (str[1].equals("SRPAREN") || str[1].equals("SRBRACKET"))) 
							break;

						if (flag == 0 && str[1].equals("SSEMICOLON")) 
							break;
					}
				}

				if (str[1].equals("SCOLON")) {
					functionArgument = 1;
					str = scanner.nextLine().split("\t");

					if (str[1].equals("SINTEGER") || str[1].equals("SCHAR") || str[1].equals("SBOOLEAN")) {
						temp = str[0];

						if (multiArgument==0) index++;

						if (procintro==1) {
							for (int i = indexTemp; i < index; i++) {
								if (multiArgument==0) {
									variable[i][1] = initArgment;
								}
								variable[i][2] = temp;
								if (scope.equals("global"))
									variable[i][0] = "global";
								else
									variable[i][0] = scope;
							}	
						}

						if (functionArgument == 0)
							scope = "global";

						indexTemp = index;
						str = scanner.nextLine().split("\t");
					} else
						break;
				}
			}

			if (str[1].equals("SPLUS") || str[1].equals("SMINUS") || str[1].equals("SSTAR") || str[1].equals("SDIVD")) {
				something = 1;
				bracketTocalculate=1;

				if (calculation(scanner)) {
					bracketTocalculate=0;
					if (str[1].equals("SSEMICOLON")) {} 
				} else
					break;
			}

			if (str[1].equals("SNOT")) {
				if (conditionalFlag==1)
					Operatorlist.add(str[0].toUpperCase());
				return true;
			}

			if (something == 0) 
				break;

			if (str[1].equals("STHEN") || str[1].equals("SDO")) 
				return true;

			if (str[1].equals("SSEMICOLON")) {
				if (procToBracket==1) {
					while(addrOfArgument>=firstArgument) {
						changeArgument();
						addrOfArgument--;
					}
				}
				procflag = 0;
				return true;
			}

			if (str[1].equals("STRUE") || str[1].equals("SFALSE") || str[1].equals("SAND") || str[1].equals("SOR")) {
				if (conditionalFlag==1 &(str[1].equals("SAND") || str[1].equals("SOR")))
					Operatorlist.add(str[0].toUpperCase());	
				return true;
			}

			if (str[1].equals("SRPAREN") || str[1].equals("SRBRACKET")) {
				if (procToBracket==1) {
					while(addrOfArgument>=firstArgument) {
						changeArgument();
						addrOfArgument--;
					}
				}
				procflag = 0;

				if (sassignFlag==0) {
					if (arrayreferFlag == 1 &  str[1].equals("SRBRACKET")) {
						if (conditionalFlag==1) {
							sb.append("\t" + "POP" + "\t"+"GR2" + "\n");
							sb.append("\t" + "ADDA" + "\t"+"GR2," + "\t"+"=" + arrayPointer(0,"pop") + "\n");
							sb.append("\t" + "LD" + "\t"+"GR1," + "\t"+"VAR," + "\t"+"GR2" + "\n");
							sb.append("\t" + "PUSH" + "\t"+"0," + "\t"+"GR1" + "\n");
						}
						else {
							sbsub.append("\t" + "POP" + "\t"+"GR2" + "\n");
							sbsub.append("\t" + "ADDA" + "\t"+"GR2," + "\t"+"=" + arrayPointer(0,"pop") + "\n");

							if (sassignForPop == 1 && bracketcouter<=1) {
								sbsub.append("\t" + "POP" + "\t"+"GR1" + "\n");
								sbsub.append("\t" + "ST" + "\t"+"GR1," + "\t"+"VAR," + "\t"+"GR2" + "\n");
							}
							else if (bracketcouter>1){
								sbsub.append("\t" + "LD" + "\t"+"GR1," + "\t"+"VAR," + "\t"+"GR2" + "\n");
								sbsub.append("\t" + "PUSH" + "\t"+"0," + "\t"+"GR1" + "\n");
								bracketcouter--;
							}
						}
					}
					else if (arrayreferFlagRight == 1 & str[1].equals("SRBRACKET") & conditionalFlag==1){
						sbForScope.append("\t" + "POP" + "\t"+"GR2" + "\n");
						sbForScope.append("\t" + "ADDA" + "\t"+"GR2," + "\t"+"=" + arrayPointer(0,"pop") + "\n");
						sbForScope.append("\t" + "LD" + "\t"+"GR1," + "\t"+"VAR," + "\t"+"GR2" + "\n");
						sbForScope.append("\t" + "PUSH" + "\t"+"0," + "\t"+"GR1" + "\n");
						selectStringbuilder ();
					}
				}
				else {
					if (arrayreferFlag == 1 &  str[1].equals("SRBRACKET")) {
						sbForScope.append("\t" + "POP" + "\t"+"GR2" + "\n");
						sbForScope.append("\t" + "ADDA" + "\t"+"GR2," + "\t"+"=" + arrayPointer(0,"pop") + "\n");
						sbForScope.append("\t" + "LD" + "\t"+"GR1," + "\t"+"VAR," + "\t"+"GR2" + "\n");
						sbForScope.append("\t" + "PUSH" + "\t"+"0," + "\t"+"GR1" + "\n");
						selectStringbuilder ();
					}
					else if (arrayreferFlagRight == 1 &  str[1].equals("SRBRACKET")) {
						sbForScope.append("\t" + "POP" + "\t"+"GR2" + "\n");
						sbForScope.append("\t" + "ADDA" + "\t"+"GR2," + "\t"+"=" + arrayPointer(0,"pop") + "\n");
						sbForScope.append("\t" + "LD" + "\t"+"GR1," + "\t"+"VAR," + "\t"+"GR2" + "\n");
						sbForScope.append("\t" + "PUSH" + "\t"+"0," + "\t"+"GR1" + "\n");
						selectStringbuilder ();
					}
					arrayreferFlagRight=0;
				}
				return true;
			}

			if (str[1].equals("SEQUAL") || str[1].equals("SNOTEQUAL") || str[1].equals("SLESS")
					|| str[1].equals("SLESSEQUAL") || str[1].equals("SGREATEQUAL") || str[1].equals("SGREAT")) {

				inputcomparisonOperator();

				if (arrayreferFlagRight == 1 ) {
					sbForScope.append("\t" + "POP" + "\t"+"GR2" + "\n");
					sbForScope.append("\t" + "ADDA" + "\t"+"GR2," + "\t"+"=" + arrayPointer(0,"pop") + "\n");
					sbForScope.append("\t" + "LD" + "\t"+"GR1," + "\t"+"VAR," + "\t"+"GR2" + "\n");
					sbForScope.append("\t" + "PUSH" + "\t"+"0," + "\t"+"GR1" + "\n");
					selectStringbuilder ();
				}
				arrayreferFlagRight=0;
				return true;
			}
		}
		return false;
	}

	public boolean conditionalExpression(Scanner scanner,int count) {
		int brflag = 0;
		int loopcount = 0;
		int formercomparisonOperator=0;	
		String logicalOperator=null;	
		Operatorlist = new ArrayList<String>();
		conditionalFlag=1;
		notflag = 0;	

		while (true) {
			str = scanner.nextLine().split("\t");

			if (str[1].equals("SLPAREN") || str[1].equals("SLBRACKET")) {
				if (conditionalFlag==1)Operatorlist.add(str[0].toUpperCase());
				str = scanner.nextLine().split("\t");
				brflag++;
			}
			if (str[1].equals("SNOT")) {
				if (conditionalFlag==1)Operatorlist.add(str[0].toUpperCase());
				notflag = 1;
				str = scanner.nextLine().split("\t");
			}

			if (!calculation(scanner))
				break;

			if (str[1].equals("SEQUAL") || str[1].equals("SNOTEQUAL") || str[1].equals("SLESS")
					|| str[1].equals("SLESSEQUAL") || str[1].equals("SGREATEQUAL") || str[1].equals("SGREAT")) {
				if (formercomparisonOperator==0)formercomparisonOperator=comparisonOperator;

				str = scanner.nextLine().split("\t");

				if (str[1].equals("SDO") || str[1].equals("STHEN"))
					break;

				if (str[1].equals("SLPAREN") || str[1].equals("SLBRACKET")) 
					str = scanner.nextLine().split("\t");

				if (str[1].equals("SNOT")) 
					str = scanner.nextLine().split("\t");

				if (!calculation(scanner))
					break;
				loopcount++;
			}

			if (str[1].equals("STRUE") || str[1].equals("SFALSE"))
				str = scanner.nextLine().split("\t");

			if (brflag > 0 && (str[1].equals("SRPAREN"))) {
				str = scanner.nextLine().split("\t");
				
				if (Operatorlist.size()>0) {
					selectLogicalOperator(Operatorlist.get(Operatorlist.size()-1),-1);
					if (Operatorlist.size()>0)Operatorlist.remove(Operatorlist.size()-1);
					if (Operatorlist.size()>0)Operatorlist.remove(Operatorlist.size()-1);
					if (Operatorlist.size()>0) {
						if (Operatorlist.get(Operatorlist.size()-1).equals("NOT")) {
							notOperator(-1);
							if (Operatorlist.size()>0)Operatorlist.remove(Operatorlist.size()-1);	
						}
					}
				}
				brflag--;
				loopcount++;
			}

			if (str[1].equals("SAND") || str[1].equals("SOR")) {
				logicalOperator= str[0].toUpperCase();	
				comparisonOperatorFlag=1;
				loopcount++;
			}

			if ((brflag == 0) && (str[1].equals("SDO") || str[1].equals("STHEN"))) {
				comparisonOperatorFlag=0; 
				if (specialCoditonalFlag==1) {	
					if (comparisonOperator!=0)selectComparisonOperator();	
					else conditionalFinish(count);	
					specialCoditonalFlag=0;	
				}	
				else {	
					if (logicalOperator!=null) {	
						comparisonOperatorFlag=1;	
						selectComparisonOperator();	
						comparisonOperatorFlag=0;	
						selectLogicalOperator(logicalOperator,count);	
					}	
					else if(formercomparisonOperator!=comparisonOperator){	
						comparisonOperatorFlag=1;
						selectComparisonOperator();
						comparisonOperatorFlag=0;	
						comparisonOperator=formercomparisonOperator;	
						selectComparisonOperator();	
					}	
					else selectComparisonOperator();	
				}	

				if (loopcount == 0 && brflag == 0) {
					if (booleanType == 1) {
						booleanType = 0;
						conditionalFlag=0;
						comparisonOperatorFlag=0;
						return true;
					} else {
						errorType = 1;
						break;
					}
				} else {
					conditionalFlag=0;
					return true;
				}
			}
			loopcount++;
		}
		return false;
	}

	public boolean SWHILE(Scanner scanner) {
		whileFlag = 1;
		whilecount++;
		int initwhile=whilecount;
		int wtrue=whilecount;
		int endwhile=whilecount;

		sbForScope.append("LOOP" + initwhile + "\t"+"NOP" + "\n");
		selectStringbuilder ();

		if (conditionalExpression(scanner,whilecount)) {
			str = scanner.nextLine().split("\t");

			if (str[1].equals("SBEGIN")) {

				while (scanner.hasNextLine()) {

					if (whileFlag == 1) {
						sbForScope.append("WTRUE" + wtrue + "\t"+"NOP" + "\n");
						selectStringbuilder ();
						whileFlag = 0;
					}

					if ((str[1].equals("SWHILE")) || (str[1].equals("SIF")) || (str[1].equals("SWRITELN"))) {} 
					else  str = scanner.nextLine().split("\t");

					if (str[1].equals("SWHILE")) {
						if (!SWHILE(scanner))
							return false;
					}

					if (str[1].equals("SIF")) {
						if (!SIF(scanner))
							return false;
					}

					if (str[1].equals("SWRITELN")) {
						if (!SWRITELN(scanner))
							return false;
					}

					if (str[1].equals("SIDENTIFIER")) {
						if (!SIDENTIFIER(scanner, scope))
							return false;
					}

					if (str[1].equals("SEND")) {
						str = scanner.nextLine().split("\t");
						sbForScope.append("\t" + "JUMP" + "\t" + "LOOP" + initwhile + "\n");
						sbForScope.append("ENDLP" + endwhile + "\t"+"NOP" + "\n");
						selectStringbuilder ();
						break;
					}
				}

				if (str[1].equals("SSEMICOLON")) {
					whileFlag = 0;
					return true;
				}
			}
		}
		return false;
	}

	public boolean SIF(Scanner scanner) {
		ifFlag = 1;
		ifcount++;
		int itrueif=ifcount;
		int elseif=ifcount;
		int endif=ifcount;
		if (conditionalExpression(scanner,ifcount)) {
			str = scanner.nextLine().split("\t");

			if (str[1].equals("SBEGIN")) {
				while (scanner.hasNextLine()) {
					if (ifFlag == 1) {
						sbForScope.append("ITRUE" + itrueif + "\t"+"NOP" + "\n");
						selectStringbuilder ();
						ifFlag = 0;
					}
					str = scanner.nextLine().split("\t");

					if (str[1].equals("SIF")) {
						if (!SIF(scanner))
							return false;
					}

					if (str[1].equals("SWHILE")) {
						if (!SWHILE(scanner))
							return false;
					}

					if (str[1].equals("SWRITELN")) {
						if (!SWRITELN(scanner))
							return false;
					}

					if (str[1].equals("SIDENTIFIER")) {
						if (!SIDENTIFIER(scanner, scope))
							return false;
					}

					if (str[1].equals("SEND")) {
						str = scanner.nextLine().split("\t");
						break;
					}
				}

				sbForScope.append("\t" + "JUMP" + "\t"+"ENDIF" + endif + "\n");
				sbForScope.append("ELSE" + elseif + "\t"+"NOP" + "\n");
				selectStringbuilder ();

				if (str[1].equals("SSEMICOLON")) {
					sbForScope.append("ENDIF" + endif + "\t"+"NOP" + "\n");
					selectStringbuilder ();
					ifFlag = 0;
					return true;
				}

				else if (str[1].equals("SELSE")) {
					str = scanner.nextLine().split("\t");

					if (str[1].equals("SBEGIN")) {
						while (scanner.hasNextLine()) {
							str = scanner.nextLine().split("\t");

							if (str[1].equals("SIF")) {
								if (!SIF(scanner))
									return false;
							}

							if (str[1].equals("SWHILE")) {
								if (!SWHILE(scanner))
									return false;
							}

							if (str[1].equals("SWRITELN")) {
								if (!SWRITELN(scanner))
									return false;
							}

							if (str[1].equals("SIDENTIFIER")) {
								if (!SIDENTIFIER(scanner, scope))
									return false;
							}

							if (str[1].equals("SEND")) {
								str = scanner.nextLine().split("\t");
								break;
							}
						}

						if (str[1].equals("SSEMICOLON")) {
							sbForScope.append("ENDIF" + endif + "\t"+"NOP" + "\n");
							selectStringbuilder ();
							ifFlag = 0;
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public boolean SPROCEDURE(Scanner scanner) {
		procintro=1;
		sbForProc.append("PROC" + procCounter + "\t"+"NOP" + "\n");
		procCounter++;
		str = scanner.nextLine().split("\t");
		scope = str[0];

		if (str[1].equals("SIDENTIFIER")) {
			str = scanner.nextLine().split("\t");

			if (str[1].equals("SLPAREN")) {
				if (Brackets(scanner))
					str = scanner.nextLine().split("\t");
				else
					return false;
			}
			if (str[1].equals("SSEMICOLON")) {
				return true;
			}
		}
		return false;
	}

	public boolean SWRITELN(Scanner scanner) {
		int block = 0;
		int tempPoint=0;
		int countStringlength=0;
		String temp="";
		str = scanner.nextLine().split("\t");
		if (str[1].equals("SLPAREN")) {
			str = scanner.nextLine().split("\t");

			while (scanner.hasNextLine()) {
				if (str[1].equals("SIDENTIFIER") || str[1].equals("SSTRING")) {

					if (str[1].equals("SSTRING")) {
						subRoutineBuff.append("CHAR" + subRoutineBuffCount + "\t"+"DC" + "\t"+str[0] + "\n");
						countStringlength = str[0].length() - 2;
						sbForScope.append("\t" + "LD" + "\t"+"GR1," + "\t"+"=" + countStringlength + "\n");
						sbForScope.append("\t" + "PUSH" + "\t"+"0," + "\t"+"GR1" + "\n");
						sbForScope.append("\t" + "LAD" + "\t"+"GR2," + "\t"+"CHAR" + subRoutineBuffCount + "\n");
						sbForScope.append("\t" + "PUSH" + "\t"+"0," + "\t"+"GR2" + "\n");
						sbForScope.append("\t" + "POP" + "\t"+"GR2" + "\n");
						sbForScope.append("\t" + "POP" + "\t"+"GR1" + "\n");
						sbForScope.append("\t" + "CALL" + "\t"+"WRTSTR" + "\n");
						selectStringbuilder ();
						subRoutineBuffCount++;
						block = 1;
					}

					else if (str[1].equals("SIDENTIFIER")) {
						temp = varTypeCheck(str[0]);
						if (!arrayCheck(str[0])) {
							for (int i = 0; variable[i][0] != null; i++) {
								if (scope.equals(variable[i][0]) & str[0].equals(variable[i][1])) {
									arrayPointer(i, "push");
									break;
								}
								else if (!scope.equals("global") & variable[i][0].equals("global")
										& str[0].equals(variable[i][1]) && variableSameName(str[0]) == 1) {
									arrayPointer(i, "push");
									break;
								}
							}
							tempPoint=arrayPointer(0, "pop");
							sbForScope.append("\t" + "LD" + "\t"+"GR2," + "\t"+"=" + tempPoint + "\n");
							sbForScope.append("\t" + "LD" + "\t"+"GR1," + "\t"+"VAR," + "\t"+"GR2" + "\n");
							sbForScope.append("\t" + "PUSH" + "\t"+"0," + "\t"+"GR1" + "\n");
							sbForScope.append("\t" + "POP" + "\t"+"GR2" + "\n");
							selectStringbuilder ();

							temp = varTypeCheck(str[0]);
							if (temp == null)
								temp = variable[tempPoint][2];

							if (temp != null) {
								if (temp.equals("integer")) {
									sbForScope.append("\t" + "CALL" + "\t"+"WRTINT" + "\n");
									selectStringbuilder ();
								}

								else if (temp.equals("char")) {
									sbForScope.append("\t" + "CALL" + "\t"+"WRTCH" + "\n");
									selectStringbuilder ();
								} else {}
							}
							block = 1;
						}

						else {
							arrayreferFlag = 1;
							arrayPointerPush();
						}
					}

					str = scanner.nextLine().split("\t");

					if (str[1].equals("SLPAREN") || str[1].equals("SLBRACKET")) {
						if (!Brackets(scanner))
							return false;
						else
							str = scanner.nextLine().split("\t");

						if (scope.equals("global"))
							sb.append(sbsub);
						else
							sbForProc.append(sbsub);
						sbsub.setLength(0);
						arrayreferFlag = 0;
					}

					if (str[1].equals("SCOMMA")) {
						str = scanner.nextLine().split("\t");

						if (block != 1) {
							sbForScope.append("\t" + "LD" + "\t"+"GR1," + "\t"+"VAR," + "\t"+"GR2" + "\n");
							sbForScope.append("\t" + "PUSH" + "\t"+"0," + "\t"+"GR1" + "\n");
							sbForScope.append("\t" + "POP" + "\t"+"GR2" + "\n");

							if (temp != null) {	
								if (temp.equals("integer")) {	
									sbForScope.append("\t" + "CALL" + "\t"+"WRTINT" + "\n");	
								}	
								else if (temp.equals("char")) {	
									sbForScope.append("\t" + "CALL" + "\t"+"WRTCH" + "\n");	
								} else {}	
							}
							selectStringbuilder ();
						}
						block = 0;
					}
					if (str[1].equals("SRPAREN")) {
						if (block != 1) {
							sbForScope.append("\t" + "LD" + "\t"+"GR1," + "\t"+"VAR," + "\t"+"GR2" + "\n");
							sbForScope.append("\t" + "PUSH" + "\t"+"0," + "\t"+"GR1" + "\n");
							sbForScope.append("\t" + "POP" + "\t"+"GR2" + "\n");
							if (temp != null) {	
								if (temp.equals("integer")) 
									sbForScope.append("\t" + "CALL" + "\t"+"WRTINT" + "\n");	
								else if (temp.equals("char")) 
									sbForScope.append("\t" + "CALL" + "\t"+"WRTCH" + "\n");	
								else {}	
							}
							selectStringbuilder ();
						}
						sbForScope.append("\t" + "CALL" + "\t"+"WRTLN" + "\n");
						selectStringbuilder ();
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean varDuplicationCheck(String varName) {
		for (int i = 0; variable[i][0] != null; i++) {
			if (variable[i][0].equals(scope)){
				if (variable[i][1].equals(varName)) 
					return false;
			}
		}
		return true;
	}

	public boolean substitutionTypeCheck(String varName, String substitution, String substitutionType) {
		String varType = null;
		for (int i = 0; variable[i][0] != null; i++) {
			if (variable[i][0].equals(scope)) {
				if (variable[i][1].equals(varName)) {
					if (str[1].equals("SIDENTIFIER")) {
						for (int j = 0; variable[j][0] != null; j++) {
							if (variable[j][0].equals(scope)) {
								if (variable[j][1].equals(substitution)) {
									varType = variable[j][2];
									break;
								}
							}
						}
					}
					if (varType == null)
						varType = substitutionType;
					if (variable[i][2].equals(varType)) 
						return true;
					else if ((variable[i][2].equals("integer")) && (varType.equals("SCONSTANT"))) 
						return true;
					else if ((variable[i][2].equals("char")) && (varType.equals("SSTRING"))) 
						return true;
					else if ((variable[i][2].equals("boolean"))
							&& (varType.equals("STRUE") || varType.equals("SFALSE"))) 
						return true;
					else
						break;
				}
			}
		}
		return false;
	}

	public boolean checkSCONSTANT(String varName) {
		for (int i = 0; variable[i][0] != null; i++) {
			if ((variable[i][0].equals(scope))) {
				if (variable[i][1].equals(varName)) {
					if (variable[i][2].equals("integer")) 
						return true;
				}
			}
		}
		return false;
	}

	public String varTypeCheck(String varName) {
		for (int i = 0; variable[i][0] != null; i++) {
			if ((variable[i][0].equals(scope))) {
				if (variable[i][1].equals(varName)) 
					return variable[i][2];
			}
			else if (!scope.equals("global") & variable[i][0].equals("global")	
					& str[0].equals(variable[i][1])){	
				if (variable[i][1].equals(varName)) 	
					return variable[i][2];	
			}
		}
		return null;
	}

	public boolean procedureCheck(String procname) {
		for (int i = 0; variable[i][0] != null; i++) {
			if ((variable[i][0].equals(procname))) 
				return true;
		}
		return false;
	}

	public int whichProcedureCall(String procname) {
		int count=-1;
		String formerScope="global";
		for (int i = 0; variable[i][0] != null; i++) {
			if (formerScope.equals(variable[i][0])) {}
			else count++;

			if (variable[i][0].equals(procname)) break;
			formerScope=variable[i][0];
		}
		return count;
	}


	public boolean arrayCheck(String varName) {
		for (int i = 0; variable[i][0] != null; i++) {
			if ((variable[i][0].equals(scope))) {
				if (variable[i][1].equals(varName)) {
					if ((variable[i][3] != null) && (variable[i][3].equals("array"))) 
						return true;
				}
			}
			else {
				if (!scope.equals("global") & variable[i][0].equals("global")) { 
					if (variable[i][1].equals(varName)) {
						if ((variable[i][3] != null) && (variable[i][3].equals("array"))) 
							return true;
					}
				}
			}
		}
		return false;
	}

	int variableSameName(String varName) {
		int count = 0;
		for (int i = 0; variable[i][0] != null; i++) {
			if (variable[i][1].equals(varName)) 
				count++;

			if (variable[i][3]!=null) {
				if (count > 1 & variable[i][3].equals("array")) 
					count--;
			}
		}
		return count;
	}

	void push0ForMinus(int minusFlag) {
		if (minusFlag == 1) {
			sbTemp.append("\t" + "PUSH" + "\t"+"0" + "\n");
			selectsbForScopeORsbsub();
		}
		return;
	}

	void changeResult(int operatortype,int minusFlag) {
		if (str[1].equals("SIDENTIFIER")) {
			push0ForMinus(minusFlag);

			for (int i = 0; variable[i][0] != null; i++) {
				if (scope.equals(variable[i][0]) & str[0].equals(variable[i][1])) {

					if (sassignFlag == 1 || procflag==1 || conditionalFlag==1) {
						if  (!arrayCheck(str[0])) {
							sbForScope.append("\t" + "LD" + "\t"+"GR2," + "\t"+"=" + i + "\n");
							sbForScope.append("\t" + "LD" + "\t"+"GR1," + "\t"+"VAR," + "\t"+"GR2\n");
							sbForScope.append("\t" + "PUSH" + "\t"+"0," + "\t"+"GR1" + "\n");
							selectStringbuilder ();
						}
						else {
							arrayreferFlag=1;
							arrayPointerPush();
						}			
					}
					else {
						if (procintro==0) {
							sbsub.append("\t" + "LD" + "\t"+"GR2," + "\t"+"=" + i + "\n");
							sbsub.append("\t" + "LD" + "\t"+"GR1," + "\t"+"VAR," + "\t"+"GR2\n");
							sbsub.append("\t" + "PUSH" + "\t"+"0," + "\t"+"GR1" + "\n");
						}
					}
					break;
				}

				else if (!scope.equals("global") & variable[i][0].equals("global")
						& str[0].equals(variable[i][1]) && variableSameName(str[0]) == 1) {

					sbTemp.append("\t" + "LD" + "\t"+"GR2," + "\t"+"=" + i + "\n");
					sbTemp.append("\t" + "LD" + "\t"+"GR1," + "\t"+"VAR," + "\t"+"GR2\n");
					sbTemp.append("\t" + "PUSH" + "\t"+"0," + "\t"+"GR1" + "\n");
					selectsbForScopeORsbsub();
					break;
				}
			}
		}

		else  if (str[1].equals("SCONSTANT")){
			push0ForMinus(minusFlag);
			sbTemp.append("\t" + "PUSH" + "\t"+Integer.parseInt(str[0])+ "\n");
			selectsbForScopeORsbsub();
		}

		else if (str[1].equals("SSTRING")) {	
			sbTemp.append("\t" + "LD" + "\t"+"GR1," + "\t"+"="+str[0] + "\n");	
			sbTemp.append("\t" + "PUSH" + "\t"+"0," + "\t"+"GR1" + "\n");
			selectsbForScopeORsbsub();
		}

		if (operatortype == 0) {}

		else {
			sbTemp.append("\t" + "POP" + "\t"+"GR2" + "\n");
			sbTemp.append("\t" + "POP" + "\t"+"GR1" + "\n");
			selectsbForScopeORsbsub();
			stringbuilderForOperatortype(operatortype);
		}
		return;
	}

	public void stringbuilderForOperatortype(int operatortype) {
		String operatorname="";
		String resultGR="";

		switch (operatortype) {
		case 1:
			operatorname="ADDA";
			resultGR="GR1";
			break;

		case 2:
			operatorname="SUBA";
			resultGR="GR1";
			break;

		case 3:
			operatorname="MULT";
			resultGR="GR2";
			break;

		case 4:
			operatorname="DIV";
			resultGR="GR2";
			break;

		case 5:
			operatorname="DIV";
			resultGR="GR1";
			break;
		}

		if (operatortype>=3) {
			sbTemp.append("\t" + "CALL" + "\t"+operatorname + "\n");
			sbTemp.append("\t" + "PUSH" + "\t"+"0," + "\t"+resultGR + "\n");
		}
		else {
			sbTemp.append("\t" + operatorname + "\t"+"GR1," + "\t"+"GR2" + "\n");
			sbTemp.append("\t" + "PUSH" + "\t"+"0," + "\t"+resultGR + "\n");
		}
		selectsbForScopeORsbsub();

		return;
	}

	public void selectComparisonOperator() {
		sbForScope.append("\t" + "POP" + "\t"+"GR2" + "\n");
		sbForScope.append("\t" + "POP" + "\t"+"GR1" + "\n");
		sbForScope.append("\t" + "CPA" + "\t"+"GR1," + "\t"+"GR2\n"); // 左 - 右	
		// =, <>(!=), <, <=, >=, >
		if (comparisonOperator==1)
			sbForScope.append("\t" + "JZE" + "\t");

		if (comparisonOperator==2)
			sbForScope.append("\t" + "JNZ" + "\t");

		if (comparisonOperator==3)
			sbForScope.append("\t" + "JMI" + "\t");

		if (comparisonOperator==4)
			sbForScope.append("\t" + "JMI" + "\t");

		if (comparisonOperator==5)
			sbForScope.append("\t" + "JPL" + "\t");

		if (comparisonOperator==6)
			sbForScope.append("\t" + "JPL" + "\t");			

		if (comparisonOperatorFlag!=1) {
			if (whileFlag == 1) {
				sbForScope.append("WTRUE" + whilecount + "\n");
				if (comparisonOperator==4 || comparisonOperator ==5 ) 
					sbForScope.append("\t" + "JZE" + "\t"+"WTRUE" + whilecount + "\n");
				sbForScope.append("\t" + "JUMP" + "\t"+"ENDLP" + whilecount + "\n");
			}

			if (ifFlag == 1) {
				sbForScope.append("ITRUE" + ifcount + "\n");
				if (comparisonOperator==4 || comparisonOperator ==5 ) 
					sbForScope.append("\t" + "JZE" + "\t"+"ITRUE" + ifcount + "\n");
				sbForScope.append("\t" + "JUMP" + "\t"+"ELSE" + ifcount + "\n");
			}
		}
		selectStringbuilder ();

		if (comparisonOperatorFlag==1) {	
			sb.append("TRUE"+andCounter + "\n");	
			sb.append("\t" + "JUMP" + "\t"+"FALSE"+andCounter + "\n");	
			sb.append("TRUE"+andCounter + "\t"+"LD\t" + "GR1," + "\t"+"=#0000" + "\n");	
			sb.append("\t" + "JUMP" + "\t"+"INPUT"+andCounter + "\n");	
			sb.append("FALSE"+andCounter + "\t"+"LD\t" + "GR1," + "\t"+"=#FFFF" + "\n");	
			sb.append("INPUT"+andCounter + "\t"+"PUSH\t" + "0,\t" +"GR1\n");	
			andCounter++;	
			comparisonOperatorFlag=0;	
		}	
		comparisonOperator=0;
		return;
	}

	void selectLogicalOperator(String type, int count) {

		if (type.equals("AND")||type.equals("OR")) {}
		else return;

		sbForScope.append("\tPOP" + "\tGR2\n");
		sbForScope.append("\tPOP" + "\tGR1\n");
		sbForScope.append("\t" + type + "\tGR1,\t"+"GR2\n");
		sbForScope.append("\tPUSH" + "\t0,"+"\tGR1\n");
		if (count>0) {
			sbForScope.append("\tPOP" + "\tGR1\n");
			sbForScope.append("\tCPA" + "\tGR1,"+"\t=#FFFF\n");
			sbForScope.append("\tJZE" + "\tELSE"+count+"\n");
		}
		selectStringbuilder ();
		return;
	}

	void notOperator(int count) {
		sbForScope.append("\tPOP" + "\tGR1" +"\n");
		sbForScope.append("\tXOR" + "\tGR1, "+"\t=#FFFF\n");
		sbForScope.append("\tPUSH" + "\t0,"+"\tGR1\n");
		if (count>0) {
			sbForScope.append("\tPOP" + "\tGR1\n");
			sbForScope.append("\tCPA" + "\tGR1,"+"\t=#FFFF\n");
			sbForScope.append("\tJZE" + "\tELSE"+count+"\n");
		}
		selectStringbuilder ();
		return; 
	}

	void conditionalFinish(int count) {
		sb.append("\tPOP" + "\tGR1" +"\n");
		sb.append("\tCPA" + "\tGR1,"+"\t=#FFFF"+"\n");
		sb.append("\tJZE" + "\tELSE"+count+"\n");
	}

	void changeArgument() {
		sbForScope.append("\t" + "LD" + "\t"+"GR2, " + "\t"+"=" + addrOfArgument + ";LD4\n");
		sbForScope.append("\t" + "POP" + "\t"+"GR1" + "\n");
		sbForScope.append("\t" + "ST" + "\t"+"GR1, " + "\t"+"VAR, " + "\t"+"GR2" + "\n");
		selectStringbuilder ();
		return;
	}

	int  arrayPointer (int arraypoint, String mode) { //push or pop
		int result=0;
		if (mode.equals("push")) {
			for (int i=0; ;i++) {
				if (arraypointstack[i]==-1) {
					arraypointstack[i]=arraypoint;
					result=-1;
					break;
				}
			}
		}
		else if (mode.equals("pop")) {
			for (int i=0; ;i++) {
				if (arraypointstack[i]==-1) {
					if(i==0) {
						result=arraypointstack[i];
						arraypointstack[i]=-1;
					}
					else {
						result=arraypointstack[i-1];
						arraypointstack[i-1]=-1;
					}
					break;
				}
			}
		}
		return result;
	}

	public void selectStringbuilder () {
		if (scope.equals("global")) 
			sb.append(sbForScope);
		else 
			sbForProc.append(sbForScope);
		sbForScope.setLength(0);	
		return;
	}

	public void selectsbForScopeORsbsub() {
		if (sassignFlag == 1 || procflag==1 || conditionalFlag==1) {
			sbForScope.append(sbTemp);
			selectStringbuilder ();
		}
		else 
			sbsub.append(sbTemp);
		sbTemp.setLength(0);	
	}

	public void inputsbsubTosbForScope () {
		sbForScope.append(sbsub);
		selectStringbuilder();
		sbsub.setLength(0);
		return;
	}

	public void inputcomparisonOperator() {

		if (str[1].equals("SEQUAL")) comparisonOperator = 1;	

		if (str[1].equals("SNOTEQUAL")) comparisonOperator = 2;	

		if (str[1].equals("SLESS")) 	comparisonOperator = 3;	

		if (str[1].equals("SLESSEQUAL")) 	comparisonOperator = 4;	

		if (str[1].equals("SGREATEQUAL")) comparisonOperator = 5;	

		if (str[1].equals("SGREAT")) 		comparisonOperator = 6;

		return;
	}

	public void arrayPointerPush(){
		for (int j = 0; variable[j][0] != null; j++) {
			if (scope.equals(variable[j][0]) & str[0].equals(variable[j][1])) {
				if (j>0)j--;
				arrayPointer(j,"push");
				break;
			}
			else if (!scope.equals("global") & variable[j][0].equals("global")
					& str[0].equals(variable[j][1])) {
				if (j>0)j--;
				arrayPointer(j,"push");
				break;
			}
		}
		return;
	}

	public void grammerCorrect() {
		System.out.println("OK");
	}

	public void grammerError(String[] str, int errorType) {
		if (errorType == 1)
			System.err.println("Semantic error: line " + str[3]);
		else
			System.err.println("Syntax error: line " + str[3]);
	}
}