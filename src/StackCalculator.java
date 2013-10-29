import java.io.*;
import java.util.HashMap;

/**
 * The main logic of this class consist of 3 parts.
 * 
 * The First part check validity of input format, 
 * The Second part transforms infix expression to postfix expression.
 * The Last part calculates transformed postfix expression.
 * <p>
 * Also, the logic that second part uses can be illustrated like below.
 *  (3 + 4)^2 - 3 * (2 + 3)  |  initial input
 * = A^2 - 3*B               |  replace highest precedence with temporary string(A = 34+, B = 23+), and when replacing put postfixed expression in a hash map.
 * = C - D                   |  repeat the same process (C = A2^, D = 3B*)
 * = E                       |  repeat the same process (E = CD-)
 *
 * After replacing all existing operators with keys, then re-replace keys with postfixed expression.
 *   E                       |  initial input
 *   = CD-                   |  replace a key with its value.
 *   = A2^3B*-               |  repeat the same process.
 *   = 34+2^323+*-            |  repeat the same process.
 */
public class StackCalculator {
	private static HashMap<String, String> hashMap = new HashMap<String, String>();

	/**
	 * This char represents "À" which is a starting key that is used when putting key-value in hash map.
	 */ 
	private static char sHashmapKeyChar = '\u00c0';
	private static final char sFinalKeyChar = sHashmapKeyChar;
	private static String sHashmapKeyStr = Character.toString(sHashmapKeyChar);
	
