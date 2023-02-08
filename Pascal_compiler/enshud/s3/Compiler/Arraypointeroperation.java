package enshud.s4.compiler;

public class Arraypointeroperation{

	int [] arraypointstack = {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};

	public void arrayPointerPush(String str[],String variable[][], String scope){
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


	public int  arrayPointer (int arraypoint, String mode) { //push or pop
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


}