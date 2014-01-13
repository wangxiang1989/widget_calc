package com.revo.widget.calc;

import java.util.Locale;

import org.javia.arity.Symbols;
import org.javia.arity.SyntaxException;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Logic {
	String value = "";
	Context mContext;	
	SharedPreferences sharedPreferences ;
	static final char MINUS = '\u2212';

	
	/**
	 * #########################################################
	 */
	private static final String KEY_DISPLAY = "display";
	//private static final String KEY_DOT = "dot";
	private static final String KEY_OPERATOR = "operator";
	
	 /** remember last operation such as +1 */
    private static String mLastOperation = "";
    
    private Symbols mSymbols = new Symbols();
    private int mLineLength = 0;
    private static final String NAN      = "NaN";
    //private boolean mIsError = false;
    private static final String INFINITY_UNICODE = "\u221e";

    public static final String MARKER_EVALUATE_ON_RESUME = "?";

    // the two strings below are the result of Double.toString() for Infinity & NaN
    // they are not output to the user and don't require internationalization
    private static final String INFINITY = "Infinity";
    
    public static String currentEditable;
	
	/**
	 * #########################################################
	 */
	
	/**
	 * #########################################################
	 */
	
	static boolean isOperator(char c) {
        //plus minus times div
        return "+\u2212\u00d7\u00f7/*".indexOf(c) != -1;
    }
	
	static int lastIndexOfOperator(String text) {
        for (int i = text.length()-1; i >= 0;i--) {
            if(isOperator(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }
	
	void setLineLength(int nDigits) {
       mLineLength = nDigits;		 
    }
	
	/**
	 * #########################################################
	 */	
	public Logic(Context context, String value) {
		this.value = value;
		this.mContext = context;
		
		setLineLength(10);
		sharedPreferences = mContext.getSharedPreferences("calcWidget", 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		
		if(value.equals("=")) return;
		if(value.equals("del")) {
			String now = sharedPreferences.getString(KEY_DISPLAY, "");
			String then = "";
			if(now.length() > 0) {
				then = now.substring(0, now.length()-1);
				if(then.length() == 0) then = "";
			} else if(now.length() == 0){
				then = "";
			}
			editor.putString(KEY_DISPLAY, then);
			editor.commit();
			return;
		}
		
		//第一个符号不能输入+ ×　％
		if((sharedPreferences.getString(KEY_DISPLAY, "") == null 
				|| sharedPreferences.getString(KEY_DISPLAY, "").equals(""))
				&& (value.equals("+") || value.equals("\u00d7") || value.equals("\u00f7"))) {
			return;
		}
		
		//不能连续输入两个.
		if(sharedPreferences.getString(KEY_DISPLAY, "").equals(".") && value.equals(".")) {
			value = "";
		}
		
		//判断是否保留之前的运算结果
		if(isOperatored() && !(value.equals("+") || value.equals("\u00d7") || value.equals("\u00f7") || value.equals("\u2212"))) {			
			editor.putString(KEY_DISPLAY, "");
			editor.commit();
		}
		setOperatored(false);
		
		String text = sharedPreferences.getString(KEY_DISPLAY, "") + value;
		String convert = internalReplace(text);
		editor.putString(KEY_DISPLAY, convert);
		editor.commit();				
	}
	
	void setOperatored(boolean oper) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(KEY_OPERATOR, oper);
		editor.commit();
	}
	boolean isOperatored() {
		return sharedPreferences.getBoolean(KEY_OPERATOR, false);
	}
	
	//检测字符串：一个数字不能有2个点；不能出现连续的运算符
	String internalReplace(String original) {
		String after = "";
		
		//首先去掉连续的运算符
		int length = original.length();
		if(length >= 3 &&/*i+1 < length &&*/ isOperator(original.charAt(length-2)) && isOperator(original.charAt(length-1))
				/*&& (original.charAt(i)==original.charAt(i+1))*/) {
			//如果连续输入运算符，以最后一个为准
			after = original.substring(0, length - 2) + original.charAt(length-1);
		}
		else {
			after = original;
		}
		//Log.i("wangxiang", "===== after: " + after + ", after.length = " + after.length());
		//Log.i("wangxiang", "==> after.charAt(after.length()-1) = " + after.charAt(after.length()-1));
		 
		//去除小数点
		if(after.length() > 2 && after.charAt(after.length()-1) == '.') {
			
			//if(lastIndexOfOperator == -1) { //第一个数字
			//	String
			//} else {
			
			//不允许连续输入两个小数点
			if(after.charAt(after.length()-1) == '.' && after.charAt(after.length()-2) == '.') {
				after = after.substring(0, after.length()-1);
			}
			//不允许一个数字中有两个小数点
				int lastIndexOfOperator = lastIndexOfOperator(after);
				String str = after.substring((lastIndexOfOperator == -1) ? 0 : lastIndexOfOperator,			
										after.length()-2);
				if(str.contains(".")) {
					after = after.substring(0, after.length()-1);
				}
			
			//}
		} //else if(after.length() > 3 && after.charAt(after.length()-2) == '.' && after.charAt(after.length()-1) == '.') {
		//	Log.i("wangxiang", "==> after.charAt(after.length()-1): " + after.charAt(after.length()-1) + ", after.charAt(after.length()) :" +after.charAt(after.length()));
		//	after = after.substring(0, after.length()-2);
		//}
		
		return after;
	}
	
	 String evaluate(String input) throws SyntaxException {
	        if (input.trim().equals("")) {
	            return "";
	        }

	        if (lastIndexOfOperator(input) == -1 || lastIndexOfOperator(input) == 0) {
	            input += mLastOperation;
	        }

	        // drop final infix operators (they can only result in error)
	        int size = input.length();
	        while (size > 0 && isOperator(input.charAt(size - 1))) {
	            input = input.substring(0, size - 1);
	            --size;
	        }
	        
	        double value = mSymbols.eval(input);

	        String result = "";
	        for (int precision = mLineLength; precision > 6; precision--) {
	            result = tryFormattingWithPrecision(value, precision);
	            if (result.length() <= mLineLength) {
	                break;
	            }
	        }
	        return result.replace('-', MINUS).replace(INFINITY, INFINITY_UNICODE);
	    }
	 
	 private String tryFormattingWithPrecision(double value, int precision) {
	        // The standard scientific formatter is basically what we need. We will
	        // start with what it produces and then massage it a bit.
	        String result = String.format(Locale.US, "%" + mLineLength + "." + precision + "g", value).trim();
	        if (result.equals(NAN)) { // treat NaN as Error
	            //mIsError = true;
	            return mContext.getResources().getString(R.string.error);
	        }
	        String mantissa = result;
	        String exponent = null;
	        int e = result.indexOf('e');
	        if (e != -1) {
	            mantissa = result.substring(0, e);

	            // Strip "+" and unnecessary 0's from the exponent
	            exponent = result.substring(e + 1);
	            if (exponent.startsWith("+")) {
	                exponent = exponent.substring(1);
	            }
	            exponent = String.valueOf(Integer.parseInt(exponent));
	        } else {
	            mantissa = result;
	        }

	        int period = mantissa.indexOf('.');
	        if (period == -1) {
	            period = mantissa.indexOf(',');
	        }
	        if (period != -1) {
	            // Strip trailing 0's
	            while (mantissa.length() > 0 && mantissa.endsWith("0")) {
	                mantissa = mantissa.substring(0, mantissa.length() - 1);
	            }
	            if (mantissa.length() == period + 1) {
	                mantissa = mantissa.substring(0, mantissa.length() - 1);
	            }
	        }

	        if (exponent != null) {
	            result = mantissa + 'e' + exponent;
	        } else {
	            result = mantissa;
	        }
	        return result;
	    }
	
	String onEnter() {	
		String text = sharedPreferences.getString(KEY_DISPLAY, "");
		if (lastIndexOfOperator(text) >0) {
            mLastOperation = text.substring(lastIndexOfOperator(text));   
        }
		String result = "";
		try {
			result = evaluate(text);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		//将运算结果保存起来，作为第一个运算数(如果之后第一个数字是运算符的话)
		setOperatored(true);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(KEY_DISPLAY, result);
		editor.commit();
		
		if(result.equals("")) result = "0";
		
		return result;
	}

	public String getDisplay() {
		return sharedPreferences.getString(KEY_DISPLAY, "") == "" ? "0" : sharedPreferences.getString(KEY_DISPLAY, "");
	}
	
	
	public String onAC() {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(KEY_DISPLAY, "");
		editor.commit();
		
		setOperatored(false);
		
		return "0";
	}
}