	public static void main(String args[]) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			try {
				String input = br.readLine();
				if (input.compareTo("q") == 0) {
					break;
				}
				
				if(!checkInputFormat(input)) {
					System.out.println("ERROR");
					break;
				} 
				
				String postFixed = infixToPostfix(input);
				// "infixToPostfix" method returns null when its input format isn't correct.
				// which means "checkInputFormat" method do not perform every format check process
				// due to efficiency issue.
				if(postFixed == null) {
					System.out.println("ERROR");
					break;
				}
				System.out.println(postFixed);
				String result = calculatePostfix(postFixed);
				System.out.println(result);
			} catch (Exception e) {
				System.out.println("Exception occured! : " + e.toString());
				e.printStackTrace();
			} finally {
				hashMap = new HashMap<String, String>();
				sHashmapKeyChar = sFinalKeyChar;
				sHashmapKeyStr = Character.toString(sHashmapKeyChar);
			}
		}
	}

	private static boolean checkInputFormat(String input) {
		input = input.replaceAll("\\s+","");
		// First, check input contains unsupported operators.
		String regex = "[\\(\\)\\^\\-\\*/%\\+\\.[0-9]]";
		if(!input.replaceAll(regex, "").equals("")) {
			return false;
		}
				
		// Second, check whether brackets are correctly formatted.
		if(input.contains("(") || input.contains(")")) {
			Stack stack = new Stack();
			for(int i = 0; i < input.length(); i++) {
				char c = input.charAt(i);
				if(c == '(') {
					stack.push(c);
				} else if(c == ')') {
					if(stack.isEmpty()) {
						return false;
					} else {
						stack.pop();
					}
				}
			}
			if(!stack.isEmpty()) {
				return false;
			}
		}
		
		return true;
	}

	private static String calculatePostfix(String input) {
		while(true) {
			String tmpInput = input;
			input = calculateOnePostfix(input);
			if(tmpInput.equals(input))
				break;
		}
		return input;
	}
	
	private static String calculateOnePostfix(String input) {
		String [] s = input.split(" ");
		if(s.length == 1) {
			return input;
		} else if(s.length == 2) {
			s[1].equals("~");
			input = "-" + s[0];
			return input;
		}
		
		for(int i = 0; i < s.length; i++) {
			if(s[i].length() == 1 && isOperator(s[i].charAt(0))) {
				char operator = s[i].charAt(0);
				if(operator == '~') {
					// find other operator and execute that operation.
					// the next chunk will be a operand, and after the next chunk will be a operator.
					// There is no other case in unary operator.
					String result = executeOperation("-" + s[i-1], s[i+1], s[i+2].charAt(0));
					input = input.replaceFirst(s[i-1] + " " + "~" + s[i+1] + " " + "\\" + s[i+2], result);
					return input;
				} else {
					String result = executeOperation(s[i-2], s[i-1], operator);
					input = input.replaceFirst(s[i-2] + " " + s[i-1] + " " + "\\" + Character.toString(operator), result);
					return input;
				}
			}
		}
		return input;
	}

	private static String executeOperation(String operand1, String operand2, char opChar) {
		double firstOp = Double.parseDouble(operand1);
		double secondOp = Double.parseDouble(operand2);
		if(opChar == '+') {
			double result = firstOp + secondOp;
			double tmpResult = result;
			if((double) ((int) tmpResult) == result)
				return String.valueOf((int) result);
			else 
				return String.valueOf(result);
		} else if(opChar == '-') {
			double result = firstOp - secondOp;
			double tmpResult = result;
			if((double) ((int) tmpResult) == result)
				return String.valueOf((int) result);
			else 
				return String.valueOf(result);
		} else if(opChar == '*') {
			double result = firstOp * secondOp;
			double tmpResult = result;
			if((double) ((int) tmpResult) == result)
				return String.valueOf((int) result);
			else 
				return String.valueOf(result);
		} else if(opChar == '/') {
			double result = firstOp / secondOp;
			double tmpResult = result;
			if((double) ((int) tmpResult) == result)
				return String.valueOf((int) result);
			else 
				return String.valueOf(result);
		} else if(opChar == '%') {
			double result = firstOp % secondOp;
			double tmpResult = result;
			if((double) ((int) tmpResult) == result)
				return String.valueOf((int) result);
			else 
				return String.valueOf(result);
		} else if(opChar == '^') {
			double result = Math.pow(firstOp, secondOp);
			double tmpResult = result;
			if((double) ((int) tmpResult) == result)
				return String.valueOf((int) result);
			else 
				return String.valueOf(result);
		} else {
			return "";
		}
	}

	private static String infixToPostfix(String input) {
		input = input.replaceAll("\\s+","");
		input = replaceAllOperatorsWithKeys(input);
		if(input == null) {
			return null;
		}
		
		String output = "";
		while(true) {
			output = replaceOneKeytoExp(input);
			if(input.equals(output))
				break;
			else 
				input = output;
		}
		return output; 
	}

	private static String replaceAllOperatorsWithKeys(String input) {
		// First, replace all brackets with temporary string.
		// The output will only contain operators whose precedence are bigger than 2.
		// ex.) input = (3 + 4)^2 - 3 * (2 + 3) = (34+)^2 - 3*(23+)
		//      output = A ^ 2 - 3 * B  (A = 34+, B = 23+)
		while(input.contains("(")) {
			input = replaceInnerMostBracket(input);
			if(input == null) {
				return null;
			}
		}
		// Second, replace #2 precedence which is power operator with key.
		while(input.contains("^")) {
			input = replacePow(input);
			if(input == null) {
				return null;
			}
		}
		
		//Thrid, replace #3 precedence which is unary minus operator with key.
		while(true) {
			// A minus sign can be a binary operator as well.
			// So, we use whether input and output of replaceUnaryMin method are the same,
			// rather than "input.contains("-")". 
			String tmpInput = input;
			input = replaceUnaryMin(input);
			if(tmpInput.equals(input)) {
				break;
			}
		}
		
		// It goes on and on.
		while(input.contains("*") || input.contains("/") || input.contains("%")) {
			input = replaceMulDivMod(input);
			if(input == null) {
				return null;
			}
		}
		
		while(input.contains("+") || input.contains("-")) {
			input = replacePluMin(input);
			if(input == null) {
				return null;
			}
		}
		
		return input;
	}

	private static String replaceOneKeytoExp(String input) {
		for(int i = 0; i < input.length(); i++) {
			if(input.charAt(i) >= sFinalKeyChar) {
				String key = Character.toString(input.charAt(i));
				input = input.replaceFirst(key, hashMap.get(key));
				return input;
			} 
		}
		return input;
	}

	private static String replaceInnerMostBracket(String input) {
		// This method replaces the inner most bracket when it returns.
		// First, we need to find the inner most bracket.
		// After find it, We'll replace it with a key(some temporary string).
		// ex.) input = (3 + 4)^2 - 3 * (2 + 3) = (34+)^2 - 3*(23+)
		//      output = A ^ 2 - 3 * B  (A = 34+, B = 23+)
		int innerMostOpeningBracketIndex = 0;
		int innerMostClosingBracketIndex = 0;
		for(int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if(c == '(') {
				innerMostOpeningBracketIndex = i;
			} else if(c == ')') {
				innerMostClosingBracketIndex = i;
				break;
			}
		}
		String insideBracket = input.substring(innerMostOpeningBracketIndex + 1, innerMostClosingBracketIndex);
		// "insideBracket" only contains operators whose precedences are bigger than 2.
		
		while(insideBracket.contains("^")) {
			insideBracket = replacePow(insideBracket);
		}
		
		while(true) {
			String tmpInsideBracket = insideBracket;
			insideBracket = replaceUnaryMin(insideBracket);
			if(tmpInsideBracket.equals(insideBracket)) {
				break;
			}
		}
		
		while(insideBracket.contains("*") ||
				insideBracket.contains("/") ||
				insideBracket.contains("%")) {
			insideBracket = replaceMulDivMod(insideBracket);
		}
		
		while(insideBracket.contains("+") ||
				insideBracket.contains("-")) {
			// All unary minus operator is replaced with keys.
			// So this time, we can use insideBracket.contains("-").
			insideBracket = replacePluMin(insideBracket);
		}
		
		String regex = "";
		for(int i = innerMostOpeningBracketIndex + 1; i < innerMostClosingBracketIndex; i++) {
			char c = input.charAt(i);
			// The metacharacters supported by Java pattern API are: <([{\^-=$!|]})?*+.>
			if(c == '^' || c == '-' || c == '*' || c == '+') {
				regex = regex + "\\" + Character.toString(c);				
			} else {
				regex = regex + Character.toString(c);
			}
		}
		String output = input.replaceFirst("\\(" + regex + "\\)", insideBracket);
		return output;
	}

	private static String replaceUnaryMin(String input) {
		if(!input.contains("-")) {
			return input;
		}
		// We can check that a minus sign is unary minus operator by checking,
		// 1) The very first character is minus sign,
		// 2) There is another operators(including opening bracket) before minus sign.
		
		// handle 1) case.
		if(input.charAt(0) == '-') {
			int unaryOperandIndex = 0;
			for(int i = 1; i < input.length(); i++) {
				if(!isNumeric(input.charAt(i))){
					unaryOperandIndex = i;
					break;
				}
			}
			
			if(unaryOperandIndex == 0) {
				// This case input is just a unary operator and a single operand(ex. - 15)
				unaryOperandIndex = input.length(); 
			}
			
			String unaryOperand = input.substring(1, unaryOperandIndex); 
			String postfix = unaryOperand + " " + "~";
			hashMap.put(sHashmapKeyStr, postfix);
			
			String regex = "\\-" + unaryOperand;
			String output = input.replaceFirst(regex, sHashmapKeyStr);
			sHashmapKeyStr = String.valueOf(++sHashmapKeyChar);
			return output;	
		}
		
		// handle 2) case.
		for(int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if(c == '-') {
				char charBeforeMinSign = input.charAt(i-1);
				if(charBeforeMinSign == '(' ||
					charBeforeMinSign == '^' ||
					charBeforeMinSign == '*' ||
					charBeforeMinSign == '/' ||
					charBeforeMinSign == '%' ||
					charBeforeMinSign == '+' ||
					charBeforeMinSign == '_') {
					// It's a unary minus operator!
					int unaryOperandStartIndex = i+1;
					int unaryOperandEndIndex = 0;
					for(int j = i+1; j < input.length(); j++) {
						if(!isNumeric(input.charAt(j))) {
							unaryOperandEndIndex = j;
							break;
						}
					}
					
					if(unaryOperandEndIndex == 0) {
						// This case input is ended with just a unary operator and a single operand(ex. 10 - - 15)
						unaryOperandEndIndex = input.length(); 
					}
					
					String unaryOperand = input.substring(unaryOperandStartIndex, unaryOperandEndIndex); 
					String postfix = unaryOperand + " " + "~";
					hashMap.put(sHashmapKeyStr, postfix);
					
					String regex = "\\-" + unaryOperand;
					String output = input.replaceFirst(regex, sHashmapKeyStr);
					sHashmapKeyStr = String.valueOf(++sHashmapKeyChar);
					return output;	
				}
			}
		}
		// if there is no unary minus operator, just return what take in :)
		return input;
	}

	private static String replacePluMin(String input) {
		// replace 2-3 with K, and save the pair(K, 23-) in hashMap.
		if(!input.contains("+") && !input.contains("-")) {
			return input;
		}
		
		String operator = "";
		String [] s = null;
		String [] s2 = null;
		if(input.contains("+")) {
			operator = "+";
			s = input.split("\\-");
			for(int i = 0; i < s.length; i++) {
				if(s[i].contains("+")) {
					s2 = s[i].split("\\+");
					break;
				}
			}
		} else if(input.contains("-")) {
			operator = "-";
			s = input.split("\\+");
			for(int i = 0; i < s.length; i++) {
				if(s[i].contains("-")) {
					s2 = s[i].split("\\-");
					break;
				}
			}
		}
		
		for(int i = 0; i < s2.length; i++) {
			if(s2[i].equals(""))
				return null;
		}
		
		String postfix = s2[0] + " " + s2[1] + " " + operator; // Two " " are added for white spaces.
		hashMap.put(sHashmapKeyStr, postfix);
		
		String regex = s2[0] + "\\" + operator + s2[1];
		String output = input.replaceFirst(regex, sHashmapKeyStr);
		sHashmapKeyStr = String.valueOf(++sHashmapKeyChar);
		return output;
	}

	private static String replaceMulDivMod(String input) {
		if(!input.contains("*") && !input.contains("/") && !input.contains("%")) {
			return input;
		}
		
		String operator = "";
		String [] s = null;
		String [] s2 = null;
		if(input.contains("*")) {
			operator = "*";
			s = input.split("[/%\\+\\-]");
			for(int i = 0; i < s.length; i++) {
				if(s[i].contains("*")) {
					s2 = s[i].split("\\*");
					break;
				}
			}
		} else if(input.contains("/")) {
			operator = "/";
			s = input.split("[\\*%\\+\\-]");
			for(int i = 0; i < s.length; i++) {
				if(s[i].contains("/")) {
					s2 = s[i].split("/");
					break;
				}
			}
		} else if(input.contains("%")) {
			operator = "%";
			s = input.split("[\\*/\\+\\-]");
			for(int i = 0; i < s.length; i++) {
				if(s[i].contains("%")) {
					s2 = s[i].split("%");
					break;
				}
			}
		}

		for(int i = 0; i < s2.length; i++) {
			if(s2[i].equals(""))
				return null;
		}
		
		String postfix = s2[0] + " " + s2[1] + " " + operator;
		hashMap.put(sHashmapKeyStr, postfix);
		
		String regex = s2[0] + "\\" + operator + s2[1];
		String output = input.replaceFirst(regex, sHashmapKeyStr);
		sHashmapKeyStr = String.valueOf(++sHashmapKeyChar);
		return output;
	}

	private static String replacePow(String input) {
		if(!input.contains("^")) {
			return input;
		}
		
		String [] s = input.split("[\\-\\*/%\\+]"); // All operators except power operator(^).
		String [] s2 = null;
		for(int i = 0; i < s.length; i++) {
			if(s[i].contains("^")) {
				s2 = s[i].split("\\^");
				break;
			}
		}

		// if character right before or right after of "^" is a "^"operator, input isn't correct,
		// So, return null to indicate input ERROR.
		for(int i = 0; i < s2.length; i++) {
			if(s2[i].equals(""))
				return null;
		}

		
		String postfix = s2[s2.length - 2] + " "
				+ s2[s2.length - 1] + " "
				+ "^";
		hashMap.put(sHashmapKeyStr, postfix);

		String regex = s2[s2.length - 2] + "\\^" + s2[s2.length - 1];
		String output = input.replaceFirst(regex, sHashmapKeyStr);
		sHashmapKeyStr = String.valueOf(++sHashmapKeyChar);
		return output;
	}

	private static boolean isOperator(char c) {
		if(c == '(' || c == ')' || c == '^' || c == '-' || c == '~' ||
		   c == '*' || c == '/' || c == '%' || c == '+') {
			return true;
		} else { 
			return false;
		}
	}

	private static boolean isNumeric(char currentChar) {
		if(currentChar == '0' || currentChar == '1' || currentChar == '2' ||
		   currentChar == '3' || currentChar == '4' || currentChar == '5' ||
		   currentChar == '6' || currentChar == '7' || currentChar == '8' ||
		   currentChar == '9' || currentChar == '.' ) {
			return true;
		} else { 
			return false;
		}
	}
}

class Stack {
	private final int DEFAULT_MAX_SIZE = 256;
	
	private char [] stackArray;
	private int currentIndex;
	private int maxSize;
	
	public Stack() {
		this.maxSize = DEFAULT_MAX_SIZE;
		this.stackArray = new char [this.maxSize];
		this.currentIndex = -1;
	}
	
	public Stack(int size) {
		this.maxSize = size;
		this.stackArray = new char [this.maxSize];
		this.currentIndex = -1;
	}
	
	public void push(char input) {
		stackArray[++currentIndex] = input; 
	}
	 
	public char pop() {
		return stackArray[currentIndex--];
	}
	
	public boolean isEmpty() {
		return (currentIndex == -1); 
	}
}