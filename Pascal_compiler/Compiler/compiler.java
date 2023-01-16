package enshud.s4.compiler;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;

import enshud.casl.CaslSimulator;

public class Compiler {
	String[] str = {};
	String[][] variable = new String[100][6]; // scope name type array arrayindex value
	String scope = "global";
	int index = 0;
	int indexTemp = 0;
	int errorType = 0;
	int booleanType = 0;
	int arrayreferFlag = 0;
	int arrayreferFlagRight=0;
	String eachprocess = "";
	StringBuilder sb = new StringBuilder();
	StringBuilder sbsub = new StringBuilder();
	StringBuilder sbForProc = new StringBuilder();
	StringBuilder subRoutineBuff = new StringBuilder();
	int subRoutineBuffCount = 0;
	int varStackPoint = 0;
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
	String procname;

	public static void main(final String[] args) {
		// Compilerを実行してcasを生成する
		new Compiler().run("data/ts/normal19.ts", "tmp/out.cas");

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
					for (int i = 0; variable[i][0] != null; i++) {
						System.out.print(variable[i][0]+" ");
						System.out.print(variable[i][1]+" ");
						System.out.print(variable[i][2]+" ");
						System.out.print(variable[i][3]+" ");
						System.out.print(variable[i][4]+" ");
						System.out.println(variable[i][5]);
					}

				}
			}
			if (flag != 1)
				grammerError(str, errorType);
			scanner.close();
		} catch (IOException e) {
			System.err.println("File not found");
		}

		/// compiler

		try {
			// System.out.println("make out.cass is success");
			// FileWriterクラスのオブジェクトを生成する
			FileWriter file = new FileWriter("tmp/out.cas");
			// PrintWriterクラスのオブジェクトを生成する
			PrintWriter pw = new PrintWriter(new BufferedWriter(file));
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

			pw.print(sb);

			int countVarlist = 0;
			for (int i = 0; variable[i][0] != null; i++)
				countVarlist = i + 1;

			pw.print("VAR\t");
			pw.print("DS\t");
			pw.println(countVarlist);

			pw.print(subRoutineBuff);
			// addsubroutine

			pw.print("LIBBUF\t");
			pw.print("DS\t");
			pw.print("256\n");

			pw.print("\t" + "END" + "\n");

			// add lib.cas
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

			// add lib.cas

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
		String temp, initTemp;
		temp = str[0];
		variable[index][1] = temp;
		initTemp = temp;

		if (procedureCheck(temp)) {
			procflag = 1;
			procname = temp;
			argmentflag=1; //delete

		}
		
		
		if (arrayCheck(temp)) {
			arrayflag = 1;
			arrayreferFlag = 1;
		
		}
		
		

		for (int i = 0; variable[i][0] != null; i++) {
			if (scope.equals(variable[i][0]) & str[0].equals(variable[i][1])) {

				if (arrayflag != 1) {
					if (varFlag==0) {
						sbsub.append("\t" + "LD" + "\t");
						sbsub.append("GR2," + "\t");
						sbsub.append("=" + i + ";LD1\n");

						sbsub.append("\t" + "POP" + "\t");
						sbsub.append("GR1" + "\n");

						sbsub.append("\t" + "ST" + "\t");
						sbsub.append("GR1," + "\t");
						sbsub.append("VAR," + "\t");
						sbsub.append("GR2" + "\n");
					}

				} else
					varStackPoint = i - 1;
				break;
			}
			//add
			else if (!scope.equals("global") & variable[i][0].equals("global")
					& str[0].equals(variable[i][1]) && variableSameName(str[0]) == 1) {
				
				if (arrayflag != 1) {
					if (varFlag==0) {
						sbsub.append("\t" + "LD" + "\t");
						sbsub.append("GR2," + "\t");
						sbsub.append("=" + i + ";LD111\n");

						sbsub.append("\t" + "POP" + "\t");
						sbsub.append("GR1" + "\n");

						sbsub.append("\t" + "ST" + "\t");
						sbsub.append("GR1," + "\t");
						sbsub.append("VAR," + "\t");
						sbsub.append("GR2" + "\n");
					}
				} else
					varStackPoint = i - 1;
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
				if (flag == 0 && str[1].equals("SCOLON")) {
					break;
				}
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

					if (argmentflag==1){ //change
						if (procintro==0) {
							if (scope.equals("global")) {
								sb.append("\t" + "CALL" + "\t");
								sb.append("PROC");
								sb.append(whichProcedureCall(procname)+"\n");

								//sb.append(procCounter+"\n");
							}
							else {
								sbForProc.append("\t" + "CALL" + "\t");
								sbForProc.append("PROC");
								sbForProc.append(whichProcedureCall(procname)+"\n");
								//sbForProc.append(procCounter+"\n");
							}
						}

					}
					procflag=0;
					argmentflag=0;
					scope = "global"; // delete
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
				if (str[1].equals("SSEMICOLON")) {
					return true;
				}
			}

			else if (str[1].equals("SARRAY")) {
				str = scanner.nextLine().split("\t");

				if (str[1].equals("SLBRACKET")) {
					str = scanner.nextLine().split("\t");

					if (str[1].equals("SCONSTANT")) {

						String arraytopnum = str[0]; //

						str = scanner.nextLine().split("\t");

						if (str[1].equals("SRANGE")) {
							str = scanner.nextLine().split("\t");

							if (str[1].equals("SCONSTANT")) {

								String arraybotomnum = str[0]; //

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

											int j = Integer.parseInt(arraytopnum);

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
											if (str[1].equals("SSEMICOLON")) {
												return true;
											}
										}
									}
								}
							}
						}
					}
				}
			}

			else {
				return false;
			}
		}

		else if (str[1].equals("SASSIGN")) { // :=

			// := flag
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

				if (scope.equals("global")) {
					if (str[1].equals("STRUE")) {
						sb.append("\t" + "PUSH" + "\t");
						sb.append("#0000" + "\n");
					}

					if (str[1].equals("SFALSE")) {
						sb.append("\t" + "PUSH" + "\t");
						sb.append("#FFFF" + "\n");
					}

					if (str[1].equals("SSTRING")) {
						sb.append("\t" + "LD" + "\t");
						sb.append("GR1, " + "\t");
						sb.append("=" + str[0] + "\n");

						sb.append("\t" + "PUSH" + "\t");
						sb.append("0, " + "\t");
						sb.append("GR1" + "\n");
					}
				}

				else {
					if (str[1].equals("STRUE")) {
						sbForProc.append("\t" + "PUSH" + "\t");
						sbForProc.append("#0000" + "\n");
					}

					if (str[1].equals("SFALSE")) {
						sbForProc.append("\t" + "PUSH" + "\t");
						sbForProc.append("#FFFF" + "\n");
					}

					if (str[1].equals("SSTRING")) {
						sbForProc.append("\t" + "LD" + "\t");
						sbForProc.append("GR1, " + "\t");
						sbForProc.append("=" + str[0] + "\n");

						sbForProc.append("\t" + "PUSH" + "\t");
						sbForProc.append("0, " + "\t");
						sbForProc.append("GR1" + "\n");
					}
				}

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
					if (scope.equals("global"))
						sb.append(sbsub);
					else sbForProc.append(sbsub);

					sbsub.setLength(0);
					varStackPoint = 0;
					return true;
				}

			} 
			else {
				if (str[1].equals("SIDENTIFIER")) {
					String varType = varTypeCheck(temp);
					if ((varType != null) && (varType.equals("integer") || (varType.equals("char")))) {
						if (substitutionTypeCheck(temp, substitution, varType)) {} 
						else {
							errorType = 1;
							return false;
						}
					}
				}

				if (calculation(scanner)) {

					if (scope.equals("global"))
						sb.append(sbsub);
					else
						sbForProc.append(sbsub);


					sbsub.setLength(0);
					varStackPoint = 0;
					sassignFlag=0;
					return true;
				}
			}
		}

		else if (str[1].equals("SSEMICOLON")) {

			if (procedureCheck(temp)) {
				sb.append("\t" + "CALL" + "\t");
				sb.append("PROC");
				sb.append(whichProcedureCall(procname)+"\n");
				//sb.append(procCounter+"\n");
				scope = "global"; // delete
				return true;
			} else {
				errorType = 1;
			}

		}
		return false;
	}

	public boolean calculation(Scanner scanner) {
		if (bracketTocalculate==0)minusFlag=0;
		//else 
		int count=0;
		int flag = 0;
		int integerType = 0;
		int charType = 0;
		int stringType = 0;
		int booleanType = 0;
		int operatortype = 0;
		int formerOperatortype=0;
		int operatorFlag=0;
		int finishbracket=0;
	    int arrayreferFlagRightNotOperate=0;
	    
	     System.out.println(str[0]+"  "+str[3]);
		 System.out.println("minusFlag" +minusFlag+ "613");
		 
		while (scanner.hasNextLine()) {
			
			 
			if (str[1].equals("SLPAREN") || (str[1].equals("SLBRACKET"))) {

				while (true) {
					flag = 1;	
					
					System.out.println(str[0]+"  "+str[3]);
					 System.out.println("operatortype  "+operatortype);
					 System.out.println("formerOperatortype  "+formerOperatortype);
					 System.out.println("arrayreferFlag "+arrayreferFlag);
					 System.out.println("minusFlag" +minusFlag);
					 
					/*if (minusFlag == 1) {
						if (scope.equals("global")) {
							sb.append("\t" + "PUSH" + "\t");
							sb.append("0; " + "\n");
						} else {
							sbForProc.append("\t" + "PUSH" + "\t");
							sbForProc.append("0" + "\n");
						}

					}*/
					
					if (Brackets(scanner)) {	

						str = scanner.nextLine().split("\t");		
						
						 
						 if(arrayreferFlagRightNotOperate==1 && count>1 ) {
							 
							if (operatortype>=3) {
									changeResult(operatortype, minusFlag); //move?
								}
							else {
								changeResult(formerOperatortype, minusFlag); //move?
								formerOperatortype=operatortype;
							}
							 
						 }
						
					} else
						return false;
					
					
					
					minusFlag = 2;

					if (str[1].equals("SLPAREN") || (str[1].equals("SLBRACKET")))
						continue;
					else
						break;
				}
			}

			if ((flag == 1)
					&& (str[1].equals("SSEMICOLON") || (str[1].equals("SRPAREN")) || (str[1].equals("SRBRACKET")))) {
			
				 if (str[1].equals("SSEMICOLON")){
					// changeResult(operatortype, minusFlag); 
					// if (operatorFlag==0)changeResult(formerOperatortype, minusFlag);  // formerOperatortype=1,2 はSSEMICOLONで対応する必要あり
					 if (formerOperatortype==0) changeResult(operatortype, minusFlag); 
				     else changeResult(formerOperatortype, minusFlag); 
					 
					 arrayreferFlagRight=0;
					 
				 }
				 if (str[1].equals("SRBRACKET")) {
					 arrayreferFlagRight=0;
				 }
				return true;
			}


			if (str[1].equals("SCONSTANT")) {
				 
				if (operatortype==0) changeResult(operatortype, minusFlag); //move?
				
				else if (operatortype==formerOperatortype & operatortype > 0/*& count==1*/) {
					changeResult(0, minusFlag); //move?
				}
				
				else if (operatortype>=3) {
					changeResult(operatortype, minusFlag); //move?
				}

				integerType = 1;
				if ((integerType + charType + stringType + booleanType) > 1) {
					errorType = 1;
					break;
				}

				str = scanner.nextLine().split("\t");

				minusFlag = 2;
				if (str[1].equals("SSEMICOLON") || (str[1].equals("SRPAREN")) || (str[1].equals("SRBRACKET"))) {
					
					if (formerOperatortype==0) {
						changeResult(operatortype, minusFlag); 
					}
					else changeResult(formerOperatortype, minusFlag); 

					minusFlag=0;
					return true;
				}
			}

			else if (str[1].equals("SIDENTIFIER")) {
				
				if (sassignFlag==1 || conditionalFlag==1 /*delete*/) {
					if (arrayCheck(str[0]) && arrayreferFlagRight==0) {
						
						 System.out.println(str[0]+"  "+str[3]);
						 System.out.println("minusFlag" +minusFlag+ "727");
						
						if (minusFlag == 1) {
							if (scope.equals("global")) {
								sb.append("\t" + "PUSH" + "\t");
								sb.append("0; " + "\n");
							} else {
								sbForProc.append("\t" + "PUSH" + "\t");
								sbForProc.append("0" + "\n");
							}

						}
						
						arrayreferFlagRight = 1;
						for (int j = 0; variable[j][0] != null; j++) {
								if (scope.equals(variable[j][0]) & str[0].equals(variable[j][1])) {
									varStackPoint = j;
									varStackPoint--;
									break;
								}
								else if (!scope.equals("global") & variable[j][0].equals("global")
										& str[0].equals(variable[j][1])) {
									varStackPoint = j;
									varStackPoint--;
									break;
								}
							}
					}
				}
								
				 //here
				if (arrayreferFlagRight==0) {
					if (operatortype==0) changeResult(operatortype, minusFlag); //move?
					
					else if (operatortype==formerOperatortype & operatortype > 0/*& count==1*/) {
						changeResult(0, minusFlag); //move?
					}
					
					else if (operatortype==3 || operatortype== 4) {
						changeResult(operatortype, minusFlag); //move?
					}
				}
				else {
					arrayreferFlagRightNotOperate=1;
				}
                
				
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
					
					if (formerOperatortype==0) changeResult(operatortype, minusFlag); 
					else changeResult(formerOperatortype, minusFlag); 

					minusFlag=0;
					return true;
				}
			}

			else if (str[1].equals("SSTRING")) {
				stringType = 1;
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

			else if (str[1].equals("STRUE")) {

				sb.append("\t" + "PUSH" + "\t");
				sb.append("#0000" + "\n");
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

				if (str[1].equals("SPLUS")) {

					operatortype = 1;
					if (formerOperatortype>0)changeResult(formerOperatortype,minusFlag); 
					
					formerOperatortype=operatortype;
				}

				if (str[1].equals("SMINUS")) {
					if (minusFlag == 0) {
						minusFlag = 1;
					}
					
					operatortype = 2;
					if (formerOperatortype>0) changeResult(formerOperatortype,minusFlag); 
					
					formerOperatortype=operatortype;
				}

				if (str[1].equals("SSTAR")) {
					operatortype = 3;
					if (formerOperatortype==0) formerOperatortype=operatortype;
					
					 else if (formerOperatortype>2) {
						 changeResult(formerOperatortype,minusFlag); 
						 formerOperatortype=operatortype;
					 }
				}

				if (str[1].equals("SDIVD")) {
					operatortype = 4;
					if (formerOperatortype==0) formerOperatortype=operatortype;
					
				    else if (formerOperatortype>2) {
				    	changeResult(formerOperatortype,minusFlag); 
				    	formerOperatortype=operatortype;
				    }
				}

				if (str[1].equals("SMOD")) {
					operatortype = 5;
					if (formerOperatortype==0) formerOperatortype=operatortype;
					
					 else if (formerOperatortype>2) {
						 changeResult(formerOperatortype,minusFlag); 
						 formerOperatortype=operatortype;
					 }
				}

				str = scanner.nextLine().split("\t");

				if (str[1].equals("SSEMICOLON") || (str[1].equals("SRPAREN")) || (str[1].equals("SRBRACKET"))) {

					if (str[1].equals("SSEMICOLON")) {
						sbsub.append("\t" + "POP" + "\t");
						sbsub.append("GR1" + "\n");

						sbsub.append("\t" + "ST" + "\t");
						sbsub.append("GR1," + "\t");
						sbsub.append("VAR," + "\t");
						sbsub.append("GR2" + "\n");

					}
					minusFlag=0;
					return true;
				}
			} else
				break;
		}

		// =, !=, <, <=, >=, >

		if (str[1].equals("SEQUAL")) {
			
			 changeResult(operatortype, minusFlag); 
			 
			comparisonOperator = 1;
			if (notflag == 1) {

				if (scope.equals("global")) {
					sb.append("\t" + "POP" + "\t");
					sb.append("GR1" + "\n");

					sb.append("\t" + "XOR" + "\t");
					sb.append("GR1," + "\t");
					sb.append("=#FFFF" + "\n");

					sb.append("\t" + "PUSH" + "\t");
					sb.append("0," + "\t");
					sb.append("GR1" + "\n");
				} else {
					sbForProc.append("\t" + "POP" + "\t");
					sbForProc.append("GR1" + "\n");

					sbForProc.append("\t" + "XOR" + "\t");
					sbForProc.append("GR1," + "\t");
					sbForProc.append("=#FFFF" + "\n");

					sbForProc.append("\t" + "PUSH" + "\t");
					sbForProc.append("0," + "\t");
					sbForProc.append("GR1" + "\n");
				}
			}
			notflag = 0;
			arrayreferFlagRight=0;
			return true;
		}
		if (str[1].equals("SNOTEQUAL")) {
			 changeResult(operatortype, minusFlag); 
			comparisonOperator = 2;
			arrayreferFlagRight=0;
			return true;
		}
		if (str[1].equals("SLESS")) {
			 changeResult(operatortype, minusFlag); 
			comparisonOperator = 3;
			arrayreferFlagRight=0;
			return true;
		}
		if (str[1].equals("SLESSEQUAL")) {
			 changeResult(operatortype, minusFlag); 
			comparisonOperator = 4;
			arrayreferFlagRight=0;
			return true;
		}
		if (str[1].equals("SGREATEQUAL")) {
			 changeResult(operatortype, minusFlag); 
			comparisonOperator = 5;
			arrayreferFlagRight=0;
			return true;
		}
		if (str[1].equals("SGREAT")) {
			
			 changeResult(operatortype, minusFlag); 
			comparisonOperator = 6;
			arrayreferFlagRight=0;
			return true;
		}

		if (str[1].equals("STHEN") || str[1].equals("SDO")) {
			this.booleanType = booleanType;
			arrayreferFlagRight=0;
			return true;
		}

		if (str[1].equals("STRUE") || str[1].equals("SFALSE") || str[1].equals("SAND") || str[1].equals("SOR")
				|| str[1].equals("SNOT")) {

			if (str[1].equals("STRUE")) {
				if (scope.equals("global")) {
					sb.append("\t" + "PUSH" + "\t");
					sb.append("#0000" + "\n");
				} else {
					sbForProc.append("\t" + "PUSH" + "\t");
					sbForProc.append("#0000" + "\n");
				}
			}

			if (str[1].equals("SFALSE")) {
				if (scope.equals("global")) {
					sb.append("\t" + "PUSH" + "\t");
					sb.append("#FFFF" + "\n");
				} else {
					sbForProc.append("\t" + "PUSH" + "\t");
					sbForProc.append("#FFFF" + "\n");
				}
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
				if (Brackets(scanner))
					str = scanner.nextLine().split("\t");
				else
					break;
			}

			if (str[1].equals("SIDENTIFIER") || str[1].equals("SCONSTANT")) {
				minusFlag=2; //

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
						if (scope.equals("global")) {
							sb.append("\t" + "PUSH" + "\t");
							sb.append(Integer.parseInt(str[0]) + "\n");
						}

						else {
							sbForProc.append("\t" + "PUSH" + "\t");
							sbForProc.append(Integer.parseInt(str[0]) + "\n");
						}

					}

					if (str[1].equals("SIDENTIFIER")) {

						for (int i = 0; variable[i][0] != null; i++) {
							if (scope.equals(variable[i][0]) & str[0].equals(variable[i][1])) {

								if (scope.equals("global")) {
									sb.append("\t" + "LD" + "\t");
									sb.append("GR2,\t" + "=" + i + ";LD2\n");

									sb.append("\t" + "LD" + "\t");
									sb.append("GR1," + "\t");
									sb.append("VAR,\t");
									sb.append("GR2\n");

									sb.append("\t" + "PUSH" + "\t");
									sb.append("0,\t");
									sb.append("GR1\n");
									break; //maybe change
								}
								else {
									sbForProc.append("\t" + "LD" + "\t");
									sbForProc.append("GR2,\t" + "=" + i + ";LD2\n");

									sbForProc.append("\t" + "LD" + "\t");
									sbForProc.append("GR1," + "\t");
									sbForProc.append("VAR,\t");
									sbForProc.append("GR2; hhhh\n");

									sbForProc.append("\t" + "PUSH" + "\t");
									sbForProc.append("0,\t");
									sbForProc.append("GR1\n");
									break; //maybe change

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
						if(sassignFlag==1||(sassignFlag==0)& (conditionalFlag==1) ) {
							sb.append("\t" + "PUSH" + "\t");
							sb.append(Integer.parseInt(str[0]) + "\n");
						}
						else {
							sbsub.append("\t" + "PUSH" + "\t");
							sbsub.append(Integer.parseInt(str[0]) + "\n");
						}

					}

					if (str[1].equals("SIDENTIFIER")) {

						for (int i = 0; variable[i][0] != null; i++) {
							if (scope.equals(variable[i][0]) & str[0].equals(variable[i][1])) {
								if(sassignFlag==1||(sassignFlag==0)& (conditionalFlag==1) ) {
									sb.append("\t" + "LD" + "\t");
									sb.append("GR2,\t" + "=" + i + ";LD5\n");

									sb.append("\t" + "LD" + "\t");
									sb.append("GR1," + "\t");
									sb.append("VAR,\t");
									sb.append("GR2\n");

									sb.append("\t" + "PUSH" + "\t");
									sb.append("0,\t");
									sb.append("GR1\n");
								}
								else {
									sbsub.append("\t" + "LD" + "\t");
									sbsub.append("GR2,\t" + "=" + i + ";LD5\n");

									sbsub.append("\t" + "LD" + "\t");
									sbsub.append("GR1," + "\t");
									sbsub.append("VAR,\t");
									sbsub.append("GR2\n");

									sbsub.append("\t" + "PUSH" + "\t");
									sbsub.append("0,\t");
									sbsub.append("GR1\n");
								}
								break;
							}
						}
					}
				}

				else {	
					changeResult(0,0);
				}



				str = scanner.nextLine().split("\t"); //do not delete!!

				if (str[1].equals("SCOMMA")) {
					multiArgument=1;
					functionArgument = 1;
					if (procflag == 1 && procintro==0) argmentflag=1; //

					while (true) {

						if (flag == 0) {
							if (str[1].equals("SCOMMA")) {
								addrOfArgument++;

								if(procintro==1) {
									if (!temp.equals(",")) { //care
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


							//if (procToBracket==1)   changeArgument();

							flag = 0;
							temp = str[0];
						}
						if (flag == 0 && str[1].equals("SCOLON"))
							break;

						if (flag == 0 && (str[1].equals("SRPAREN") || str[1].equals("SRBRACKET"))) {
							break;
						}

						if (flag == 0 && str[1].equals("SSEMICOLON")) {
							break;
						}
					}
				}

				if (str[1].equals("SCOLON")) {
					functionArgument = 1;
					str = scanner.nextLine().split("\t");

					if (str[1].equals("SINTEGER") || str[1].equals("SCHAR") || str[1].equals("SBOOLEAN")) {
						temp = str[0];

						if (multiArgument==0) {
							index++;
						}

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
					if (str[1].equals("SSEMICOLON")) {
						//str = scanner.nextLine().split("\t"); 
					} 
				} else
					break;
			}


			if (str[1].equals("SNOT"))
				return true;

			if (something == 0)
				break;

			if (str[1].equals("STHEN") || str[1].equals("SDO")) {
				return true;
			}

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

			if (str[1].equals("STRUE") || str[1].equals("SFALSE") || str[1].equals("SAND") || str[1].equals("SOR"))
				return true;

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
						//conditonal の時 sbに書き込む
						if (conditionalFlag==1) {

							sb.append("\t" + "POP" + "\t");
							sb.append("GR2" + "\n");

							sb.append("\t" + "ADDA" + "\t");
							sb.append("GR2," + "\t");
							sb.append("=" + varStackPoint + "\n");

							sb.append("\t" + "LD" + "\t");
							sb.append("GR1," + "\t");
							sb.append("VAR," + "\t");
							sb.append("GR2" + "\n");

							sb.append("\t" + "PUSH" + "\t");
							sb.append("0," + "\t");
							sb.append("GR1" + "\n");

						}
						else {
							sbsub.append("\t" + "POP" + "\t");
							sbsub.append("GR2" + "\n");

							sbsub.append("\t" + "ADDA" + "\t");
							sbsub.append("GR2," + "\t");
							sbsub.append("=" + varStackPoint + "\n");

							if (sassignForPop == 1) {
								sbsub.append("\t" + "POP" + "\t");
								sbsub.append("GR1" + "\n");

								sbsub.append("\t" + "ST" + "\t");
								sbsub.append("GR1," + "\t");
								sbsub.append("VAR," + "\t");
								sbsub.append("GR2" + "\n");
							}
						}

					}
					///next
					else if (arrayreferFlagRight == 1 & str[1].equals("SRBRACKET") & conditionalFlag==1){
						if (scope.equals("global")) {
							sb.append("\t" + "POP" + "\t");
							sb.append("GR2" + "\n");

							sb.append("\t" + "ADDA" + "\t");
							sb.append("GR2," + "\t");
							sb.append("=" + varStackPoint + "\n");

							sb.append("\t" + "LD" + "\t");
							sb.append("GR1," + "\t");
							sb.append("VAR," + "\t");
							sb.append("GR2" + "\n");

							sb.append("\t" + "PUSH" + "\t");
							sb.append("0," + "\t");
							sb.append("GR1" + "\n");
						}
						else {
							sbForProc.append("\t" + "POP" + "\t");
							sbForProc.append("GR2" + "\n");

							sbForProc.append("\t" + "ADDA" + "\t");
							sbForProc.append("GR2," + "\t");
							sbForProc.append("=" + varStackPoint + "\n");

							sbForProc.append("\t" + "LD" + "\t");
							sbForProc.append("GR1," + "\t");
							sbForProc.append("VAR," + "\t");
							sbForProc.append("GR2" + "\n");

							sbForProc.append("\t" + "PUSH" + "\t");
							sbForProc.append("0," + "\t");
							sbForProc.append("GR1" + "\n");
						}
					}
					
					
				}
				else {
					int ll=0;
					if (arrayreferFlag == 1 &  str[1].equals("SRBRACKET")) {
						sb.append("\t" + "POP" + "\t");
						sb.append("GR2" + "\n");

						sb.append("\t" + "ADDA" + "\t");
						sb.append("GR2," + "\t");
						sb.append("=" + varStackPoint + "\n");

						sb.append("\t" + "LD" + "\t");
						sb.append("GR1," + "\t");
						sb.append("VAR," + "\t");
						sb.append("GR2" + "\n");

						sb.append("\t" + "PUSH" + "\t");
						sb.append("0," + "\t");
						sb.append("GR1" + "\n");


						/*if (sassignForPop == 1) {
							sbsub.append("\t" + "POP" + "\t");
							sbsub.append("GR1" + "\n");

							sbsub.append("\t" + "ST" + "\t");
							sbsub.append("GR1," + "\t");
							sbsub.append("VAR," + "\t");
							sbsub.append("GR2" + "\n");
						}*/
					}
					//new
					else if (arrayreferFlagRight == 1 &  str[1].equals("SRBRACKET")) {
						
						if (scope.equals("global")) {
							sb.append("\t" + "POP" + "\t");
							sb.append("GR2" + "\n");

							sb.append("\t" + "ADDA" + "\t");
							sb.append("GR2," + "\t");
							sb.append("=" + varStackPoint + "\n");

							sb.append("\t" + "LD" + "\t");
							sb.append("GR1," + "\t");
							sb.append("VAR," + "\t");
							sb.append("GR2" + "\n");

							sb.append("\t" + "PUSH" + "\t");
							sb.append("0," + "\t");
							sb.append("GR1" + "\n");
						}
						else {
							sbForProc.append("\t" + "POP" + "\t");
							sbForProc.append("GR2" + "\n");

							sbForProc.append("\t" + "ADDA" + "\t");
							sbForProc.append("GR2," + "\t");
							sbForProc.append("=" + varStackPoint + "\n");

							sbForProc.append("\t" + "LD" + "\t");
							sbForProc.append("GR1," + "\t");
							sbForProc.append("VAR," + "\t");
							sbForProc.append("GR2" + "\n");

							sbForProc.append("\t" + "PUSH" + "\t");
							sbForProc.append("0," + "\t");
							sbForProc.append("GR1" + "\n");
						}
						
					}
					arrayreferFlagRight=0;
				}




				return true;
			}

			if (str[1].equals("SEQUAL") || str[1].equals("SNOTEQUAL") || str[1].equals("SLESS")
					|| str[1].equals("SLESSEQUAL") || str[1].equals("SGREATEQUAL") || str[1].equals("SGREAT")) {
				
				
				if (arrayreferFlagRight == 1 ) {
					
					if (scope.equals("global")) {
						sb.append("\t" + "POP" + "\t");
						sb.append("GR2" + "\n");

						sb.append("\t" + "ADDA" + "\t");
						sb.append("GR2," + "\t");
						sb.append("=" + varStackPoint + "\n");

						sb.append("\t" + "LD" + "\t");
						sb.append("GR1," + "\t");
						sb.append("VAR," + "\t");
						sb.append("GR2" + "\n");

						sb.append("\t" + "PUSH" + "\t");
						sb.append("0," + "\t");
						sb.append("GR1" + "\n");
					}
					else {
						sbForProc.append("\t" + "POP" + "\t");
						sbForProc.append("GR2" + "\n");

						sbForProc.append("\t" + "ADDA" + "\t");
						sbForProc.append("GR2," + "\t");
						sbForProc.append("=" + varStackPoint + "\n");

						sbForProc.append("\t" + "LD" + "\t");
						sbForProc.append("GR1," + "\t");
						sbForProc.append("VAR," + "\t");
						sbForProc.append("GR2" + "\n");

						sbForProc.append("\t" + "PUSH" + "\t");
						sbForProc.append("0," + "\t");
						sbForProc.append("GR1" + "\n");
					}
					
				}
				arrayreferFlagRight=0;
				return true;
			}
				
		}
		return false;
	}

	public boolean conditionalExpression(Scanner scanner) {
		int brflag = 0;
		int loopcount = 0;
		conditionalFlag=1;

		while (true) {
			str = scanner.nextLine().split("\t");

			if (str[1].equals("SLPAREN") || str[1].equals("SLBRACKET")) {
				str = scanner.nextLine().split("\t");
				brflag++;
			}
			if (str[1].equals("SNOT")) {
				notflag = 1;
				str = scanner.nextLine().split("\t");
			}

			if (!calculation(scanner))
				break;
			 


			if (str[1].equals("SEQUAL") || str[1].equals("SNOTEQUAL") || str[1].equals("SLESS")
					|| str[1].equals("SLESSEQUAL") || str[1].equals("SGREATEQUAL") || str[1].equals("SGREAT")) {

				str = scanner.nextLine().split("\t");

				if (str[1].equals("SDO") || str[1].equals("STHEN"))
					break;

				if (str[1].equals("SLPAREN") || str[1].equals("SLBRACKET")) {
					str = scanner.nextLine().split("\t");
				}

				if (str[1].equals("SNOT")) {
					str = scanner.nextLine().split("\t");
				}

				if (!calculation(scanner))
					break;

				loopcount++;
			}

			else if (str[1].equals("STRUE") || str[1].equals("SFALSE")) {
				str = scanner.nextLine().split("\t");
			}

			if (str[1].equals("STRUE") || str[1].equals("SFALSE"))
				str = scanner.nextLine().split("\t");

			if (brflag > 0 && (str[1].equals("SRPAREN"))) {
				str = scanner.nextLine().split("\t");
				brflag--;
				loopcount++;
			}

			if (str[1].equals("SAND") || str[1].equals("SOR"))
				loopcount++;

			if ((brflag == 0) && (str[1].equals("SDO") || str[1].equals("STHEN"))) {

				selectComparisonOperator();

				if (loopcount == 0 && brflag == 0) {
					if (booleanType == 1) {
						booleanType = 0;
						conditionalFlag=0;
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

		if (scope.equals("global")) {
			sb.append("LOOP" + initwhile + "\t");
			sb.append("NOP" + "\n");
		} else {
			sbForProc.append("LOOP" + initwhile + "\t");
			sbForProc.append("NOP" + "\n");
		}

		if (conditionalExpression(scanner)) {
			str = scanner.nextLine().split("\t");

			if (str[1].equals("SBEGIN")) {

				while (scanner.hasNextLine()) {

					if (whileFlag == 1) {
						if (scope.equals("global")) {
							sb.append("WTRUE" + wtrue + "\t");
							sb.append("NOP" + "\n");
						} else {
							sbForProc.append("WTRUE" + wtrue + "\t");
							sbForProc.append("NOP" + "\n");
						}
						whileFlag = 0;
					}

					if ((str[1].equals("SWHILE")) || (str[1].equals("SIF")) || (str[1].equals("SWRITELN"))) {
					} else
						str = scanner.nextLine().split("\t");

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

						if (scope.equals("global")) {
							sb.append("\t" + "JUMP" + "\t" + "LOOP" + initwhile + "\n");
							sb.append("ENDLP" + endwhile + "\t");
							sb.append("NOP" + "\n");
						}

						else {
							sbForProc.append("\t" + "JUMP" + "\t" + "LOOP" + initwhile + "\n");
							sbForProc.append("ENDLP" + endwhile + "\t");
							sbForProc.append("NOP" + "\n");
						}
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
		if (conditionalExpression(scanner)) {

			str = scanner.nextLine().split("\t");

			if (str[1].equals("SBEGIN")) {

				while (scanner.hasNextLine()) {

					if (ifFlag == 1) {

						if (scope.equals("global")) {
							sb.append("ITRUE" + itrueif + "\t");
							sb.append("NOP" + "\n");
						}

						else {
							sbForProc.append("ITRUE" + itrueif + "\t");
							sbForProc.append("NOP" + "\n");
						}
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

				if (scope.equals("global")) {
					sb.append("\t" + "JUMP" + "\t");
					sb.append("ENDIF" + endif + "\n");
					sb.append("ELSE" + elseif + "\t");
					sb.append("NOP" + "\n");
				}

				else {
					sbForProc.append("\t" + "JUMP" + "\t");
					sbForProc.append("ENDIF" + endif + "\n");
					sbForProc.append("ELSE" + elseif + "\t"); 
					sbForProc.append("NOP" + ";\n");
				}

				if (str[1].equals("SSEMICOLON")) {
					if (scope.equals("global")) {
						sb.append("ENDIF" + endif + "\t");
						sb.append("NOP" + "\n");
					} else {
						sbForProc.append("ENDIF" + endif + "\t"); 
						sbForProc.append("NOP" + "\n");
					}

					//ifcount++;
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
							if (scope.equals("global")) {
								sb.append("ENDIF" + endif + "\t");
								sb.append("NOP" + "\n");
							} else {
								sbForProc.append("ENDIF" + endif + "\t");
								sbForProc.append("NOP" + "\n");
							}
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
		sbForProc.append("PROC" + procCounter + "\t");
		sbForProc.append("NOP" + "\n");
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
		str = scanner.nextLine().split("\t");
		if (str[1].equals("SLPAREN")) {
			str = scanner.nextLine().split("\t");

			while (scanner.hasNextLine()) {
				if (str[1].equals("SIDENTIFIER") || str[1].equals("SSTRING")) {

					if (str[1].equals("SSTRING")) {

						subRoutineBuff.append("CHAR" + subRoutineBuffCount + "\t");
						subRoutineBuff.append("DC" + "\t");
						subRoutineBuff.append(str[0] + "\n");
						subRoutineBuffCount++;

						int countStringlength = str[0].length() - 2;

						if (scope.equals("global")) {
							sb.append("\t" + "LD" + "\t");
							sb.append("GR1," + "\t");
							sb.append("=" + countStringlength + "\n");

							sb.append("\t" + "PUSH" + "\t");
							sb.append("0," + "\t");
							sb.append("GR1" + "\n");

							sb.append("\t" + "LAD" + "\t");
							sb.append("GR2," + "\t");
							subRoutineBuffCount--;
							sb.append("CHAR" + subRoutineBuffCount + "\n");
							subRoutineBuffCount++;

							sb.append("\t" + "PUSH" + "\t");
							sb.append("0," + "\t");
							sb.append("GR2" + "\n");

							sb.append("\t" + "POP" + "\t");
							sb.append("GR2" + "\n");

							sb.append("\t" + "POP" + "\t");
							sb.append("GR1" + "\n");

							sb.append("\t" + "CALL" + "\t");
							sb.append("WRTSTR" + "\n");
						}

						else {
							sbForProc.append("\t" + "LD" + "\t");
							sbForProc.append("GR1," + "\t");
							sbForProc.append("=" + countStringlength + "\n");

							sbForProc.append("\t" + "PUSH" + "\t");
							sbForProc.append("0," + "\t");
							sbForProc.append("GR1" + "\n");

							sbForProc.append("\t" + "LAD" + "\t");
							sbForProc.append("GR2," + "\t");
							subRoutineBuffCount--;
							sbForProc.append("CHAR" + subRoutineBuffCount + "\n");
							subRoutineBuffCount++;

							sbForProc.append("\t" + "PUSH" + "\t");
							sbForProc.append("0," + "\t");
							sbForProc.append("GR2" + "\n");

							sbForProc.append("\t" + "POP" + "\t");
							sbForProc.append("GR2" + "\n");

							sbForProc.append("\t" + "POP" + "\t");
							sbForProc.append("GR1" + "\n");

							sbForProc.append("\t" + "CALL" + "\t");
							sbForProc.append("WRTSTR" + "\n");
						}
						block = 1;
					}

					else if (str[1].equals("SIDENTIFIER")) {

						if (!arrayCheck(str[0])) {
							if (scope.equals("global")) {
								for (int i = 0; variable[i][0] != null; i++) {
									if (scope.equals(variable[i][0]) & str[0].equals(variable[i][1])) {
										varStackPoint = i;
										break;
									}
								}

								sb.append("\t" + "LD" + "\t");
								sb.append("GR2," + "\t");
								sb.append("=" + varStackPoint + ";LD6\n");

								sb.append("\t" + "LD" + "\t");
								sb.append("GR1," + "\t");
								sb.append("VAR," + "\t");
								sb.append("GR2" + "\n");

								sb.append("\t" + "PUSH" + "\t");
								sb.append("0," + "\t");
								sb.append("GR1" + "\n");

								sb.append("\t" + "POP" + "\t");
								sb.append("GR2" + "\n");
							}

							else {
								for (int i = 0; variable[i][0] != null; i++) {
									if (scope.equals(variable[i][0]) & str[0].equals(variable[i][1])) {
										varStackPoint = i;
										break;
									}

									else if (!scope.equals("global") & variable[i][0].equals("global")
											& str[0].equals(variable[i][1]) && variableSameName(str[0]) == 1) {
										varStackPoint = i;
										break;
									}
								}

								sbForProc.append("\t" + "LD" + "\t");
								sbForProc.append("GR2," + "\t");
								sbForProc.append("=" + varStackPoint + ";LD7\n");

								sbForProc.append("\t" + "LD" + "\t");
								sbForProc.append("GR1," + "\t");
								sbForProc.append("VAR," + "\t");
								sbForProc.append("GR2" + "\n");

								sbForProc.append("\t" + "PUSH" + "\t");
								sbForProc.append("0," + "\t");
								sbForProc.append("GR1" + "\n");

								sbForProc.append("\t" + "POP" + "\t");
								sbForProc.append("GR2" + "\n");
							}

							String temp = varTypeCheck(str[0]);
							if (temp == null)
								temp = variable[varStackPoint][2];

							if (temp != null) {
								if (temp.equals("integer")) {
									if (scope.equals("global")) {
										sb.append("\t" + "CALL" + "\t");
										sb.append("WRTINT" + "\n");
									} else {
										sbForProc.append("\t" + "CALL" + "\t");
										sbForProc.append("WRTINT" + "\n");
									}
								}

								else if (temp.equals("char")) {
									if (scope.equals("global")) {
										sb.append("\t" + "CALL" + "\t");
										sb.append("WRTCH" + "\n");
									} else {
										sbForProc.append("\t" + "CALL" + "\t");
										sbForProc.append("WRTCH" + "\n");
									}
								} else {
								}
							}
							block = 1;
						}

						else {
							arrayreferFlag = 1;
							for (int i = 0; variable[i][0] != null; i++) {
								if (scope.equals(variable[i][0]) & str[0].equals(variable[i][1])) {
									varStackPoint = i;
									varStackPoint--;
									break;
								}
								else if (!scope.equals("global") & variable[i][0].equals("global")
										& str[0].equals(variable[i][1])) {
									varStackPoint = i;
									varStackPoint--;
									break;
								}
							}
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
						varStackPoint = 0;
						arrayreferFlag = 0;
					}

					if (str[1].equals("SCOMMA")) {
						str = scanner.nextLine().split("\t");

						if (block != 1) {
							if (scope.equals("global")) {
								sb.append("\t" + "LD" + "\t");
								sb.append("GR1," + "\t");
								sb.append("VAR," + "\t");
								sb.append("GR2" + "\n");

								sb.append("\t" + "PUSH" + "\t");
								sb.append("0," + "\t");
								sb.append("GR1" + "\n");

								sb.append("\t" + "POP" + "\t");
								sb.append("GR2" + "\n");

								sb.append("\t" + "CALL" + "\t");
								sb.append("WRTINT" + "\n");
							}

							else {
								sbForProc.append("\t" + "LD" + "\t");
								sbForProc.append("GR1," + "\t");
								sbForProc.append("VAR," + "\t");
								sbForProc.append("GR2" + "\n");

								sbForProc.append("\t" + "PUSH" + "\t");
								sbForProc.append("0," + "\t");
								sbForProc.append("GR1" + "\n");

								sbForProc.append("\t" + "POP" + "\t");
								sbForProc.append("GR2" + "\n");

								sbForProc.append("\t" + "CALL" + "\t");
								sbForProc.append("WRTINT" + "\n");
							}
						}
						block = 0;
					}
					if (str[1].equals("SRPAREN")) {
						if (block != 1) {
							if (scope.equals("global")) {
								sb.append("\t" + "LD" + "\t");
								sb.append("GR1," + "\t");
								sb.append("VAR," + "\t");
								sb.append("GR2" + "\n");

								sb.append("\t" + "PUSH" + "\t");
								sb.append("0," + "\t");
								sb.append("GR1" + "\n");

								sb.append("\t" + "POP" + "\t");
								sb.append("GR2" + "\n");

								sb.append("\t" + "CALL" + "\t");
								sb.append("WRTINT" + "\n");

							} else {
								sbForProc.append("\t" + "LD" + "\t");
								sbForProc.append("GR1," + "\t");
								sbForProc.append("VAR," + "\t");
								sbForProc.append("GR2" + "\n");

								sbForProc.append("\t" + "PUSH" + "\t");
								sbForProc.append("0," + "\t");
								sbForProc.append("GR1" + "\n");

								sbForProc.append("\t" + "POP" + "\t");
								sbForProc.append("GR2" + "\n");

								sbForProc.append("\t" + "CALL" + "\t");
								sbForProc.append("WRTINT" + "\n");
							}
						}
						if (scope.equals("global")) {
							sb.append("\t" + "CALL" + "\t");
							sb.append("WRTLN" + "\n");
						} else {
							sbForProc.append("\t" + "CALL" + "\t");
							sbForProc.append("WRTLN" + "\n");
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean varDuplicationCheck(String varName) {
		for (int i = 0; i < 100; i++) {
			if ((variable[i][0] != null) && (variable[i][0].equals(scope))) {
				if (variable[i][1].equals(varName)) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean substitutionTypeCheck(String varName, String substitution, String substitutionType) {
		String varType = null;
		for (int i = 0; i < 100; i++) {
			if ((variable[i][0] != null) && (variable[i][0].equals(scope))) {
				if (variable[i][1].equals(varName)) {
					if (str[1].equals("SIDENTIFIER")) {
						for (int j = 0; j < 100; j++) {
							if ((variable[j][0] != null) && (variable[j][0].equals(scope))) {
								if (variable[j][1].equals(substitution)) {
									varType = variable[j][2];
									break;
								}
							}
						}
					}

					if (varType == null)
						varType = substitutionType;

					if (variable[i][2].equals(varType)) {
						return true;
					} else if ((variable[i][2].equals("integer")) && (varType.equals("SCONSTANT"))) {
						return true;
					} else if ((variable[i][2].equals("char")) && (varType.equals("SSTRING"))) {
						return true;
					} else if ((variable[i][2].equals("boolean"))
							&& (varType.equals("STRUE") || varType.equals("SFALSE"))) {
						return true;
					} else
						break;
				}
			}
		}
		return false;
	}

	public boolean checkSCONSTANT(String varName) {
		for (int i = 0; i < 100; i++) {
			if ((variable[i][0] != null) && (variable[i][0].equals(scope))) {
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
		for (int i = 0; i < 100; i++) {
			if ((variable[i][0] != null) && (variable[i][0].equals(scope))) {
				if (variable[i][1].equals(varName)) {
					return variable[i][2];
				}
			}
		}
		return null;
	}

	public boolean procedureCheck(String procname) {
		for (int i = 0; i < 100; i++) {
			if ((variable[i][0] != null) && ((variable[i][0].equals(procname)))) {
				return true;
			}
		}
		return false;
	}


	public int whichProcedureCall(String procname) {
		int count=-1;
		String formerScope="global";
		for (int i = 0; i < 100; i++) {
			if ((variable[i][0] != null)) {
				if (formerScope.equals(variable[i][0])) {
				}

				else {
					count++;
				}

				if (variable[i][0].equals(procname)) {
					break;
				}
				formerScope=variable[i][0];

			}
		}
		return count;
	}


	public boolean arrayCheck(String varName) {
		for (int i = 0; i < 100; i++) {
			if ((variable[i][0] != null) && (variable[i][0].equals(scope))) {
				if (variable[i][1].equals(varName)) {

					if ((variable[i][3] != null) && (variable[i][3].equals("array"))) {
						return true;
					}
				}
			}

			else if ((variable[i][0] != null)) {
				if (!scope.equals("global") & variable[i][0].equals("global")) { // maybe delete
					if (variable[i][1].equals(varName)) {
						if ((variable[i][3] != null) && (variable[i][3].equals("array"))) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	int variableSameName(String varName) {
		int count = 0;
		for (int i = 0; variable[i][0] != null; i++) {
			
			 if (variable[i][1].equals(varName)) {
					count++;
				}
			 
			 if (variable[i][3]!=null) {
				 if (count > 1 & variable[i][3].equals("array")) {
					 count--;
				}
			 }
					
		}
		
		return count;
	}

	void changeResult(int operatortype,int minusFlag) {
        int kjll=0;
		int temp = 0;
		if (str[1].equals("SIDENTIFIER")) {
			
			if (minusFlag == 1) {
				System.out.println(str[0]+str[3]+"ff");
				if (scope.equals("global")) {
					if (sassignFlag == 1 || procflag==1 || conditionalFlag==1) {
						sb.append("\t" + "PUSH" + "\t");
						sb.append("0;" + "\n");
					}
					else {
						sbsub.append("\t" + "PUSH" + "\t");
						sbsub.append("0;" + "\n");
					}

				} else {
					sbForProc.append("\t" + "PUSH" + "\t");
					sbForProc.append("0;changeResult" + "\n");
				}
			}

			for (int i = 0; variable[i][0] != null; i++) {
				if (scope.equals(variable[i][0]) & str[0].equals(variable[i][1])) {
					if (sassignFlag == 1 || procflag==1 || conditionalFlag==1) {
						if  (!arrayCheck(str[0])) {
							if (scope.equals("global")) {
								sb.append("\t" + "LD" + "\t");
								sb.append("GR2," + "\t");
								sb.append("=" + i + ";lllll\n");

								sb.append("\t" + "LD" + "\t");
								sb.append("GR1," + "\t");
								sb.append("VAR," + "\t");
								sb.append("GR2\n");

								sb.append("\t" + "PUSH" + "\t");
								sb.append("0," + "\t");
								sb.append("GR1" + "\n");
							}

							else {
								sbForProc.append("\t" + "LD" + "\t");
								sbForProc.append("GR2," + "\t");
								sbForProc.append("=" + i + ";vvvvvvvv\n");

								sbForProc.append("\t" + "LD" + "\t");
								sbForProc.append("GR1," + "\t");
								sbForProc.append("VAR," + "\t");
								sbForProc.append("GR2\n");

								sbForProc.append("\t" + "PUSH" + "\t");
								sbForProc.append("0," + "\t");
								sbForProc.append("GR1" + "\n");
							}
						}

						else {
							arrayreferFlag=1;
							
							for (int j = 0; variable[j][0] != null; j++) {
								if (scope.equals(variable[j][0]) & str[0].equals(variable[j][1])) {
									varStackPoint = j;
									varStackPoint--;
									break;
								}
								else if (!scope.equals("global") & variable[j][0].equals("global")
										& str[0].equals(variable[j][1])) {

									varStackPoint = j;
									varStackPoint--;
									break;
								}
							}
							
						}			
					}

					else {
						//here
					
					//procedure宣言文
					if (procintro==0) {
						sbsub.append("\t" + "LD" + "\t");
						sbsub.append("GR2," + "\t");
						sbsub.append("=" + i + ";ppppppp\n");

						sbsub.append("\t" + "LD" + "\t");
						sbsub.append("GR1," + "\t");
						sbsub.append("VAR," + "\t");
						sbsub.append("GR2\n");

						sbsub.append("\t" + "PUSH" + "\t");
						sbsub.append("0," + "\t");
						sbsub.append("GR1" + "\n");
					}
						
						
					}
					break;
				}

				else if (!scope.equals("global") & variable[i][0].equals("global")
						& str[0].equals(variable[i][1]) && variableSameName(str[0]) == 1) {
					if (sassignFlag == 1 || procflag==1 || conditionalFlag==1) {
						if (scope.equals("global")) {
							sb.append("\t" + "LD" + "\t");
							sb.append("GR2," + "\t");
							sb.append("=" + i + ";rrrrrrr\n");

							sb.append("\t" + "LD" + "\t");
							sb.append("GR1," + "\t");
							sb.append("VAR," + "\t");
							sb.append("GR2\n");

							sb.append("\t" + "PUSH" + "\t");
							sb.append("0," + "\t");
							sb.append("GR1" + "\n");
						}

						else {
							sbForProc.append("\t" + "LD" + "\t");
							sbForProc.append("GR2," + "\t");
							sbForProc.append("=" + i + ";ddddd\n");

							sbForProc.append("\t" + "LD" + "\t");
							sbForProc.append("GR1," + "\t");
							sbForProc.append("VAR," + "\t");
							sbForProc.append("GR2\n");

							sbForProc.append("\t" + "PUSH" + "\t");
							sbForProc.append("0," + "\t");
							sbForProc.append("GR1" + "\n");
						}
					}

					else {
						
						sbsub.append("\t" + "LD" + "\t");
						sbsub.append("GR2," + "\t");
						sbsub.append("=" + i + ";tttttt\n");

						sbsub.append("\t" + "LD" + "\t");
						sbsub.append("GR1," + "\t");
						sbsub.append("VAR," + "\t");
						sbsub.append("GR2\n");

						sbsub.append("\t" + "PUSH" + "\t");
						sbsub.append("0," + "\t");
						sbsub.append("GR1" + "\n");
						
					}
					break;
				}
			}

		}

		else  if (str[1].equals("SCONSTANT")){
			
			if (minusFlag == 1) {
				if (scope.equals("global")) {
					sb.append("\t" + "PUSH" + "\t");
					sb.append("0;changeResult" + "\n");
				} else {
					sbForProc.append("\t" + "PUSH" + "\t");
					sbForProc.append("0" + "\n");
				}
			}

			temp = Integer.parseInt(str[0]);
			if (scope.equals("global")) {
				
				
				if (sassignFlag == 1 || procflag==1 || conditionalFlag==1) {
					sb.append("\t" + "PUSH" + "\t");
					sb.append(temp + "\n");
				}
				else {
					sbsub.append("\t" + "PUSH" + "\t");
					sbsub.append(temp + "\n");
				}
			}

			else {
				if (sassignFlag == 1 || procflag==1 || conditionalFlag==1) {
					sbForProc.append("\t" + "PUSH" + "\t");
					sbForProc.append(temp + "\n");
				}
				else {
					sbsub.append("\t" + "PUSH" + "\t");
					sbsub.append(temp + "\n");
				}
				
				//sbForProc.append("\t" + "PUSH" + "\t");
				//sbForProc.append(temp + "\n");
			}
		}

		if (operatortype == 0) {

		}

		else {
			if (scope.equals("global")) {
				//if(sassignFlag==1 ||(sassignFlag==0)& (conditionalFlag==1) ) {
				if (sassignFlag == 1 || procflag==1 || conditionalFlag==1) {
					sb.append("\t" + "POP" + "\t");
					sb.append("GR2" + "\n");

					sb.append("\t" + "POP" + "\t");
					sb.append("GR1" + "\n");
				}
				else {
					sbsub.append("\t" + "POP" + "\t");
					sbsub.append("GR2" + "\n");

					sbsub.append("\t" + "POP" + "\t");
					sbsub.append("GR1" + "\n");
				}

			} else {
				
				if (sassignFlag == 1 || procflag==1 || conditionalFlag==1) {
					sbForProc.append("\t" + "POP" + "\t");
					sbForProc.append("GR2" + "\n");

					sbForProc.append("\t" + "POP" + "\t");
					sbForProc.append("GR1" + "\n");
				}
				else {
					sbsub.append("\t" + "POP" + "\t");
					sbsub.append("GR2" + "\n");

					sbsub.append("\t" + "POP" + "\t");
					sbsub.append("GR1" + "\n");
				}
				
			}

			if (operatortype == 1) {

				if (scope.equals("global")) {
				
					if (sassignFlag == 1 || procflag==1 || conditionalFlag==1) {
						sb.append("\t" + "ADDA" + "\t");
						sb.append("GR1," + "\t");
						sb.append("GR2" + "\n");

						sb.append("\t" + "PUSH" + "\t");
						sb.append("0," + "\t");
						sb.append("GR1" + "\n");
					
					}
					else {
						sbsub.append("\t" + "ADDA" + "\t");
						sbsub.append("GR1," + "\t");
						sbsub.append("GR2" + "\n");

						sbsub.append("\t" + "PUSH" + "\t");
						sbsub.append("0," + "\t");
						sbsub.append("GR1" + "\n");
					}

				}

				else {
					if (sassignFlag == 1 || procflag==1 || conditionalFlag==1) {
						sbForProc.append("\t" + "ADDA" + "\t");
						sbForProc.append("GR1," + "\t");
						sbForProc.append("GR2" + "\n");

						sbForProc.append("\t" + "PUSH" + "\t");
						sbForProc.append("0," + "\t");
						sbForProc.append("GR1" + "\n");
					}
					else {
						sbsub.append("\t" + "ADDA" + "\t");
						sbsub.append("GR1," + "\t");
						sbsub.append("GR2" + "\n");

						sbsub.append("\t" + "PUSH" + "\t");
						sbsub.append("0," + "\t");
						sbsub.append("GR1" + "\n");
						
					}
					
					
					
				}

			}

			if (operatortype == 2) {
				if (scope.equals("global")) {
					if (sassignFlag == 1 || procflag==1 || conditionalFlag==1) {
						sb.append("\t" + "SUBA" + "\t");
						sb.append("GR1," + "\t");
						sb.append("GR2" + "\n");

						sb.append("\t" + "PUSH" + "\t");
						sb.append("0," + "\t");
						sb.append("GR1" + "\n");
					}
					else {
						sbsub.append("\t" + "SUBA" + "\t");
						sbsub.append("GR1," + "\t");
						sbsub.append("GR2" + "\n");

						sbsub.append("\t" + "PUSH" + "\t");
						sbsub.append("0," + "\t");
						sbsub.append("GR1" + "\n");
					}

				}

				else {
					if (sassignFlag == 1 || procflag==1 || conditionalFlag==1) {
						sbForProc.append("\t" + "SUBA" + "\t");
						sbForProc.append("GR1," + "\t");
						sbForProc.append("GR2" + "\n");

						sbForProc.append("\t" + "PUSH" + "\t");
						sbForProc.append("0," + "\t");
						sbForProc.append("GR1" + "\n");
					}
					else {
						sbsub.append("\t" + "SUBA" + "\t");
						sbsub.append("GR1," + "\t");
						sbsub.append("GR2" + "\n");

						sbsub.append("\t" + "PUSH" + "\t");
						sbsub.append("0," + "\t");
						sbsub.append("GR1" + "\n");
					}

				}

			}

			if (operatortype == 3) {
			
				if (scope.equals("global")) {
					
					if (sassignFlag == 1 || procflag==1 || conditionalFlag==1) {
					
						sb.append("\t" + "CALL" + "\t");
						sb.append("MULT" + "\n");

						sb.append("\t" + "PUSH" + "\t");
						sb.append("0," + "\t");
						sb.append("GR2" + "\n");
					}
					else {
						sbsub.append("\t" + "CALL" + "\t");
						sbsub.append("MULT" + "\n");

						sbsub.append("\t" + "PUSH" + "\t");
						sbsub.append("0," + "\t");
						sbsub.append("GR2" + "\n");
					}

				} else {
					
					if (sassignFlag == 1 || procflag==1 || conditionalFlag==1) {
						sbForProc.append("\t" + "CALL" + "\t");
						sbForProc.append("MULT" + "\n");

						sbForProc.append("\t" + "PUSH" + "\t");
						sbForProc.append("0," + "\t");
						sbForProc.append("GR2" + "\n");
					}
					else {
						sbsub.append("\t" + "CALL" + "\t");
						sbsub.append("MULT" + "\n");

						sbsub.append("\t" + "PUSH" + "\t");
						sbsub.append("0," + "\t");
						sbsub.append("GR2" + "\n");
					}
					
				}

			}

			if (operatortype == 4) {
				if (scope.equals("global")) {
					if (sassignFlag == 1 || procflag==1 || conditionalFlag==1) {
						sb.append("\t" + "CALL" + "\t");
						sb.append("DIV" + "\n");

						sb.append("\t" + "PUSH" + "\t");
						sb.append("0," + "\t");
						sb.append("GR2" + "\n");
					}

					else {
						sbsub.append("\t" + "CALL" + "\t");
						sbsub.append("DIV" + "\n");

						sbsub.append("\t" + "PUSH" + "\t");
						sbsub.append("0," + "\t");
						sbsub.append("GR2" + "\n");
					}

				} else {
					if (sassignFlag == 1 || procflag==1 || conditionalFlag==1) {
						sbForProc.append("\t" + "CALL" + "\t");
						sbForProc.append("DIV" + "\n");

						sbForProc.append("\t" + "PUSH" + "\t");
						sbForProc.append("0," + "\t");
						sbForProc.append("GR2" + "\n");
					}

					else {
						sbsub.append("\t" + "CALL" + "\t");
						sbsub.append("DIV" + "\n");

						sbsub.append("\t" + "PUSH" + "\t");
						sbsub.append("0," + "\t");
						sbsub.append("GR2" + "\n");
					}

					
				}


			}

			if (operatortype == 5) {
				if (scope.equals("global")) {
					if (sassignFlag == 1 || procflag==1 || conditionalFlag==1) {
						sb.append("\t" + "CALL" + "\t");
						sb.append("DIV" + "\n");

						sb.append("\t" + "PUSH" + "\t");
						sb.append("0," + "\t");
						sb.append("GR1" + "\n");
					}
					else {
						sbsub.append("\t" + "CALL" + "\t");
						sbsub.append("DIV" + "\n");

						sbsub.append("\t" + "PUSH" + "\t");
						sbsub.append("0," + "\t");
						sbsub.append("GR1" + "\n");
					}

				} else {
					if (sassignFlag == 1 || procflag==1 || conditionalFlag==1) {
						sbForProc.append("\t" + "CALL" + "\t");
						sbForProc.append("DIV" + "\n");

						sbForProc.append("\t" + "PUSH" + "\t");
						sbForProc.append("0," + "\t");
						sbForProc.append("GR1" + "\n");
					}
					else {
						sbsub.append("\t" + "CALL" + "\t");
						sbsub.append("DIV" + "\n");

						sbsub.append("\t" + "PUSH" + "\t");
						sbsub.append("0," + "\t");
						sbsub.append("GR1" + "\n");
					}
					
				}

			}
		}

		return;
	}

	public void selectComparisonOperator() {
		if (scope.equals("global")) {
			sb.append("\t" + "POP" + "\t");
			sb.append("GR2" + "\n");

			sb.append("\t" + "POP" + "\t");
			sb.append("GR1" + "\n");

			sb.append("\t" + "CPA" + "\t"); // 左 - 右

			sb.append("GR1," + "\t");
			sb.append("GR2\n");
		} else {
			sbForProc.append("\t" + "POP" + "\t");
			sbForProc.append("GR2" + "\n");

			sbForProc.append("\t" + "POP" + "\t");
			sbForProc.append("GR1" + "\n");

			// フラグに応じてCASLのCPA→ JPL, JMI, JNZ, JZEを呼び出す
			sbForProc.append("\t" + "CPA" + "\t"); // 左 - 右

			sbForProc.append("GR1," + "\t");
			sbForProc.append("GR2\n");
		}

		switch (comparisonOperator) {
		// =, <>(!=), <, <=, >=, >
		case 1:
			if (scope.equals("global")) {
				sb.append("\t" + "JZE" + "\t");
				if (whileFlag == 1) {
					sb.append("WTRUE" + whilecount + "\n");
					sb.append("\t" + "JUMP" + "\t");
					sb.append("ENDLP" + whilecount + "\n");
				}

				if (ifFlag == 1) {
					sb.append("ITRUE" + ifcount + "\n");
					sb.append("\t" + "JUMP" + "\t");
					sb.append("ELSE" + ifcount + "\n");
				}

			} else {
				sbForProc.append("\t" + "JZE" + "\t");
				if (whileFlag == 1) {
					sbForProc.append("WTRUE" + whilecount + "\n");
					sbForProc.append("\t" + "JUMP" + "\t");
					sbForProc.append("ENDLP" + whilecount + "\n");
				}

				if (ifFlag == 1) {
					sbForProc.append("ITRUE" + ifcount + "\n");
					sbForProc.append("\t" + "JUMP" + "\t");
					sbForProc.append("ELSE" + ifcount + "\n");
				}
			}

			break;

		case 2:
			if (scope.equals("global")) {
				sb.append("\t" + "JNZ" + "\t");
				if (whileFlag == 1) {
					sb.append("WTRUE" + whilecount + "\n");
					sb.append("\t" + "JUMP" + "\t");
					sb.append("ENDLP" + whilecount + "\n");
				}

				if (ifFlag == 1) {
					sb.append("ITRUE" + ifcount + "\n");
					sb.append("\t" + "JUMP" + "\t");
					sb.append("ELSE" + ifcount + "\n");
				}

			} else {
				sbForProc.append("\t" + "JNZ" + "\t");
				if (whileFlag == 1) {
					sbForProc.append("WTRUE" + whilecount + "\n");
					sbForProc.append("\t" + "JUMP" + "\t");
					sbForProc.append("ENDLP" + whilecount + "\n");
				}

				if (ifFlag == 1) {
					sbForProc.append("ITRUE" + ifcount + "\n");
					sbForProc.append("\t" + "JUMP" + "\t");
					sbForProc.append("ELSE" + ifcount + "\n");
				}
			}

			break;

		case 3:
			if (scope.equals("global")) {
				sb.append("\t" + "JMI" + "\t");
				if (whileFlag == 1) {
					sb.append("WTRUE" + whilecount + "\n");
					sb.append("\t" + "JUMP" + "\t");
					sb.append("ENDLP" + whilecount + "\n");
				}

				if (ifFlag == 1) {
					sb.append("ITRUE" + ifcount + "\n");
					sb.append("\t" + "JUMP" + "\t");
					sb.append("ELSE" + ifcount + "\n");
				}

			} else {
				sbForProc.append("\t" + "JMI" + "\t");
				if (whileFlag == 1) {
					sbForProc.append("WTRUE" + whilecount + "\n");
					sbForProc.append("\t" + "JUMP" + "\t");
					sbForProc.append("ENDLP" + whilecount + "\n");
				}

				if (ifFlag == 1) {
					sbForProc.append("ITRUE" + ifcount + "\n");
					sbForProc.append("\t" + "JUMP" + "\t");
					sbForProc.append("ELSE" + ifcount + "\n");
				}
			}
			break;

		case 4:
			if (scope.equals("global")) {
				sb.append("\t" + "JMI" + "\t");
				if (whileFlag == 1) {
					sb.append("WTRUE" + whilecount + "\n");
					sb.append("\t" + "JZE" + "\t");
					sb.append("WTRUE" + whilecount + "\n");
					sb.append("\t" + "JUMP" + "\t");
					sb.append("ENDLP" + whilecount + "\n");
				}

				if (ifFlag == 1) {
					sb.append("ITRUE" + ifcount + "\n");
					sb.append("\t" + "JZE" + "\t");
					sb.append("ITRUE" + ifcount + "\n");
					sb.append("\t" + "JUMP" + "\t");
					sb.append("ELSE" + ifcount + "\n");
				}
			}

			else {
				sbForProc.append("\t" + "JMI" + "\t");
				if (whileFlag == 1) {
					sbForProc.append("WTRUE" + whilecount + "\n");
					sbForProc.append("\t" + "JZE" + "\t");
					sbForProc.append("WTRUE" + whilecount + "\n");
					sbForProc.append("\t" + "JUMP" + "\t");
					sbForProc.append("ENDLP" + whilecount + "\n");
				}

				if (ifFlag == 1) {
					sbForProc.append("ITRUE" + ifcount + "\n");
					sbForProc.append("\t" + "JZE" + "\t");
					sbForProc.append("ITRUE" + ifcount + "\n");
					sbForProc.append("\t" + "JUMP" + "\t");
					sbForProc.append("ELSE" + ifcount + "\n");
				}
			}

			break;

		case 5:
			if (scope.equals("global")) {
				sb.append("\t" + "JPL" + "\t");
				if (whileFlag == 1) {
					sb.append("WTRUE" + whilecount + "\n");
					sb.append("\t" + "JZE" + "\t");
					sb.append("WTRUE" + whilecount + "\n");
					sb.append("\t" + "JUMP" + "\t");
					sb.append("ENDLP" + whilecount + "\n");
				}

				if (ifFlag == 1) {
					sb.append("ITRUE" + ifcount + "\n");
					sb.append("\t" + "JZE" + "\t");
					sb.append("ITRUE" + ifcount + "\n");
					sb.append("\t" + "JUMP" + "\t");
					sb.append("ELSE" + ifcount + "\n");
				}
			}

			else {
				sbForProc.append("\t" + "JPL" + "\t");
				if (whileFlag == 1) {
					sbForProc.append("WTRUE" + whilecount + "\n");
					sbForProc.append("\t" + "JZE" + "\t");
					sbForProc.append("WTRUE" + whilecount + "\n");
					sbForProc.append("\t" + "JUMP" + "\t");
					sbForProc.append("ENDLP" + whilecount + "\n");
				}

				if (ifFlag == 1) {
					sbForProc.append("ITRUE" + ifcount + "\n");
					sbForProc.append("\t" + "JZE" + "\t");
					sbForProc.append("ITRUE" + ifcount + "\n");
					sbForProc.append("\t" + "JUMP" + "\t");
					sbForProc.append("ELSE" + ifcount + "\n");
				}
			}
			break;

		case 6:
			if (scope.equals("global")) {
				sb.append("\t" + "JPL" + "\t");
				if (whileFlag == 1) {
					sb.append("WTRUE" + whilecount + "\n");
					sb.append("\t" + "JUMP" + "\t");
					sb.append("ENDLP" + whilecount + "\n");
				}

				if (ifFlag == 1) {
					sb.append("ITRUE" + ifcount + "\n");
					sb.append("\t" + "JUMP" + "\t");
					sb.append("ELSE" + ifcount + "\n");
				}
			} else {
				sbForProc.append("\t" + "JPL" + "\t");
				if (whileFlag == 1) {
					sbForProc.append("WTRUE" + whilecount + "\n");
					sbForProc.append("\t" + "JUMP" + "\t");
					sbForProc.append("ENDLP" + whilecount + "\n");
				}

				if (ifFlag == 1) {
					sbForProc.append("ITRUE" + ifcount + "\n");
					sbForProc.append("\t" + "JUMP" + "\t");
					sbForProc.append("ELSE" + ifcount + "\n");
				}
			}
			break;
		}
		return;
	}

	void changeArgument() {
		if (scope.equals("global")) {
			sb.append("\t" + "LD" + "\t");
			sb.append("GR2, " + "\t");
			sb.append("=" + addrOfArgument + ";LD3\n");

			sb.append("\t" + "POP" + "\t");
			sb.append("GR1" + "\n");

			sb.append("\t" + "ST" + "\t");
			sb.append("GR1, " + "\t");
			sb.append("VAR, " + "\t");
			sb.append("GR2" + "\n");
		}

		else {

			sbForProc.append("\t" + "LD" + "\t");
			sbForProc.append("GR2, " + "\t");
			sbForProc.append("=" + addrOfArgument + ";LD4\n");

			sbForProc.append("\t" + "POP" + "\t");
			sbForProc.append("GR1" + "\n");

			sbForProc.append("\t" + "ST" + "\t");
			sbForProc.append("GR1, " + "\t");
			sbForProc.append("VAR, " + "\t");
			sbForProc.append("GR2" + "\n");
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
