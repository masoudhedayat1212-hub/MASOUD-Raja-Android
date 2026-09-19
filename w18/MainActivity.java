package com.masoud.raja;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Ringtone;
import android.app.RingtoneManager;
import android.content.ClipData;
import android.content.Intent;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.InputType;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(244,249,255);
    private static final int PANEL = Color.rgb(255,255,255);
    private static final int PANEL_2 = Color.rgb(225,241,255);
    private static final int FIELD = Color.rgb(255,255,255);
    private static final int TEXT = Color.rgb(18,48,74);
    private static final int MUTED = Color.rgb(105,139,166);
    private static final int BLUE = Color.rgb(0,132,255);
    private static final int CYAN = Color.rgb(0,206,255);
    private static final int GREEN = Color.rgb(0,200,120);
    private static final int RED = Color.rgb(255,67,82);
    private static final int BORDER = Color.rgb(185,220,246);
    private static final int RINGTONE_REQUEST = 4205;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private WebView webView;
    private EditText phone, password, origin, destination, travelDate, trainNumber, minPrice, maxPrice, adults, children, refresh;
    private SeekBar refreshSlider;
    private String passengerType = "normal";
    private CheckBox priceMode, coupe, alarm;
    private TextView status, logView, passengerSummary;
    private ScrollView logScroll;
    private LinearLayout runPanel, browserPanel;
    private Button startBtn, stopBtn, tabRun, tabBrowser;
    private boolean running = false;
    private boolean loggedIn = false;
    private boolean searchSubmitted = false;
    private boolean reserved = false;
    private boolean alarmPlayed = false;
    private boolean reauthRequired = false;
    private int sessionGeneration=0;
    private Runnable sessionDeadline;
    private boolean logoutInProgress = false;
    private boolean loginInProgress = false;
    private int loginRetryCount = 0;
    private String lastFinishedUrl = "";
    private long lastFinishedAt = 0L;
    private int actionState = 0;
    private Uri selectedAlarmUri;
    private long nextRefreshAt = 0L;
    private Runnable monitorRunnable;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        buildUi();
        setupWebView();
        loadAccount();
        showPanel(runPanel);
    }

    private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }

    private GradientDrawable bg(int color, int radius, int strokeColor, int stroke){
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); if(stroke>0)d.setStroke(dp(stroke),strokeColor); return d;
    }
    private TextView label(String text) {
        TextView v = new TextView(this); v.setText(text); v.setTextColor(TEXT); v.setTextSize(14); v.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); v.setPadding(dp(4),dp(5),dp(4),dp(4)); return v;
    }
    private EditText field(String hint) {
        EditText e = new EditText(this); e.setHint(hint); e.setHintTextColor(MUTED); e.setTextColor(TEXT); e.setTextSize(16); e.setSingleLine(true); e.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); e.setBackground(bg(FIELD,14,BORDER,1)); e.setPadding(dp(13),dp(8),dp(13),dp(8)); e.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(50))); return e;
    }
    private AutoCompleteTextView stationField(String hint){
        AutoCompleteTextView e=new AutoCompleteTextView(this); e.setHint(hint); e.setHintTextColor(MUTED); e.setTextColor(TEXT); e.setTextSize(16); e.setSingleLine(true); e.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); e.setBackground(bg(FIELD,14,BORDER,1)); e.setPadding(dp(13),dp(8),dp(13),dp(8)); e.setThreshold(0);
        String[] cities={"تهران","مشهد","قم","اصفهان","شیراز","تبریز","اهواز","کرمان","یزد","رشت","ساری","کرج","قزوین","اراک","بندرعباس","زنجان","همدان","گرگان","شاهرود","نیشابور","سبزوار","طبس","کاشان","اندیمشک","خرمشهر"};
        ArrayAdapter<String> a=new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,cities){
            private View style(View v){ TextView t=(TextView)v; t.setTextColor(TEXT); t.setTextSize(17); t.setTypeface(null,1); t.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); t.setPadding(dp(18),dp(12),dp(18),dp(12)); t.setBackgroundColor(PANEL); return t; }
            @Override public View getView(int position,View convertView,ViewGroup parent){ return style(super.getView(position,convertView,parent)); }
            @Override public View getDropDownView(int position,View convertView,ViewGroup parent){ return style(super.getDropDownView(position,convertView,parent)); }
        };
        e.setAdapter(a); e.setDropDownBackgroundDrawable(bg(PANEL,12,BORDER,1)); e.setOnClickListener(v->e.showDropDown()); e.setOnFocusChangeListener((v,has)->{if(has)e.showDropDown();}); return e;
    }
    private Button button(String text, int color) {
        Button b = new Button(this); b.setText(text); b.setTextColor(color==PANEL_2?TEXT:Color.WHITE); b.setTextSize(15); b.setAllCaps(false); b.setBackground(bg(color,14,color,0)); b.setMinHeight(dp(48)); b.setPadding(dp(8),0,dp(8),0); return b;
    }
    private LinearLayout section(String title) {
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(13),dp(11),dp(13),dp(13)); box.setBackground(bg(PANEL,16,BORDER,1));
        TextView t = label(title); t.setTextSize(17); t.setTypeface(null,1); box.addView(t,new LinearLayout.LayoutParams(-1,dp(38))); return box;
    }
    private void gap(LinearLayout p, int h){ Space s=new Space(this); p.addView(s,new LinearLayout.LayoutParams(1,dp(h))); }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(BG); root.setPadding(0,dp(4),0,0);
        FrameLayout headerFrame = new FrameLayout(this);
        ImageView referenceHeader = new ImageView(this);
        referenceHeader.setImageResource(R.drawable.w2_header_reference);
        referenceHeader.setScaleType(ImageView.ScaleType.CENTER_CROP);
        referenceHeader.setAdjustViewBounds(false);
        headerFrame.addView(referenceHeader,new FrameLayout.LayoutParams(-1,-1));
        TextView version = new TextView(this);
        version.setText("W18");
        version.setTextColor(Color.BLACK);
        version.setTextSize(18);
        version.setTypeface(null,1);
        version.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams versionLp = new FrameLayout.LayoutParams(dp(58),dp(40),Gravity.TOP|Gravity.RIGHT);
        versionLp.setMargins(0,dp(8),dp(10),0);
        headerFrame.addView(version,versionLp);
        root.addView(headerFrame,new LinearLayout.LayoutParams(-1,dp(171)));
        LinearLayout tabs = new LinearLayout(this); tabs.setOrientation(LinearLayout.HORIZONTAL); tabs.setPadding(dp(10),0,dp(10),dp(10)); tabs.setGravity(Gravity.CENTER);
        tabRun=button("⌂  خانه",BLUE); tabBrowser=button("▣  رجا",PANEL_2);
        tabs.addView(tabRun,new LinearLayout.LayoutParams(0,dp(48),1)); gapH(tabs,6); tabs.addView(tabBrowser,new LinearLayout.LayoutParams(0,dp(48),1)); root.addView(tabs);
        FrameLayout body = new FrameLayout(this); root.addView(body,new LinearLayout.LayoutParams(-1,0,1));
        runPanel = makeRunPanel(); browserPanel = makeBrowserPanel(); body.addView(runPanel); body.addView(browserPanel);
        tabRun.setOnClickListener(v->showPanel(runPanel)); tabBrowser.setOnClickListener(v->showPanel(browserPanel)); setContentView(root);
    }

    private void gapH(LinearLayout p,int w){ Space s=new Space(this); p.addView(s,new LinearLayout.LayoutParams(dp(w),1)); }
    private void tabStyle(Button b,boolean active){ b.setBackground(bg(active?BLUE:PANEL_2,14,active?CYAN:BORDER,1)); b.setTextColor(active?Color.WHITE:MUTED); }
    private void showPanel(View target){ runPanel.setVisibility(target==runPanel?View.VISIBLE:View.GONE); browserPanel.setVisibility(target==browserPanel?View.VISIBLE:View.GONE); if(tabRun!=null){ tabStyle(tabRun,target==runPanel); tabStyle(tabBrowser,target==browserPanel); } }

    private LinearLayout makeRunPanel(){
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); LinearLayout p=new LinearLayout(this); p.setOrientation(LinearLayout.VERTICAL); p.setPadding(dp(10),0,dp(10),dp(24)); scroll.addView(p);
        LinearLayout route=section("انتخاب مسیر");
        FrameLayout routeFrame=new FrameLayout(this);
        LinearLayout routeFields=new LinearLayout(this); routeFields.setOrientation(LinearLayout.VERTICAL); routeFields.setBackground(bg(FIELD,14,BORDER,1));
        origin=stationField("مبدا (شهر)"); origin.setBackgroundColor(Color.TRANSPARENT); origin.setCompoundDrawablesWithIntrinsicBounds(0,0,android.R.drawable.ic_menu_mylocation,0);
        destination=stationField("مقصد (شهر)"); destination.setBackgroundColor(Color.TRANSPARENT); destination.setCompoundDrawablesWithIntrinsicBounds(0,0,android.R.drawable.ic_menu_mylocation,0);
        routeFields.addView(origin,new LinearLayout.LayoutParams(-1,dp(56)));
        View divider=new View(this); divider.setBackgroundColor(BORDER); routeFields.addView(divider,new LinearLayout.LayoutParams(-1,dp(1)));
        routeFields.addView(destination,new LinearLayout.LayoutParams(-1,dp(56)));
        routeFrame.addView(routeFields,new FrameLayout.LayoutParams(-1,dp(113)));
        Button swap=button("⇅",PANEL); swap.setTextColor(TEXT); swap.setTextSize(22); swap.setBackground(bg(PANEL,30,BORDER,1));
        FrameLayout.LayoutParams swapLp=new FrameLayout.LayoutParams(dp(58),dp(58),Gravity.LEFT|Gravity.CENTER_VERTICAL); swapLp.setMargins(dp(24),0,0,0); routeFrame.addView(swap,swapLp);
        swap.setOnClickListener(v->{ String a=origin.getText().toString(); origin.setText(destination.getText().toString()); destination.setText(a); pressAction(swap,()->toast("مبدا و مقصد جابه‌جا شد")); });
        route.addView(routeFrame,new LinearLayout.LayoutParams(-1,dp(113))); gap(route,10);
        LinearLayout drow=new LinearLayout(this); drow.setOrientation(LinearLayout.HORIZONTAL); drow.setGravity(Gravity.CENTER_VERTICAL); travelDate=field("تاریخ رفت"); travelDate.setFocusable(false); travelDate.setCompoundDrawablesWithIntrinsicBounds(0,0,android.R.drawable.ic_menu_my_calendar,0); travelDate.setOnClickListener(v->showPersianCalendar()); drow.addView(travelDate,new LinearLayout.LayoutParams(-1,dp(54))); route.addView(drow); p.addView(route); gap(p,9);
        adults=field(""); adults.setText("1"); adults.setFocusable(false);
        children=field(""); children.setText("0"); children.setFocusable(false);
        passengerSummary=label("مسافران     ۱ مسافر");
        passengerSummary.setTextSize(17); passengerSummary.setTypeface(null,1); passengerSummary.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        passengerSummary.setPadding(dp(16),dp(8),dp(16),dp(8)); passengerSummary.setBackground(bg(FIELD,14,BORDER,1));
        passengerSummary.setText("⚙   مسافران     ۱ مسافر"); passengerSummary.setTextColor(TEXT); passengerSummary.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
        passengerSummary.setOnClickListener(v->showPassengerDialog());
        p.addView(passengerSummary,new LinearLayout.LayoutParams(-1,dp(58))); gap(p,9);
        LinearLayout search=section("تنظیمات جستجو"); trainNumber=field("شماره قطار"); trainNumber.setInputType(InputType.TYPE_CLASS_NUMBER); search.addView(trainNumber); gap(search,7);
        priceMode=new CheckBox(this); priceMode.setText("جستجو با بازه قیمت"); priceMode.setTextColor(TEXT); priceMode.setButtonTintList(ColorStateList.valueOf(BLUE)); search.addView(priceMode); minPrice=field("از قیمت (ریال)"); minPrice.setInputType(InputType.TYPE_CLASS_NUMBER); maxPrice=field("تا قیمت (ریال)"); maxPrice.setInputType(InputType.TYPE_CLASS_NUMBER); enablePriceFormatting(minPrice); enablePriceFormatting(maxPrice); search.addView(minPrice); gap(search,6); search.addView(maxPrice); gap(search,7);
        coupe=new CheckBox(this); coupe.setText("فقط کوپه دربست"); coupe.setTextColor(TEXT); coupe.setButtonTintList(ColorStateList.valueOf(BLUE)); alarm=new CheckBox(this); alarm.setText("آلارم صوتی پیدا شدن بلیت"); alarm.setTextColor(TEXT); alarm.setTypeface(null,1); alarm.setButtonTintList(ColorStateList.valueOf(BLUE)); alarm.setChecked(true); search.addView(coupe); search.addView(alarm); Button alarmTone=button("♫ انتخاب صدای آلارم",PANEL_2); alarmTone.setTextColor(TEXT); alarmTone.setOnClickListener(v->chooseAlarmTone()); search.addView(alarmTone,new LinearLayout.LayoutParams(-1,dp(46))); p.addView(search); gap(p,9);
        LinearLayout live=section("کنترل زنده"); LinearLayout rr=new LinearLayout(this); rr.setOrientation(LinearLayout.HORIZONTAL); rr.setGravity(Gravity.CENTER_VERTICAL); refresh=field(""); refresh.setFocusable(false); refresh.setGravity(Gravity.CENTER); refreshSlider=new SeekBar(this); refreshSlider.setMax(119); float savedRefresh=getPreferences(MODE_PRIVATE).getFloat("refresh_seconds",2.0f); int savedProgress=Math.max(0,Math.min(119,Math.round((savedRefresh-0.5f)*2f))); refreshSlider.setProgress(savedProgress); refresh.setText(String.format(Locale.US,"%.1f ثانیه",0.5+(savedProgress*0.5))); refreshSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){ public void onProgressChanged(SeekBar bar,int progress,boolean fromUser){ double value=0.5+(progress*0.5); refresh.setText(String.format(Locale.US,"%.1f ثانیه",value)); if(fromUser)getPreferences(MODE_PRIVATE).edit().putFloat("refresh_seconds",(float)value).apply(); } public void onStartTrackingTouch(SeekBar bar){} public void onStopTrackingTouch(SeekBar bar){} }); rr.addView(refreshSlider,new LinearLayout.LayoutParams(0,dp(52),1)); gapH(rr,7); rr.addView(refresh,new LinearLayout.LayoutParams(dp(105),dp(50))); live.addView(rr); p.addView(live); gap(p,9);
        LinearLayout account=section("حساب رجا"); phone=field("شماره موبایل حساب رجا"); phone.setInputType(InputType.TYPE_CLASS_PHONE); password=field("رمز عبور"); password.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD); account.addView(phone); gap(account,8); account.addView(password); p.addView(account); gap(p,9);
        LinearLayout actions=new LinearLayout(this); actions.setOrientation(LinearLayout.HORIZONTAL); startBtn=button("▶ شروع ربات",GREEN); stopBtn=button("■ توقف",RED); actions.addView(stopBtn,new LinearLayout.LayoutParams(0,dp(54),1)); gapH(actions,7); actions.addView(startBtn,new LinearLayout.LayoutParams(0,dp(54),2)); p.addView(actions); gap(p,9);
        status=label("● آماده"); status.setTextColor(GREEN); status.setBackground(bg(PANEL,12,BORDER,1)); status.setPadding(dp(12),dp(10),dp(12),dp(10)); p.addView(status,new LinearLayout.LayoutParams(-1,dp(46))); gap(p,9);
        LinearLayout logs=section("لاگ زنده رجا / ربات"); LinearLayout logActions=new LinearLayout(this); logActions.setOrientation(LinearLayout.HORIZONTAL); Button clearLog=button("پاک‌کردن لاگ",PANEL_2); Button copy=button("کپی لاگ",PANEL_2); logActions.addView(clearLog,new LinearLayout.LayoutParams(0,dp(44),1)); gapH(logActions,7); logActions.addView(copy,new LinearLayout.LayoutParams(0,dp(44),1)); logs.addView(logActions); gap(logs,6); logView=new TextView(this); logView.setTextColor(TEXT); logView.setTextSize(12); logView.setGravity(Gravity.RIGHT); logView.setTextIsSelectable(true); logView.setBackground(bg(FIELD,12,BORDER,1)); logView.setPadding(dp(10),dp(10),dp(10),dp(10)); logScroll=new ScrollView(this); logScroll.setFillViewport(true); logScroll.addView(logView,new ScrollView.LayoutParams(-1,-2)); logs.addView(logScroll,new LinearLayout.LayoutParams(-1,dp(170))); p.addView(logs);
        copy.setOnClickListener(v->{ ClipboardManager cm=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE); cm.setPrimaryClip(ClipData.newPlainText("MASOUD Raja Log",logView.getText())); toast("لاگ کپی شد"); }); clearLog.setOnClickListener(v->{ logView.setText(""); toast("لاگ پاک شد"); });
        startBtn.setOnClickListener(v->pressAction(startBtn,()->{ if(running){ toast("جستجو در حال اجراست"); return; } startBot(); })); stopBtn.setOnClickListener(v->pressAction(stopBtn,()->{ if(!running){ toast("ربات متوقف است"); return; } stopBot("توقف توسط کاربر"); })); priceMode.setOnCheckedChangeListener((b,checked)->{ trainNumber.setEnabled(!checked); minPrice.setEnabled(checked); maxPrice.setEnabled(checked); }); minPrice.setEnabled(false); maxPrice.setEnabled(false);
        updateActionButtons();
        LinearLayout container=new LinearLayout(this); container.setOrientation(LinearLayout.VERTICAL); container.addView(scroll,new LinearLayout.LayoutParams(-1,-1)); return container;
    }

    private void showPassengerDialog(){
        final int[] counts={parseInt(adults.getText().toString(),1),parseInt(children.getText().toString(),0)};
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(16),dp(8),dp(16),dp(14)); box.setBackgroundColor(PANEL);
        TextView title=label("مسافران"); title.setTextSize(20); title.setTypeface(null,1); box.addView(title,new LinearLayout.LayoutParams(-1,dp(48)));
        TextView[] values={new TextView(this),new TextView(this)};
        box.addView(makeDialogPassengerRow("بزرگسال (۱۲ سال به بالا)",0,counts,values)); gap(box,6);
        box.addView(makeDialogPassengerRow("کودک (۲ تا ۱۲ سال)",1,counts,values));
        TextView limit=label("ⓘ حداکثر تعداد مسافران ۶ نفر است."); limit.setTextColor(RED); limit.setGravity(Gravity.CENTER); box.addView(limit,new LinearLayout.LayoutParams(-1,dp(44)));
        View line=new View(this); line.setBackgroundColor(BORDER); box.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));
        RadioGroup group=new RadioGroup(this); group.setOrientation(RadioGroup.VERTICAL); group.setGravity(Gravity.RIGHT);
        RadioButton normal=dialogRadio("مسافران عادی","normal"); RadioButton men=dialogRadio("ویژه برادران","men"); RadioButton women=dialogRadio("ویژه خواهران","women");
        group.addView(normal); group.addView(men); group.addView(women);
        if("men".equals(passengerType))men.setChecked(true); else if("women".equals(passengerType))women.setChecked(true); else normal.setChecked(true);
        group.setOnCheckedChangeListener((g,id)->{ if(id==men.getId())passengerType="men"; else if(id==women.getId())passengerType="women"; else passengerType="normal"; });
        box.addView(group);
        Button confirm=button("تأیید",BLUE); box.addView(confirm,new LinearLayout.LayoutParams(-1,dp(54)));
        AlertDialog dialog=new AlertDialog.Builder(this).setView(box).create();
        confirm.setOnClickListener(v->{ adults.setText(String.valueOf(counts[0])); children.setText(String.valueOf(counts[1])); updatePassengerSummary(); dialog.dismiss(); });
        dialog.setOnShowListener(d->{ WindowManager.LayoutParams lp=dialog.getWindow().getAttributes(); lp.width=WindowManager.LayoutParams.MATCH_PARENT; dialog.getWindow().setAttributes(lp); });
        dialog.show();
    }
    private RadioButton dialogRadio(String text,String tag){ RadioButton r=new RadioButton(this); r.setId(View.generateViewId()); r.setText(text); r.setTag(tag); r.setTextColor(TEXT); r.setTextSize(16); r.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); r.setButtonTintList(ColorStateList.valueOf(BLUE)); return r; }
    private LinearLayout makeDialogPassengerRow(String title,int index,int[] counts,TextView[] values){
        LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        Button minus=button("−",BLUE), plus=button("+",BLUE); TextView value=label(String.valueOf(counts[index])); value.setGravity(Gravity.CENTER); value.setTextSize(18); values[index]=value;
        TextView name=label(title); name.setTextSize(15); name.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        minus.setOnClickListener(v->{ int min=index==0?1:0; if(counts[index]>min){ counts[index]--; value.setText(String.valueOf(counts[index])); } });
        plus.setOnClickListener(v->{ int total=counts[0]+counts[1]; if(total>=6){ toast("حداکثر تعداد مسافران ۶ نفر است"); return; } counts[index]++; value.setText(String.valueOf(counts[index])); });
        row.addView(minus,new LinearLayout.LayoutParams(dp(52),dp(48))); gapH(row,5); row.addView(value,new LinearLayout.LayoutParams(dp(42),dp(48))); gapH(row,5); row.addView(plus,new LinearLayout.LayoutParams(dp(52),dp(48))); row.addView(name,new LinearLayout.LayoutParams(0,dp(58),1)); return row;
    }
    private void updatePassengerSummary(){ int total=parseInt(adults.getText().toString(),1)+parseInt(children.getText().toString(),0); String type="normal".equals(passengerType)?"":"  •  "+("men".equals(passengerType)?"ویژه برادران":"ویژه خواهران"); passengerSummary.setText("⚙   مسافران     "+total+" مسافر"+type); passengerSummary.setTextColor(TEXT); passengerSummary.setTypeface(null,1); }

    private LinearLayout makePassengerStepper(String text,boolean adult){
        LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR); row.setBackground(bg(FIELD,14,BORDER,1)); row.setPadding(dp(10),dp(5),dp(10),dp(5));
        TextView l=label(text); l.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL); l.setTextSize(16); l.setTypeface(null,1); row.addView(l,new LinearLayout.LayoutParams(0,dp(48),1));
        Button minus=button("−",PANEL_2); minus.setTextColor(TEXT); minus.setTextSize(22); minus.setTypeface(null,1); Button plus=button("+",BLUE); EditText val=field(""); val.setGravity(Gravity.CENTER); val.setInputType(InputType.TYPE_CLASS_NUMBER); val.setText(adult?"1":"0"); val.setFocusable(false); if(adult) adults=val; else children=val;
        minus.setOnClickListener(v->{ int n=parseInt(val.getText().toString(),adult?1:0); int min=adult?1:0; val.setText(String.valueOf(Math.max(min,n-1))); }); plus.setOnClickListener(v->{ int n=parseInt(val.getText().toString(),adult?1:0); val.setText(String.valueOf(Math.min(9,n+1))); });
        row.addView(minus,new LinearLayout.LayoutParams(dp(50),dp(44))); gapH(row,5); row.addView(val,new LinearLayout.LayoutParams(dp(56),dp(44))); gapH(row,5); row.addView(plus,new LinearLayout.LayoutParams(dp(50),dp(44))); return row;
    }


    private void pressAction(Button b,Runnable action){ b.setAlpha(0.55f); b.animate().scaleX(0.96f).scaleY(0.96f).setDuration(70).withEndAction(()->{ b.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(90).withEndAction(action).start(); }).start(); }
    private void updateActionButtons(){ if(startBtn==null||stopBtn==null)return; int paleGreen=Color.rgb(157,225,195), paleRed=Color.rgb(255,170,178); startBtn.setText(running?"✓ در حال جستجو":"▶ شروع ربات"); stopBtn.setText(actionState==2?"■ متوقف":"■ توقف"); startBtn.setBackground(bg(actionState==1?Color.rgb(0,165,95):paleGreen,14,actionState==1?Color.rgb(0,165,95):paleGreen,0)); stopBtn.setBackground(bg(actionState==2?Color.rgb(225,45,62):paleRed,14,actionState==2?Color.rgb(225,45,62):paleRed,0)); startBtn.setTextColor(actionState==1?Color.WHITE:TEXT); stopBtn.setTextColor(actionState==2?Color.WHITE:TEXT); startBtn.setEnabled(true); stopBtn.setEnabled(true); }

    private void showRefreshMenu(View anchor){ PopupMenu m=new PopupMenu(this,anchor); for(int i=1;i<=10;i++) m.getMenu().add(String.valueOf(i)); m.setOnMenuItemClickListener(item->{ refresh.setText(item.getTitle().toString()); return true; }); m.show(); }
    private void showPersianCalendar(){
        int[] today=getTodayJalali();
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(14),dp(8),dp(14),dp(8));
        LinearLayout top=new LinearLayout(this); top.setOrientation(LinearLayout.HORIZONTAL); top.setGravity(Gravity.CENTER);
        Spinner year=new Spinner(this), month=new Spinner(this); List<String> ys=new ArrayList<>(), ms=new ArrayList<>();
        for(int y=today[0];y<=today[0]+8;y++)ys.add(String.valueOf(y));
        String[] mn={"فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند"}; for(String x:mn)ms.add(x);
        year.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,ys));
        month.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,ms));
        top.addView(year,new LinearLayout.LayoutParams(0,dp(52),1)); top.addView(month,new LinearLayout.LayoutParams(0,dp(52),1)); box.addView(top);
        GridLayout days=new GridLayout(this); days.setColumnCount(7); days.setAlignmentMode(GridLayout.ALIGN_BOUNDS); days.setUseDefaultMargins(false); days.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        box.addView(days,new LinearLayout.LayoutParams(-1,dp(270)));
        final int[] selected={today[2]};
        year.setSelection(0); month.setSelection(today[1]-1);
        String old=travelDate.getText().toString(); try{ String[] p=old.split("/"); int oy=Integer.parseInt(p[0]); if(oy>=today[0]&&oy<=today[0]+8)year.setSelection(oy-today[0]); month.setSelection(Math.max(0,Integer.parseInt(p[1])-1)); selected[0]=Integer.parseInt(p[2]); }catch(Exception ignored){}
        AdapterView.OnItemSelectedListener refreshCalendar=new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> parent,View view,int position,long id){ populatePersianMonth(days,Integer.parseInt(year.getSelectedItem().toString()),month.getSelectedItemPosition()+1,selected); }
            @Override public void onNothingSelected(AdapterView<?> parent){}
        };
        year.setOnItemSelectedListener(refreshCalendar); month.setOnItemSelectedListener(refreshCalendar);
        populatePersianMonth(days,Integer.parseInt(year.getSelectedItem().toString()),month.getSelectedItemPosition()+1,selected);
        new AlertDialog.Builder(this).setTitle("انتخاب تاریخ شمسی — هماهنگ با گوشی").setView(box).setNegativeButton("لغو",null).setPositiveButton("تأیید",(d,w)->{
            String chosen=year.getSelectedItem()+"/"+String.format(Locale.US,"%02d",month.getSelectedItemPosition()+1)+"/"+String.format(Locale.US,"%02d",selected[0]);
            String min=String.format(Locale.US,"%04d/%02d/%02d",today[0],today[1],today[2]);
            if(chosen.compareTo(min)<0){ toast("تاریخ گذشته قابل انتخاب نیست"); travelDate.setText(""); }else travelDate.setText(chosen);
        }).show();
    }

    private void populatePersianMonth(GridLayout days,int jy,int jm,int[] selected){
        days.removeAllViews();
        String[] week={"شنبه","یکشنبه","دوشنبه","سه‌شنبه","چهارشنبه","پنجشنبه","جمعه"};
        for(String name:week){ TextView h=label(name); h.setGravity(Gravity.CENTER); h.setTextSize(10); h.setTypeface(null,1); GridLayout.LayoutParams hp=new GridLayout.LayoutParams(); hp.width=0; hp.height=dp(32); hp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); days.addView(h,hp); }
        int[] g=jalaliToGregorian(jy,jm,1); java.util.Calendar first=java.util.Calendar.getInstance(); first.clear(); first.set(g[0],g[1]-1,g[2],12,0,0);
        int offset=first.get(java.util.Calendar.DAY_OF_WEEK)%7;
        for(int i=0;i<offset;i++){ Space empty=new Space(this); GridLayout.LayoutParams ep=new GridLayout.LayoutParams(); ep.width=0; ep.height=dp(42); ep.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); days.addView(empty,ep); }
        int count=jalaliMonthLength(jy,jm); if(selected[0]>count)selected[0]=count;
        for(int i=1;i<=count;i++){
            Button d=button(String.valueOf(i),PANEL_2); d.setTextColor(TEXT); d.setTextSize(14); d.setMinWidth(0); d.setMinimumWidth(0); d.setMinHeight(0); d.setMinimumHeight(0); d.setPadding(0,0,0,0);
            final int day=i; if(day==selected[0]){ d.setBackground(bg(BLUE,8,BLUE,0)); d.setTextColor(Color.WHITE); }
            d.setOnClickListener(v->{ selected[0]=day; populatePersianMonth(days,jy,jm,selected); });
            GridLayout.LayoutParams lp=new GridLayout.LayoutParams(); lp.width=0; lp.height=dp(42); lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); lp.setMargins(dp(1),dp(1),dp(1),dp(1)); days.addView(d,lp);
        }
    }

    private int jalaliMonthLength(int jy,int jm){ if(jm<=6)return 31; if(jm<=11)return 30; int r=jy%33; return (r==1||r==5||r==9||r==13||r==17||r==22||r==26||r==30)?30:29; }

    private int[] jalaliToGregorian(int jy,int jm,int jd){
        jy+=1595; int days=-355668+(365*jy)+(jy/33)*8+((jy%33)+3)/4+jd+(jm<7?(jm-1)*31:((jm-7)*30)+186);
        int gy=400*(days/146097); days%=146097;
        if(days>36524){ gy+=100*(--days/36524); days%=36524; if(days>=365)days++; }
        gy+=4*(days/1461); days%=1461;
        if(days>365){ gy+=(days-1)/365; days=(days-1)%365; }
        int gd=days+1; int leap=(gy%4==0&&gy%100!=0)||gy%400==0?1:0; int[] md={0,31,28+leap,31,30,31,30,31,31,30,31,30,31}; int gm=1;
        while(gm<=12&&gd>md[gm]){ gd-=md[gm]; gm++; }
        return new int[]{gy,gm,gd};
    }

    private int[] getTodayJalali(){
        java.util.Calendar c=java.util.Calendar.getInstance(); int gy=c.get(java.util.Calendar.YEAR), gm=c.get(java.util.Calendar.MONTH)+1, gd=c.get(java.util.Calendar.DAY_OF_MONTH);
        int[] gdm={0,31,59,90,120,151,181,212,243,273,304,334}; int gy2=gm>2?gy+1:gy; int days=355666+365*gy+(gy2+3)/4-(gy2+99)/100+(gy2+399)/400+gd+gdm[gm-1]; int jy=-1595+33*(days/12053); days%=12053; jy+=4*(days/1461); days%=1461; if(days>365){jy+=(days-1)/365;days=(days-1)%365;} int jm=days<186?1+days/31:7+(days-186)/30; int jd=1+(days<186?days%31:(days-186)%30); return new int[]{jy,jm,jd};
    }

    private LinearLayout makeBrowserPanel(){ LinearLayout p=new LinearLayout(this); p.setOrientation(LinearLayout.VERTICAL); p.setPadding(dp(8),0,dp(8),dp(8)); TextView h=label("صفحه رجا — هنگام پیدا شدن بلیت خودکار باز می‌شود"); h.setGravity(Gravity.CENTER); h.setTextColor(CYAN); p.addView(h,new LinearLayout.LayoutParams(-1,dp(38))); webView=new WebView(this); webView.setBackgroundColor(Color.WHITE); p.addView(webView,new LinearLayout.LayoutParams(-1,0,1)); return p; }

    private void setupWebView(){
        WebSettings s=webView.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setLoadsImagesAutomatically(true); s.setJavaScriptCanOpenWindowsAutomatically(true); s.setUserAgentString(s.getUserAgentString()+" MASOUD-Android/1.0");
        webView.addJavascriptInterface(new JsBridge(),"MasoudBridge"); webView.setWebChromeClient(new WebChromeClient()); webView.setWebViewClient(new WebViewClient(){ @Override public void onPageFinished(WebView v,String url){ long now=System.currentTimeMillis(); if(url.equals(lastFinishedUrl)&&now-lastFinishedAt<1200)return; boolean changed=!url.equals(lastFinishedUrl); lastFinishedUrl=url; lastFinishedAt=now; if(changed && loginInProgress){ loginInProgress=false; log("صفحه ورود عوض شد؛ فرم جدید بررسی می‌شود"); } log("صفحه باز شد: "+url); if(running&&reauthRequired)logoutInProgress=false; if(running) handler.postDelayed(()->advanceAutomation(),100); } });
        webView.loadUrl("https://www.raja.ir/");
    }


    private void startBot(){
        if(origin.getText().toString().trim().isEmpty()||destination.getText().toString().trim().isEmpty()||travelDate.getText().toString().trim().isEmpty()){ toast("مبدا، مقصد و تاریخ را کامل کن."); return; }
        if(phone.getText().toString().trim().isEmpty()||password.getText().toString().trim().isEmpty()){ toast("حساب رجا را وارد کن."); return; }
        if(!priceMode.isChecked() && trainNumber.getText().toString().trim().isEmpty()){ toast("شماره قطار را وارد کن یا حالت بازه قیمت را فعال کن."); return; }
        saveAccount(); sessionGeneration++; running=true; actionState=1; reauthRequired=false; logoutInProgress=false; loginInProgress=false; loginRetryCount=0; loggedIn=false; searchSubmitted=false; reserved=false; alarmPlayed=false; updateActionButtons(); status.setText("● فرمان شروع ثبت شد؛ در حال اجرا..."); status.setTextColor(Color.rgb(0,130,75)); logView.setText(""); log("شروع اجرا | "+origin.getText()+" ← "+destination.getText()+" | "+travelDate.getText()); log("تنظیمات | بزرگسال="+adults.getText()+" | کودک="+children.getText()+" | نوع="+passengerTypeLabel()+" | رفرش="+String.format(Locale.US,"%.1f",getRefreshSeconds())+" ثانیه"); showPanel(browserPanel);
        if(!webView.getUrl().startsWith("https://www.raja.ir")) webView.loadUrl("https://www.raja.ir/"); else webView.loadUrl("https://www.raja.ir/"); startMonitorLoop();
    }
    private void stopBot(String why){ running=false; sessionGeneration++; if(sessionDeadline!=null){handler.removeCallbacks(sessionDeadline);sessionDeadline=null;} webView.evaluateJavascript("if(window.__masoudSessionCancel)window.__masoudSessionCancel();",null); actionState=2; if(monitorRunnable!=null) handler.removeCallbacks(monitorRunnable); updateActionButtons(); status.setText("■ "+why); status.setTextColor(RED); log(why); }

    private void startMonitorLoop(){ monitorRunnable=new Runnable(){ @Override public void run(){ if(!running) return; if(searchSubmitted&&!reserved){ long now=SystemClock.elapsedRealtime(); if(now>=nextRefreshAt){ double seconds=getRefreshSeconds(); log("رفرش دقیق اجرا شد | "+String.format(Locale.US,"%.1f",seconds)+" ثانیه"); webView.reload(); nextRefreshAt=now+Math.round(seconds*1000.0); } } handler.postDelayed(this,50); } }; handler.post(monitorRunnable); }
    private double getRefreshSeconds(){ return refreshSlider==null?2.0:0.5+(refreshSlider.getProgress()*0.5); }
    private String passengerTypeLabel(){ return "men".equals(passengerType)?"ویژه برادران":"women".equals(passengerType)?"ویژه خواهران":"مسافران عادی"; }

    private void advanceAutomation(){
        if(!running) return; String url=webView.getUrl()==null?"":webView.getUrl();
        if(url.contains("registerticket")){ reserved=true; status.setText("● وارد صفحه مشخصات مسافر شد."); log("رزرو و ادامه خرید انجام شد؛ صفحه مشخصات مسافر باز شد."); showPanel(browserPanel); if(alarm.isChecked()) playAlarmOnce(); return; }
        if(!loggedIn){ if(!loginInProgress)injectLogin(); return; }
        if(!searchSubmitted){ injectSearchFormV11(); return; }
        inspectResultsAndReserve();
    }

    private void injectLogout(){
        if(logoutInProgress)return; logoutInProgress=true;
        final int token=sessionGeneration;
        if(sessionDeadline==null){ log("بررسی ورود یا خروج حساب رجا..."); sessionDeadline=()->{sessionDeadline=null;if(running&&reauthRequired&&token==sessionGeneration)stopBot("ورود یا خروج رجا طی ۱۲ ثانیه مشخص نشد");}; handler.postDelayed(sessionDeadline,12000); }
        String js="(function(){\n if(window.__masoudSessionCancel)window.__masoudSessionCancel();\n const token=TOKEN, text=e=>String(e.innerText||e.textContent||'').replace(/\\s+/g,' ').trim();\n const vis=e=>e&&e.getClientRects().length>0&&getComputedStyle(e).visibility!=='hidden';\n const nodes=()=>[...document.querySelectorAll('a,button,[role=button],span,div')].filter(vis);\n const actionable=e=>e.closest('a,button,[role=button],[data-toggle],[data-bs-toggle]')||e;\n const exact=re=>nodes().filter(e=>re.test(text(e))).sort((a,b)=>a.querySelectorAll('*').length-b.querySelectorAll('*').length)[0];\n let stopped=false,clickedExit=false,observer,timer; const opened=new Set();\n const cancel=()=>{stopped=true;if(observer)observer.disconnect();if(timer)clearInterval(timer);};\n window.__masoudSessionCancel=cancel;\n const ready=()=>{cancel();MasoudBridge.sessionReady(token);};\n const scan=()=>{if(stopped)return;try{\n  let out=exact(/^(خروج|خروج از حساب)$/);\n  let login=exact(/^ورود\\s*\\/\\s*عضویت$/);\n  if(clickedExit){if(login&&!out)ready();return;}\n  if(out){clickedExit=true;MasoudBridge.log('خروج از حساب فعال رجا...');actionable(out).click();return;}\n  if(login){MasoudBridge.log('حساب وارد نشده؛ ورود مستقیم');ready();return;}\n  let account=exact(/^(حساب کاربری|پروفایل|پروفایل شخصی)$/);\n  if(!account){account=nodes().find(e=>/profile|account|user|حساب|پروفایل/i.test((e.getAttribute('aria-label')||'')+' '+(e.getAttribute('title')||'')+' '+(e.getAttribute('id')||'')+' '+(e.getAttribute('class')||''))&&e.matches('a,button,[role=button],[data-toggle],[data-bs-toggle]'));}\n  if(!account){const icon=nodes().find(e=>/^(?:.*\\s)?(?:icon-user|fa-user|bi-person|icon-profile)(?:\\s.*)?$/.test(String(e.className||'')));if(icon)account=actionable(icon);}\n  if(account){let target=actionable(account);if(!opened.has(target)){opened.add(target);target.click();}}\n }catch(e){cancel();MasoudBridge.sessionFailed(token,'خطای بررسی حساب رجا');}};\n observer=new MutationObserver(scan);observer.observe(document.documentElement,{childList:true,subtree:true,attributes:true});timer=setInterval(scan,200);scan();\n})();".replace("TOKEN",String.valueOf(token));
        webView.evaluateJavascript(js,null);
    }

    private void injectLogin(){
        if(loginInProgress)return;
        loginInProgress=true;
        final int token=sessionGeneration;
        if(sessionDeadline==null){log("منتظر آماده‌شدن ورود / عضویت...");sessionDeadline=()->{sessionDeadline=null;if(running&&!loggedIn&&token==sessionGeneration)stopBot("ورود / عضویت یا فرم ورود طی ۱۲ ثانیه آماده نشد");};handler.postDelayed(sessionDeadline,12000);}
        String js="(function(){try{const txt=s=>(s||'').trim();const els=[...document.querySelectorAll('button,a,span,div')];let op=els.find(e=>txt(e.innerText)==='ورود / عضویت')||els.find(e=>txt(e.innerText).includes('ورود / عضویت'));if(op){op.click();setTimeout(()=>{let ins=[...document.querySelectorAll('input')].filter(x=>x.offsetParent!==null);if(ins.length>=2){ins[0].focus();ins[0].value="+q(phone.getText().toString())+";ins[0].dispatchEvent(new Event('input',{bubbles:true}));ins[1].focus();ins[1].value="+q(password.getText().toString())+";ins[1].dispatchEvent(new Event('input',{bubbles:true}));let bs=[...document.querySelectorAll('button')].filter(x=>x.offsetParent!==null);let b=bs.find(x=>txt(x.innerText)==='ورود')||bs.find(x=>txt(x.innerText).includes('ورود'));if(b){b.click();MasoudBridge.loginClicked();}else MasoudBridge.log('دکمه ورود پیدا نشد');}else MasoudBridge.log('فیلدهای ورود پیدا نشد');},700);return 'opening';}MasoudBridge.log('ورود/عضویت پیدا نشد');return 'missing';}catch(e){MasoudBridge.log('خطای ورود: '+e);return 'error';}})();";
        webView.evaluateJavascript("(function(){\n if(window.__masoudSessionCancel)window.__masoudSessionCancel();\n let stopped=false,observer,timer;\n const cancel=()=>{stopped=true;if(observer)observer.disconnect();if(timer)clearInterval(timer);};\n window.__masoudSessionCancel=cancel;\n const scan=()=>{if(stopped)return;\n const op=[...document.querySelectorAll('button,a,span,div')].find(e=>e.getClientRects().length>0&&getComputedStyle(e).visibility!=='hidden'&&(e.innerText||'').trim()==='ورود / عضویت');\n if(!op)return;\n cancel();MasoudBridge.log('ورود / عضویت آماده شد؛ اجرای ورود W6');\n"+js+"\n};observer=new MutationObserver(scan);observer.observe(document.documentElement,{childList:true,subtree:true,attributes:true});timer=setInterval(scan,200);scan();})();",null);
    }

    private void injectSearchForm(){
        int total=parseInt(adults.getText().toString(),1)+parseInt(children.getText().toString(),0);
        String js="(async function(){const sleep=ms=>new Promise(r=>setTimeout(r,ms));const fire=(el)=>{el.dispatchEvent(new Event('input',{bubbles:true}));el.dispatchEvent(new Event('change',{bubbles:true}));};try{let change=[...document.querySelectorAll('button,div,a')].find(x=>(x.innerText||'').trim()==='تغییر جستجو');if(change){change.click();await sleep(100);}async function station(kind,city){let comps=[...document.querySelectorAll('app-stations')];let c=comps.find(x=>((x.getAttribute('name')||'').toLowerCase()).includes(kind==='from'?'from':'to'));if(!c)return false;let clear=c.querySelector('.ng-clear-wrapper');if(clear)clear.click();let inp=c.querySelector('input[role=combobox]');if(!inp)return false;inp.focus();inp.value=city;fire(inp);await sleep(50);let opts=[...document.querySelectorAll('ng-dropdown-panel .ng-option')];let o=opts.find(x=>(x.innerText||'').trim()===city)||opts.find(x=>(x.innerText||'').includes(city));if(o){o.click();await sleep(80);return true;}return false;}let a=await station('from',"+q(origin.getText().toString().trim())+");let b=await station('to',"+q(destination.getText().toString().trim())+");if(!a||!b){MasoudBridge.log('مبدا یا مقصد تنظیم نشد');return;}let dateText="+q(travelDate.getText().toString().trim())+";let parts=dateText.replace(/-/g,'/').split('/').map(Number);let picker=document.querySelector('app-datepicker-single[name=oneWayDatePicker]');if(!picker||parts.length!==3){MasoudBridge.log('تقویم رجا پیدا نشد');return;}let cal=picker.querySelector('button.icon-calendar')||picker.querySelector('input[name=dp]');if(cal)cal.click();await sleep(50);let jy=parts[0],jm=parts[1],jd=parts[2];let monthNames=['','فروردین','اردیبهشت','خرداد','تیر','مرداد','شهریور','مهر','آبان','آذر','دی','بهمن','اسفند'];for(let sel of [...document.querySelectorAll('select')].filter(x=>x.offsetParent!==null)){let opts=[...sel.options];let yo=opts.find(o=>(o.textContent||'').includes(String(jy)));if(yo){sel.value=yo.value;fire(sel);await sleep(80);continue;}let mo=opts.find(o=>(o.textContent||'').includes(monthNames[jm])||(o.textContent||'').trim()===String(jm));if(mo){sel.value=mo.value;fire(sel);await sleep(80);}}let dayFa=String(jd).replace(/[0-9]/g,d=>'۰۱۲۳۴۵۶۷۸۹'[Number(d)]);let nodes=[...document.querySelectorAll('button,td,span,div')].filter(x=>x.offsetParent!==null);let day=nodes.find(x=>{let t=(x.innerText||'').trim();let c=(x.className||'').toString().toLowerCase();return (t===String(jd)||t===dayFa)&&!c.includes('disabled')&&!c.includes('muted')&&!c.includes('outside');});if(!day){MasoudBridge.log('روز تاریخ در تقویم پیدا نشد');return;}day.click();await sleep(50);let dateInp=picker.querySelector('input[name=dp]');if(!dateInp||!(dateInp.value||'').trim()){MasoudBridge.log('تاریخ در رجا ثبت نشد');return;}let pb=document.querySelector('#dropdownPassenger')||[...document.querySelectorAll('button,div,[role=button]')].filter(x=>x.offsetParent!==null).filter(x=>/مسافر/.test((x.innerText||'').trim())).sort((a,b)=>(a.innerText||'').length-(b.innerText||'').length)[0];if(pb){pb.click();await sleep(100);async function setPassenger(label,want){let candidates=[...document.querySelectorAll('div,li')].filter(x=>x.offsetParent!==null&&(x.innerText||'').includes(label));let row=candidates.sort((a,b)=>(a.innerText||'').length-(b.innerText||'').length).find(x=>x.querySelectorAll('button').length>=2)||candidates[0];if(!row)return false;for(let p=row;p&&p!==document.body&&p.querySelectorAll('button').length<2;p=p.parentElement)row=p;let buttons=[...row.querySelectorAll('button')].filter(x=>x.offsetParent!==null);let plus=buttons.find(x=>/\\+|افزایش/.test((x.innerText||'')+(x.getAttribute('aria-label')||'')))||buttons[buttons.length-1];let minus=buttons.find(x=>/−|-|کاهش/.test((x.innerText||'')+(x.getAttribute('aria-label')||'')))||buttons[0];let nums=((row.innerText||'').match(/[0-9۰-۹]+/g)||[]).map(n=>Number(n.replace(/[۰-۹]/g,d=>'۰۱۲۳۴۵۶۷۸۹'.indexOf(d))));let cur=nums.find(n=>n>=0&&n<=6);if(cur===undefined)cur=label==='بزرگسال'?1:0;while(cur<want&&plus){plus.click();cur++;await sleep(50);}while(cur>want&&minus){minus.click();cur--;await sleep(50);}return true;}let adultOk=await setPassenger('بزرگسال',"+parseInt(adults.getText().toString(),1)+");let childOk=await setPassenger('خردسال',"+parseInt(children.getText().toString(),0)+");if(!childOk)childOk=await setPassenger('کودک',"+parseInt(children.getText().toString(),0)+");if(adultOk&&childOk)MasoudBridge.log('تعداد بزرگسال و کودک در رجا تنظیم شد');else MasoudBridge.log('کنترل تعداد مسافران در رجا کامل پیدا نشد');}await sleep(100);let pt="+q(passengerType)+";let typeLabel=pt==='men'?'ویژه برادران':pt==='women'?'ویژه خواهران':'مسافران عادی';let allType=[...document.querySelectorAll('ng-select,.ng-select-container,[role=combobox],button,div,span')].filter(x=>x.offsetParent!==null);let leaf=allType.filter(x=>{let t=(x.innerText||x.value||'').trim();return t==='مسافران عادی'||t==='ویژه برادران'||t==='ویژه خواهران';}).sort((a,b)=>(a.innerText||'').length-(b.innerText||'').length)[0];let typeControl=leaf&&(leaf.closest('ng-select,[role=combobox],button,.ng-select-container')||leaf);if(!typeControl){MasoudBridge.selectionError('کادر نوع مسافر در رجا پیدا نشد');return;}typeControl.click();await sleep(100);let choices=[...document.querySelectorAll('.ng-option,[role=option],li,button,div,span')].filter(x=>x.offsetParent!==null&&(x.innerText||'').trim()===typeLabel).sort((a,b)=>(a.innerText||'').length-(b.innerText||'').length);let choice=choices[0];if(choice){let target=choice.closest('.ng-option,[role=option],li,button')||choice;target.click();await sleep(100);}let current=[...document.querySelectorAll('ng-select,.ng-select-container,[role=combobox],button,div,span')].filter(x=>x.offsetParent!==null).some(x=>(x.innerText||'').trim()===typeLabel);if(!choice||!current){MasoudBridge.selectionError('انتخاب نوع مسافر تأیید نشد: '+typeLabel);return;}MasoudBridge.log('نوع مسافر در رجا تأیید شد: '+typeLabel);"+(coupe.isChecked()?"let cc=[...document.querySelectorAll('input[type=checkbox]')].find(x=>((x.parentElement?.innerText)||'').includes('کوپه دربست'));if(cc&&!cc.checked)cc.click();":"")+"let bs=[...document.querySelectorAll('button')].filter(x=>x.offsetParent!==null);let s=bs.find(x=>(x.innerText||'').trim().includes('جستجو'));if(s){s.click();MasoudBridge.searchClicked();}else MasoudBridge.log('دکمه جستجو پیدا نشد');}catch(e){MasoudBridge.log('خطای تنظیم جستجو: '+e);}})();";
        webView.evaluateJavascript(js,null);
    }

    private void injectSearchFormV11(){
        final int adultCount=parseInt(adults.getText().toString(),1);
        final int childCount=parseInt(children.getText().toString(),0);
        String js="(async function(){"
            +"const sleep=ms=>new Promise(r=>setTimeout(r,ms));"
            +"const vis=e=>e&&e.offsetParent!==null;"
            +"const text=e=>String((e&&(e.innerText||e.textContent||e.value))||'').replace(/\\s+/g,' ').trim();"
            +"const fire=e=>{e.dispatchEvent(new Event('input',{bubbles:true}));e.dispatchEvent(new Event('change',{bubbles:true}));};"
            +"const wait=async fn=>{for(let i=0;i<80;i++){let v=fn();if(v)return v;await sleep(50);}return null;};"
            +"const fail=m=>{MasoudBridge.selectionError(m);return false;};"
            +"try{"
            +"let change=[...document.querySelectorAll('button,a,[role=button]')].filter(vis).find(e=>text(e)==='تغییر جستجو');if(change){change.click();await sleep(100);}"
            +"async function station(kind,city){let c=await wait(()=>[...document.querySelectorAll('app-stations')].find(x=>String(x.getAttribute('name')||'').toLowerCase().includes(kind)));if(!c)return false;let clear=c.querySelector('.ng-clear-wrapper');if(clear)clear.click();let inp=c.querySelector('input[role=combobox],input');if(!inp)return false;inp.focus();let d=Object.getOwnPropertyDescriptor(HTMLInputElement.prototype,'value');if(d&&d.set)d.set.call(inp,city);else inp.value=city;fire(inp);let option=await wait(()=>[...document.querySelectorAll('ng-dropdown-panel .ng-option,[role=option]')].filter(vis).find(x=>text(x)===city||text(x).includes(city)));if(!option)return false;option.click();await sleep(100);return text(c).includes(city)||String(inp.value).includes(city);}"
            +"let fromOk=await station('from',"+q(origin.getText().toString().trim())+");if(!fromOk){fail('مبدا در رجا تنظیم نشد');return;}MasoudBridge.log('مبدا در رجا تأیید شد');"
            +"let toOk=await station('to',"+q(destination.getText().toString().trim())+");if(!toOk){fail('مقصد در رجا تنظیم نشد');return;}MasoudBridge.log('مقصد در رجا تأیید شد');"
            +"let parts="+q(travelDate.getText().toString().trim())+".replace(/-/g,'/').split('/').map(Number);let picker=await wait(()=>document.querySelector('app-datepicker-single[name=oneWayDatePicker],app-datepicker-single'));if(!picker||parts.length!==3){fail('تقویم رجا پیدا نشد');return;}let cal=picker.querySelector('button.icon-calendar,input[name=dp],input');if(cal)cal.click();let jy=parts[0],jm=parts[1],jd=parts[2],months=['','فروردین','اردیبهشت','خرداد','تیر','مرداد','شهریور','مهر','آبان','آذر','دی','بهمن','اسفند'];await sleep(100);for(let sel of [...document.querySelectorAll('select')].filter(vis)){let opts=[...sel.options];let yo=opts.find(o=>text(o).includes(String(jy)));if(yo){sel.value=yo.value;fire(sel);await sleep(80);continue;}let mo=opts.find(o=>text(o).includes(months[jm])||text(o)===String(jm));if(mo){sel.value=mo.value;fire(sel);await sleep(80);}}let fa=String(jd).replace(/[0-9]/g,d=>'۰۱۲۳۴۵۶۷۸۹'[Number(d)]);let day=await wait(()=>[...document.querySelectorAll('button,td,[role=gridcell],span,div')].filter(vis).find(x=>{let t=text(x),c=String(x.className||'').toLowerCase();return(t===String(jd)||t===fa)&&!c.includes('disabled')&&!c.includes('muted')&&!c.includes('outside');}));if(!day){fail('روز تاریخ در تقویم پیدا نشد');return;}day.click();let dateInput=await wait(()=>picker.querySelector('input[name=dp],input'));if(!dateInput){fail('فیلد تاریخ رجا پیدا نشد');return;}await sleep(100);MasoudBridge.log('تاریخ در رجا تأیید شد: '+String(dateInput.value||"+q(travelDate.getText().toString().trim())+"));"
            +"let pb=await wait(()=>document.querySelector('#dropdownPassenger')||[...document.querySelectorAll('button,[role=button],div')].filter(vis).filter(x=>/مسافر/.test(text(x))).sort((a,b)=>text(a).length-text(b).length)[0]);if(!pb){fail('کادر تعداد مسافر در رجا پیدا نشد');return;}pb.click();await sleep(500);"
            +"async function setCount(label,want,def){let row=await wait(()=>{let cs=[...document.querySelectorAll('div,li')].filter(vis).filter(x=>text(x).includes(label));return cs.sort((a,b)=>text(a).length-text(b).length).find(x=>x.querySelectorAll('button').length>=2)||null;});if(!row)return false;let buttons=[...row.querySelectorAll('button')].filter(vis),plus=buttons.find(x=>/\\+|افزایش/.test(text(x)+(x.getAttribute('aria-label')||'')))||buttons[buttons.length-1],minus=buttons.find(x=>/−|-|کاهش/.test(text(x)+(x.getAttribute('aria-label')||'')))||buttons[0];let nums=(text(row).match(/[0-9۰-۹]+/g)||[]).map(n=>Number(n.replace(/[۰-۹]/g,d=>'۰۱۲۳۴۵۶۷۸۹'.indexOf(d))));let cur=nums.find(n=>n>=0&&n<=6);if(cur===undefined)cur=def;while(cur<want&&plus){plus.click();cur++;await sleep(300);}while(cur>want&&minus){minus.click();cur--;await sleep(300);}return cur===want;}"
            +"let adultOk=await setCount('بزرگسال',"+adultCount+",1),childOk=await setCount('خردسال',"+childCount+",0);if(!childOk)childOk=await setCount('کودک',"+childCount+",0);if(!adultOk||!childOk){fail('تنظیم تعداد بزرگسال یا کودک تأیید نشد');return;}let confirm=[...document.querySelectorAll('button,[role=button]')].filter(vis).find(x=>text(x)==='تأیید'||text(x)==='تایید');if(confirm){confirm.click();await sleep(800);}else{pb.click();await sleep(500);}MasoudBridge.log('تعداد بزرگسال و کودک در رجا تأیید شد');"
            +"let wanted="+q(passengerTypeLabel())+";let typeControl=await wait(()=>{let exact=[...document.querySelectorAll('ng-select,.ng-select-container,[role=combobox],button,div,span')].filter(vis).filter(x=>['مسافران عادی','ویژه برادران','ویژه خواهران'].includes(text(x))).sort((a,b)=>text(a).length-text(b).length)[0];return exact&&(exact.closest('ng-select,[role=combobox],button,.ng-select-container')||exact);});if(!typeControl){fail('کادر نوع مسافر در رجا پیدا نشد');return;}typeControl.scrollIntoView({block:'center',behavior:'instant'});await sleep(300);typeControl.click();let choice=await wait(()=>[...document.querySelectorAll('ng-dropdown-panel .ng-option,[role=option]')].filter(vis).find(x=>text(x)===wanted));if(!choice){fail('گزینه نوع مسافر پیدا نشد: '+wanted);return;}choice.scrollIntoView({block:'nearest',behavior:'instant'});choice.click();await sleep(350);if(!text(typeControl).includes(wanted)){fail('انتخاب نوع مسافر تأیید نشد: '+wanted);return;}MasoudBridge.log('نوع مسافر در رجا تأیید شد: '+wanted);"
            +(""+(coupe.isChecked()?"let cc=[...document.querySelectorAll('input[type=checkbox]')].find(x=>text(x.parentElement).includes('کوپه دربست'));if(cc&&!cc.checked){cc.click();await sleep(80);}MasoudBridge.log('کوپه دربست در رجا اعمال شد');":""))
            +"let search=await wait(()=>[...document.querySelectorAll('button,[role=button],input[type=submit]')].filter(vis).find(x=>/جستجو/.test(text(x))));if(!search){fail('دکمه جستجوی رجا پیدا نشد');return;}search.click();MasoudBridge.searchClicked();"
            +"}catch(e){MasoudBridge.selectionError('خطای تنظیم جستجو: '+e);}})();";
        webView.evaluateJavascript(js,null);
    }

    private void inspectResultsAndReserve(){
        String min=digitsOnly(minPrice.getText().toString()), max=digitsOnly(maxPrice.getText().toString()), wanted=digitsOnly(trainNumber.getText().toString());
        String match;
        if(priceMode.isChecked()){ String lo=min.isEmpty()?"true":"price>=Number('"+min+"')"; String hi=max.isEmpty()?"true":"price<=Number('"+max+"')"; match="("+lo+"&&"+hi+")"; }
        else match="tn==='"+wanted+"'";
        String js="(function(){try{const ds=s=>String(s||'').replace(/[^0-9۰-۹٠-٩]/g,'').replace(/[۰-۹]/g,d=>'۰۱۲۳۴۵۶۷۸۹'.indexOf(d)).replace(/[٠-٩]/g,d=>'٠١٢٣٤٥٦٧٨٩'.indexOf(d));const vis=e=>e&&e.offsetParent!==null;const scan=()=>{if(window.__masoudReserved)return;let allButtons=[...document.querySelectorAll('button,a,[role=button]')].filter(vis);let reserve=allButtons.filter(b=>/رزرو|انتخاب بلیت|خرید بلیت/.test((b.innerText||'').trim())&&!/ادامه/.test((b.innerText||'').trim()));let sold=[...document.querySelectorAll('body *')].filter(x=>vis(x)&&x.children.length===0&&/تمام شد|تکمیل ظرفیت|ناموجود/.test((x.innerText||'').trim())).length;let available=reserve.length;let total=available+sold;let sig=total+'|'+available+'|'+sold;if(window.__masoudLastSig!==sig){window.__masoudLastSig=sig;MasoudBridge.log('نتایج رجا | کل='+total+' | موجود='+available+' | تکمیل‌شده='+sold+' | دکمه رزرو='+reserve.length);}for(let b of reserve){let card=b.closest('.train-result,[class*=train-result],app-train-item,[class*=ticket]')||b.parentElement?.parentElement?.parentElement||b.parentElement;let text=card?(card.innerText||''):(b.parentElement?.innerText||'');let priceEl=card&&card.querySelector?card.querySelector('span.price,[class*=price]'):null;let price=priceEl?Number(ds(priceEl.textContent)):Number(ds((text.match(/[0-9۰-۹٠-٩,،]+\\s*ریال/)||['0'])[0]));let timeline=card&&card.querySelector?card.querySelector('app-timeline'):null;let tn=timeline?ds(timeline.getAttribute('data-trainnumber-to')||timeline.getAttribute('data-trainnumber-from')||''):'';if(!tn){let m=text.match(/شماره\\s*قطار\\s*[:：]?\\s*([0-9۰-۹٠-٩]+)/);if(m)tn=ds(m[1]);}if("+match+"){window.__masoudReserved=true;let seen=performance.now();b.click();let clicked=performance.now();MasoudBridge.reserveFast(String(Math.max(0,Math.round(clicked-seen))));const next=()=>{let c=[...document.querySelectorAll('button,a,[role=button]')].filter(vis).find(x=>(x.innerText||'').trim()==='ادامه خرید');if(c){c.click();MasoudBridge.continueClicked();return true;}return false;};if(!next()){let n=new MutationObserver(()=>{if(next())n.disconnect();});n.observe(document.documentElement,{childList:true,subtree:true,attributes:true});setTimeout(()=>n.disconnect(),10000);}return;}}};window.__masoudScan=scan;if(window.__masoudObserver)window.__masoudObserver.disconnect();window.__masoudObserver=new MutationObserver(scan);window.__masoudObserver.observe(document.documentElement,{childList:true,subtree:true,attributes:true});if(window.__masoudFallback)clearInterval(window.__masoudFallback);window.__masoudFallback=setInterval(scan,100);scan();MasoudBridge.log('ناظر سریع بلیت فعال شد: واکنش ۱۰۰ میلی‌ثانیه‌ای');}catch(e){MasoudBridge.log('خطای ناظر نتایج: '+e);}})();";
        webView.evaluateJavascript(js,null);
    }

    private void enablePriceFormatting(EditText field){
        field.addTextChangedListener(new TextWatcher(){ boolean editing=false;
            @Override public void beforeTextChanged(CharSequence text,int start,int count,int after){}
            @Override public void onTextChanged(CharSequence text,int start,int before,int count){}
            @Override public void afterTextChanged(Editable value){ if(editing)return; String digits=digitsOnly(value.toString()); editing=true; if(digits.isEmpty())field.setText(""); else{ try{ field.setText(String.format(Locale.US,"%,d",Long.parseLong(digits))); }catch(Exception e){ field.setText(digits); } } field.setSelection(field.getText().length()); editing=false; }
        });
    }

    private int parseInt(String s,int def){ try{return Math.max(0,Integer.parseInt(s.trim()));}catch(Exception e){return def;} }
    private String digitsOnly(String s){ return s==null?"":s.replaceAll("[^0-9]",""); }
    private String q(String s){ return JSONObject.quote(s==null?"":s); }
    private void log(String m){ runOnUiThread(()->{ String old=logView.getText().toString(); if(old.length()>18000){ int cut=old.indexOf('\n',3000); old=cut>=0?old.substring(cut+1):old.substring(Math.min(3000,old.length())); } String time=new java.text.SimpleDateFormat("HH:mm:ss.SSS",Locale.US).format(new java.util.Date()); logView.setText(old+"["+time+"] "+m+"\n"); if(logScroll!=null)logScroll.post(()->logScroll.fullScroll(View.FOCUS_DOWN)); }); }
    private void toast(String m){ Toast.makeText(this,m,Toast.LENGTH_SHORT).show(); }
    private void saveAccount(){ getPreferences(MODE_PRIVATE).edit().putString("phone",phone.getText().toString()).putString("password",password.getText().toString()).apply(); }
    private void loadAccount(){ phone.setText(getPreferences(MODE_PRIVATE).getString("phone","")); password.setText(getPreferences(MODE_PRIVATE).getString("password","")); String u=getPreferences(MODE_PRIVATE).getString("alarm_uri",""); if(!u.isEmpty())selectedAlarmUri=Uri.parse(u); }
    private void chooseAlarmTone(){ Intent i=new Intent(RingtoneManager.ACTION_RINGTONE_PICKER); i.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_ALARM|RingtoneManager.TYPE_NOTIFICATION); i.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,"انتخاب صدای آلارم ربات"); i.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,selectedAlarmUri); startActivityForResult(i,RINGTONE_REQUEST); }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){ super.onActivityResult(requestCode,resultCode,data); if(requestCode==RINGTONE_REQUEST&&resultCode==RESULT_OK&&data!=null){ selectedAlarmUri=data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI); getPreferences(MODE_PRIVATE).edit().putString("alarm_uri",selectedAlarmUri==null?"":selectedAlarmUri.toString()).apply(); toast("صدای آلارم انتخاب شد"); } }

    private void playAlarmOnce(){ if(alarmPlayed) return; alarmPlayed=true; try{ Uri uri=selectedAlarmUri!=null?selectedAlarmUri:RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM); if(uri==null) uri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); Ringtone r=RingtoneManager.getRingtone(this,uri); if(r!=null) r.play(); }catch(Exception e){ toast("بلیت پیدا شد و رزرو انجام شد"); } }

    private class JsBridge {
        @JavascriptInterface public void log(String m){ MainActivity.this.log(m); }
        @JavascriptInterface public void sessionFailed(int token,String message){runOnUiThread(()->{if(running&&reauthRequired&&token==sessionGeneration)stopBot(message);});}
        @JavascriptInterface public void sessionReady(int token){runOnUiThread(()->{if(!running||!reauthRequired||token!=sessionGeneration)return; if(sessionDeadline!=null)handler.removeCallbacks(sessionDeadline); reauthRequired=false;logoutInProgress=false;loggedIn=false;loginInProgress=false;loginRetryCount=0; log("فرم ورود آماده است؛ اجرای ورود W6"); sessionDeadline=()->{sessionDeadline=null;if(running&&!loggedIn&&token==sessionGeneration)stopBot("ارسال فرم ورود طی ۱۰ ثانیه انجام نشد");};handler.postDelayed(sessionDeadline,10000);advanceAutomation();});}
        @JavascriptInterface public void loginMissing(){ runOnUiThread(()->{ loginInProgress=false; loginRetryCount++; if(loginRetryCount<=5){ log("ورود/عضویت پیدا نشد؛ تلاش مجدد "+loginRetryCount+" از ۵"); handler.postDelayed(()->advanceAutomation(),100); }else{ stopBot("ورود به حساب رجا انجام نشد"); } }); }
        @JavascriptInterface public void loginClicked(){ runOnUiThread(()->{ if(!running)return; if(sessionDeadline!=null){handler.removeCallbacks(sessionDeadline);sessionDeadline=null;} loginInProgress=false; loggedIn=true; loginRetryCount=0; log("دکمه ورود زده شد؛ منتظر تکمیل ورود..."); handler.postDelayed(()->advanceAutomation(),2600); }); }
        @JavascriptInterface public void searchClicked(){ runOnUiThread(()->{ searchSubmitted=true; nextRefreshAt=SystemClock.elapsedRealtime()+Math.round(getRefreshSeconds()*1000.0); log("جستجو ارسال شد؛ ناظر سریع در حال فعال‌شدن است."); handler.postDelayed(()->advanceAutomation(),100); }); }
        @JavascriptInterface public void reserveClicked(){ reserveFast("نامشخص"); }
        @JavascriptInterface public void reserveFast(String milliseconds){ runOnUiThread(()->{ reserved=true; status.setText("● بلیت پیدا شد؛ رزرو زده شد..."); log("رزرو بلیت کلیک شد | زمان واکنش داخلی="+milliseconds+" میلی‌ثانیه"); showPanel(browserPanel); if(alarm.isChecked()) playAlarmOnce(); }); }
        @JavascriptInterface public void selectionError(String message){ runOnUiThread(()->{ status.setText("■ "+message); status.setTextColor(RED); stopBot(message); }); }
        @JavascriptInterface public void continueClicked(){ runOnUiThread(()->log("ادامه خرید کلیک شد.")); }
        @JavascriptInterface public void notFound(){ runOnUiThread(()->{ reserved=false; status.setText("● یافت نشد؛ جستجوی مجدد..."); log("بلیط مطابق معیار فعلاً یافت نشد."); }); }
    }

}
