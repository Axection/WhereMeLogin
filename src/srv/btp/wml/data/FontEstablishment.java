package srv.btp.wml.data;

import java.util.Hashtable;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ScaleXSpan;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

public class FontEstablishment {

    private static Hashtable<String, Typeface> fontCache = new Hashtable<String, Typeface>();

    public static Typeface get(String name, Context context) {
    	String pretyped = "fonts/" + name;
        Typeface tf = fontCache.get(pretyped);
        if(tf == null) {
            try {
                tf = Typeface.createFromAsset(context.getAssets(), pretyped);
            }
            catch (Exception e) {
            	e.printStackTrace();
                return null;
            }
            fontCache.put(pretyped, tf);
        }
        return tf;
    }
    
    //So much declaration
    public static void setCustomFont(TextView obj, String font, Context context) {
        if(font == null) {
            return;
        }
        Typeface tf = FontEstablishment.get(font, context);
        if(tf != null) {
            obj.setTypeface(tf);
        }
    }
    public static void setCustomFont(EditText obj, String font, Context context) {
        if(font == null) {
            return;
        }
        Typeface tf = FontEstablishment.get(font, context);
        if(tf != null) {
            obj.setTypeface(tf);
        }
    }
    
    public static void setCustomFont(CheckBox obj, String font, Context context) {
        if(font == null) {
            return;
        }
        Typeface tf = FontEstablishment.get(font, context);
        if(tf != null) {
            obj.setTypeface(tf);
        }
    }
    
    public static void setCustomFont(Button obj, String font, Context context) {
        if(font == null) {
            return;
        }
        Typeface tf = FontEstablishment.get(font, context);
        if(tf != null) {
            obj.setTypeface(tf);
        }
    }
    
    public static void setCustomFont(Spinner obj, String font, Context context) {
        if(font == null) {
            return;
        }
        Typeface tf = FontEstablishment.get(font, context);
        if(tf != null) {
            //obj.get
        }
    }
   
    
    
    public static Spannable applyKerning(CharSequence src, float kerning)
    {
        if (src == null) return null;
        final int srcLength = src.length();
        if (srcLength < 2) return src instanceof Spannable
                                  ? (Spannable)src
                                  : new SpannableString(src);

        final String nonBreakingSpace = "\u00A0";
        final SpannableStringBuilder builder = src instanceof SpannableStringBuilder
                                               ? (SpannableStringBuilder)src
                                               : new SpannableStringBuilder(src);
        for (int i = src.length() - 1; i >= 1; i--)
        {
            builder.insert(i, nonBreakingSpace);
            builder.setSpan(new ScaleXSpan((kerning+1)/10), i, i + 1,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return builder;
    }
}