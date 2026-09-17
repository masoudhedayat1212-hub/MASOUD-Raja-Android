package com.masoud.raja;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String VERSION = "V7";

    private static final int WHITE = Color.rgb(255,255,255);
    private static final int TEXT = Color.rgb(40,44,52);
    private static final int MUTED = Color.rgb(145,154,164);
    private static final int BORDER = Color.rgb(205,211,217);
    private static final int BLUE = Color.rgb(12,132,216);
    private static final int YELLOW = Color.rgb(255,184,21);
    private static final int SOFT = Color.rgb(248,249,250);

    private EditText origin;
    private EditText destination;
    private EditText travelDate;
    private TextView passengerValue;
    private int adults = 1;
    private int children = 0;
    private int infants = 0;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(WHITE);
        getWindow().setNavigationBarColor(WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        buildUi();
    }

    private int dp(int v){
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable bg(int color, float radius, int strokeColor, int strokeWidth){
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp((int)radius));
        if(strokeWidth > 0) d.setStroke(dp(strokeWidth), strokeColor);
        return d;
    }

    private TextView text(String value, float size, int color, int gravity){
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(gravity);
        t.setIncludeFontPadding(false);
        return t;
    }

    private void addGap(LinearLayout p, int h){
        Space s = new Space(this);
        p.addView(s, new LinearLayout.LayoutParams(1, dp(h)));
    }

    private LinearLayout rowBox(String title, View valueView){
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(10), dp(16), dp(10));
        box.setBackground(bg(WHITE, 12, BORDER, 1));
        TextView label = text(title, 12, MUTED, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        box.addView(label, new LinearLayout.LayoutParams(-1, dp(20)));
        box.addView(valueView, new LinearLayout.LayoutParams(-1, dp(42)));
        return box;
    }

    private EditText plainField(String hint){
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(MUTED);
        e.setTextColor(TEXT);
        e.setTextSize(18);
        e.setSingleLine(true);
        e.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        e.setPadding(0,0,0,0);
        e.setBackgroundColor(Color.TRANSPARENT);
        e.setTextDirection(View.TEXT_DIRECTION_RTL);
        return e;
    }

    private void buildUi(){
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(WHITE);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(18), dp(8), dp(18), dp(8));

        TextView title = text("قطار", 22, TEXT, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        TextView version = text(VERSION, 12, MUTED, Gravity.CENTER);
        version.setBackground(bg(SOFT, 14, BORDER, 1));
        header.addView(title, new LinearLayout.LayoutParams(0, dp(52), 1));
        header.addView(version, new LinearLayout.LayoutParams(dp(48), dp(30)));
        root.addView(header, new LinearLayout.LayoutParams(-1, dp(68)));

        View topLine = new View(this);
        topLine.setBackgroundColor(Color.rgb(236,238,240));
        root.addView(topLine, new LinearLayout.LayoutParams(-1, dp(1)));

        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setGravity(Gravity.CENTER);
        tabs.setPadding(dp(18),0,dp(18),0);

        LinearLayout oneWay = new LinearLayout(this);
        oneWay.setOrientation(LinearLayout.VERTICAL);
        oneWay.setGravity(Gravity.CENTER);
        TextView oneTxt = text("یک طرفه", 16, BLUE, Gravity.CENTER);
        oneTxt.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        View oneLine = new View(this);
        oneLine.setBackground(bg(BLUE, 3, BLUE, 0));
        oneWay.addView(oneTxt, new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout.LayoutParams ulp = new LinearLayout.LayoutParams(dp(92),dp(4));
        ulp.gravity = Gravity.CENTER;
        oneWay.addView(oneLine, ulp);

        TextView round = text("رفت و برگشت", 16, Color.rgb(85,89,96), Gravity.CENTER);
        tabs.addView(oneWay, new LinearLayout.LayoutParams(0, dp(64), 1));
        tabs.addView(round, new LinearLayout.LayoutParams(0, dp(64), 1));
        root.addView(tabs, new LinearLayout.LayoutParams(-1, dp(64)));

        View tabLine = new View(this);
        tabLine.setBackgroundColor(Color.rgb(236,238,240));
        root.addView(tabLine, new LinearLayout.LayoutParams(-1,dp(1)));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18),dp(26),dp(18),dp(28));
        content.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        scroll.addView(content, new ScrollView.LayoutParams(-1,-2));

        FrameLayout stationsFrame = new FrameLayout(this);
        LinearLayout stations = new LinearLayout(this);
        stations.setOrientation(LinearLayout.VERTICAL);
        stations.setBackground(bg(WHITE, 12, BORDER, 1));

        origin = plainField("مبدا (شهر)");
        origin.setPadding(dp(18),0,dp(18),0);
        stations.addView(origin, new LinearLayout.LayoutParams(-1,dp(66)));

        View stationLine = new View(this);
        stationLine.setBackgroundColor(BORDER);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(-1,dp(1));
        slp.setMargins(dp(18),0,dp(18),0);
        stations.addView(stationLine, slp);

        destination = plainField("مقصد (شهر)");
        destination.setPadding(dp(18),0,dp(18),0);
        stations.addView(destination, new LinearLayout.LayoutParams(-1,dp(66)));

        stationsFrame.addView(stations, new FrameLayout.LayoutParams(-1,dp(133)));

        Button swap = new Button(this);
        swap.setText("⇅");
        swap.setTextSize(22);
        swap.setTextColor(Color.rgb(78,85,92));
        swap.setPadding(0,0,0,0);
        swap.setBackground(bg(WHITE, 28, BORDER, 1));
        FrameLayout.LayoutParams swp = new FrameLayout.LayoutParams(dp(56),dp(56),Gravity.LEFT | Gravity.CENTER_VERTICAL);
        swp.leftMargin = dp(34);
        stationsFrame.addView(swap, swp);
        swap.setOnClickListener(v->{
            String a = origin.getText().toString();
            origin.setText(destination.getText().toString());
            destination.setText(a);
        });

        content.addView(stationsFrame, new LinearLayout.LayoutParams(-1,dp(133)));
        addGap(content, 18);

        travelDate = plainField("تاریخ رفت");
        LinearLayout dateBox = rowBox("", travelDate);
        content.addView(dateBox, new LinearLayout.LayoutParams(-1,dp(76)));
        addGap(content, 18);

        passengerValue = text("1 مسافر", 19, TEXT, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        passengerValue.setPadding(0,0,0,0);
        LinearLayout passengersBox = rowBox("مسافران", passengerValue);
        passengersBox.setOnClickListener(v->showPassengerDialog());
        passengerValue.setOnClickListener(v->showPassengerDialog());
        content.addView(passengersBox, new LinearLayout.LayoutParams(-1,dp(82)));
        addGap(content, 28);

        Button search = new Button(this);
        search.setText("جستجوی قطار");
        search.setTextSize(20);
        search.setTextColor(Color.rgb(38,38,38));
        search.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        search.setAllCaps(false);
        search.setBackground(bg(YELLOW, 12, YELLOW, 0));
        search.setPadding(0,0,0,0);
        content.addView(search, new LinearLayout.LayoutParams(-1,dp(58)));

        root.addView(scroll, new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
    }

    private TextView countText(int value){
        TextView t = text(String.valueOf(value), 20, TEXT, Gravity.CENTER);
        t.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        return t;
    }

    private Button squareButton(String value, boolean strong){
        Button b = new Button(this);
        b.setText(value);
        b.setTextSize(25);
        b.setTextColor(WHITE);
        b.setAllCaps(false);
        b.setPadding(0,0,0,0);
        b.setBackground(bg(strong ? BLUE : Color.rgb(105,178,224), 9, Color.TRANSPARENT, 0));
        return b;
    }

    private LinearLayout counterRow(String title, String subtitle, int initial, int min, CounterChanged changed){
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        LinearLayout labelBox = new LinearLayout(this);
        labelBox.setOrientation(LinearLayout.VERTICAL);
        labelBox.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        TextView main = text(title,18,TEXT,Gravity.RIGHT);
        TextView sub = text(subtitle,13,MUTED,Gravity.RIGHT);
        labelBox.addView(main,new LinearLayout.LayoutParams(-1,dp(28)));
        labelBox.addView(sub,new LinearLayout.LayoutParams(-1,dp(24)));
        row.addView(labelBox,new LinearLayout.LayoutParams(0,dp(62),1));

        Button plus = squareButton("+", true);
        TextView count = countText(initial);
        Button minus = squareButton("−", false);
        row.addView(plus,new LinearLayout.LayoutParams(dp(54),dp(54)));
        row.addView(count,new LinearLayout.LayoutParams(dp(58),dp(54)));
        row.addView(minus,new LinearLayout.LayoutParams(dp(54),dp(54)));

        final int[] value = new int[]{initial};
        plus.setOnClickListener(v->{ value[0]++; count.setText(String.valueOf(value[0])); changed.onChanged(value[0]); });
        minus.setOnClickListener(v->{ if(value[0] > min){ value[0]--; count.setText(String.valueOf(value[0])); changed.onChanged(value[0]); } });
        return row;
    }

    private void showPassengerDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(24),dp(22),dp(24),dp(24));
        box.setBackground(bg(WHITE, 22, WHITE, 0));
        box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        TextView title = text("مسافران",22,TEXT,Gravity.RIGHT);
        title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        box.addView(title,new LinearLayout.LayoutParams(-1,dp(42)));
        addGap(box,10);

        final int[] a = new int[]{adults};
        final int[] c = new int[]{children};
        final int[] i = new int[]{infants};
        box.addView(counterRow("بزرگسال","12 سال به بالا",a[0],1,v->a[0]=v),new LinearLayout.LayoutParams(-1,dp(72)));
        addGap(box,10);
        box.addView(counterRow("کودک","2 تا 12 سال",c[0],0,v->c[0]=v),new LinearLayout.LayoutParams(-1,dp(72)));
        addGap(box,10);
        box.addView(counterRow("نوزاد","10 روز تا 2 سال",i[0],0,v->i[0]=v),new LinearLayout.LayoutParams(-1,dp(72)));
        addGap(box,22);

        Button ok = new Button(this);
        ok.setText("تایید");
        ok.setTextSize(19);
        ok.setTextColor(WHITE);
        ok.setAllCaps(false);
        ok.setBackground(bg(BLUE,10,BLUE,0));
        box.addView(ok,new LinearLayout.LayoutParams(-1,dp(58)));
        ok.setOnClickListener(v->{
            adults=a[0]; children=c[0]; infants=i[0];
            int total=adults+children+infants;
            passengerValue.setText(total+" مسافر");
            dialog.dismiss();
        });

        dialog.setContentView(box);
        Window w = dialog.getWindow();
        if(w != null){
            w.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(w.getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.BOTTOM;
            w.setAttributes(lp);
        }
        dialog.show();
        if(w != null){
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(w.getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.BOTTOM;
            w.setAttributes(lp);
        }
    }

    private interface CounterChanged { void onChanged(int value); }
}
