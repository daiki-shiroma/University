package enshud.s1.lexer;
import java.io.*;
import java.util.Scanner;
import enshud.s1.lexer.Token;


public class Lexer {

	/**
	 * サンプルmainメソッド．
	 * 単体テストの対象ではないので自由に改変しても良い．
	 */
	public static void main(final String[] args) {
		// normalの確認
	}

	/**
	 * TODO
	 * 
	 * 開発対象となるLexer実行メソッド．
	 * 以下の仕様を満たすこと．
	 * 
	 * 仕様:
	 * 第一引数で指定されたpasファイルを読み込み，トークン列に分割する．
	 * トークン列は第二引数で指定されたtsファイルに書き出すこと．
	 * 正常に処理が終了した場合は標準出力に"OK"を，
	 * 入力ファイルが見つからない場合は標準エラーに"File not found"と出力して終了すること．
	 * 
	 * @param inputFileName 入力pasファイル名
	 * @param outputFileName 出力tsファイル名
	 */
	public void run(final String inputFileName, final String outputFileName) {

		// TODO
		int height=1;
		int single_quotation= 0;
		int comment_flag=0;
		String str="";
		String result="";
		Token tokenModel = new Token();		
		try {
			File f = new File(inputFileName);
			BufferedReader br = new BufferedReader(new FileReader(f));
			int c = br.read();
			int c_next;
			while (c != -1) {
				
				if (c=='{') comment_flag=1;
				
				if (comment_flag ==0) {

					if (c == '\'') {
						single_quotation++;
						if(single_quotation==2) {
							str=str+(char)c;
							if (str!="") result=result+str+"\t"+"SSTRING"+"\t"+45+"\t"+height+"\n";
							str="";
							c = br.read();
							single_quotation=0;
						}
					}

					if (single_quotation<1) {

						if(c==' ' || c=='<' || c=='>' || c=='+' || c =='-' || c == '*' || c== '(' || c== ')' ||  c== '/' || c=='='||
								c == '[' || c== ']' || c==';' || c== ':' || c=='.' || c==','|| c=='\n' ||c=='\t' ) {

							if (str!="") result=result+outputResult(tokenModel,str,height);
							str="";

							if ( c=='/') {
								result=result+"/"+"\t"+"SDIVD"+"\t"+5+"\t"+height+"\n";
								str="";
							}

							else if ( c=='<') {
								str=str+(char)c;
								c_next = br.read();
								if (c_next=='>' || c_next=='=') {
									str=str+(char)c_next;
									if (str!="") result=result+outputResult(tokenModel,str,height);
									str="";
								}
								else {
									if (str!="") result=result+outputResult(tokenModel,str,height);
									str="";
									if (c_next!=' ')str=str+(char)c_next;
								}
							}

							else if (c=='>') {
								str=str+(char)c;
								c_next = br.read();
								if (c_next=='=') {									
									str=str+(char)c_next;
									if (str!="") result=result+outputResult(tokenModel,str,height);
									str="";
								}
								else {
									if (str!="") result=result+outputResult(tokenModel,str,height);
									str="";
									if (c_next!=' ')str=str+(char)c_next;
								}	
							}

							else if (c==':') {
								str=str+(char)c;
								c_next = br.read();
								if (c_next=='=') {				
									str=str+(char)c_next;
									if (str!="") result=result+outputResult(tokenModel,str,height);
									str="";
								}
								else {
									if (str!="") result=result+outputResult(tokenModel,str,height);
									str="";
									if (c_next!=' ')str=str+(char)c_next;
								}	
							}

							else if (c=='.') {
								str=str+(char)c;
								c_next = br.read();
								if (c_next=='.') {
									str=str+(char)c_next;
									if (str!="") result=result+outputResult(tokenModel,str,height);
									str="";
								}
								else {
									if (str!="") result=result+outputResult(tokenModel,str,height);
									str="";
									if (c_next!=' ')str=str+(char)c_next;
								}	
							}

							else if (c=='+' || c =='-' || c == '*' || c== '(' || c== ')' || c== '=' ||
									c == '[' || c== ']' || c==';' ||  c=='.' || c==',') {
								str=str+(char)c; 
								if (str!="") result=result+outputResult(tokenModel,str,height);
								str="";
							}
							else str="";	
						}
						else str=str+(char)c;		
					}
					else str=str+(char)c; //quotation=1
				} //comment=0

				if (c =='\n') height++; 
				if (c=='}' && comment_flag==1) comment_flag=0; 
				c = br.read();
			}
			br.close();

		} catch (IOException e) {
			System.err.println("File not found");
		}

		try{
			File file = new File(outputFileName);
			FileWriter filewriter = new FileWriter(file);
			filewriter.write(result);
			filewriter.close();
			System.out.println("OK");
		}catch(IOException e){
			System.err.println(e);
		}
	}

	public String outputResult (Token tokenModel, String str, int height) {
		String result;
		String type=tokenModel.tokenMethod(str).type;
		int id=tokenModel.tokenMethod(str).id;
		result=str+"\t"+type+"\t"+id+"\t"+height+"\n";
		return result;
	}
}
