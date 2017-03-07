package audio.abc1;

import static audio.Constants.NL;

public class Header {
	public String X = ""; 
	public String T = ""; 
	public String S = "";
	public String M = ""; 
	public String L = ""; 
	public String Q = ""; 
	public String K = ""; 
	
	public Header() {
	}
	
	public Header(String X, String T, String S, String M, String L, String Q, String K) {
		this.X = X; 
		this.T = T; 
		this.S = S;
		this.M = M; 
		this.L = L; 
		this.Q = Q; 
		this.K = K; 
	}
	
	public String toAbc() {
		String s = "";
		if (!X.equals("")) s += "X:" + X + NL;
		if (!T.equals("")) s += "T:" + T + NL;
		if (!S.equals("")) s += "S:" + S + NL;
		if (!M.equals("")) s += "M:" + M + NL;
		if (!L.equals("")) s += "L:" + L + NL;
		if (!Q.equals("")) s += "Q:" + Q + NL;
		if (!K.equals("")) s += "K:" + K + NL;
		return s; 
	}

	public Header clone() {
		return new Header(X, T, S, M, L, Q, K);
	}
}
