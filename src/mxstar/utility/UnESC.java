package mxstar.utility;

public class UnESC {
	private String str;

	public UnESC(String str) {
		this.str = unEsc(str);
	}

	public static String unEsc(String str) {
		StringBuilder ss = new StringBuilder();
		for(int i = 0; i < str.length(); i++) {
			if(str.charAt(i) == '\\') {
//				System.err.println(i);
				if(i + 1 < str.length()) {
					switch (str.charAt(i + 1)) {
						case 'n':
							ss.append('\n');
							break;
						case 'r':
							ss.append('\r');
							break;
						case 't':
							ss.append('\t');
							break;
						case '0':
							ss.append('\0');
							break;
						case '\\':
							ss.append('\\');
							break;
						case '"':
							ss.append('"');
							break;
						default:
							throw new Error("Undefined escaped character(s)");
					}
					i++;
				}
				else {
					throw new Error("Expect an escaped character at the end of StringConst");
				}
			}
			else {
				ss.append(str.charAt(i));
			}
		}
		return ss.toString();
	}

	public String getStr() {
		return str;
	}

	public static String enEsc(String str) {
		StringBuilder ss = new StringBuilder();
		for(int i = 0; i < str.length(); i++) {
			switch (str.charAt(i)) {
				case '\n':
					ss.append("\\n");
					break;
				case '\r':
					ss.append("\\r");
					break;
				case '\t':
					ss.append("\\t");
					break;
				case '\0':
					ss.append("\\0");
					break;
				case '\\':
					ss.append("\\\\");
					break;
				default:
					ss.append(str.charAt(i));
			}
		}

		return ss.toString();
	}
}
