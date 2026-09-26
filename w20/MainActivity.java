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
    private boolean formInProgress = false;
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
        version.setText("W20");
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

    private LinearLayout makeBrowserPanel(){
        LinearLayout p=new LinearLayout(this);
        p.setOrientation(LinearLayout.VERTICAL);
        p.setPadding(dp(12),dp(12),dp(12),dp(12));
        TextView h=label("W20 — اجرا در Google Chrome");
        h.setGravity(Gravity.CENTER);
        h.setTextColor(BLUE);
        h.setTextSize(19);
        h.setTypeface(null,1);
        p.addView(h,new LinearLayout.LayoutParams(-1,dp(54)));

        TextView info=label("کنترل رجا در Chrome واقعی انجام می‌شود. برای اجرای خودکار، سرویس «MASOUD Raja W20» در دسترسی‌پذیری باید فعال باشد.");
        info.setGravity(Gravity.RIGHT);
        info.setTextSize(15);
        info.setPadding(dp(12),dp(12),dp(12),dp(12));
        info.setBackground(bg(FIELD,14,BORDER,1));
        p.addView(info,new LinearLayout.LayoutParams(-1,dp(112)));
        gap(p,12);

        Button access=button("فعال‌سازی دسترسی W20",PANEL_2);
        access.setTextColor(TEXT);
        access.setOnClickListener(v->openAccessibilitySettings());
        p.addView(access,new LinearLayout.LayoutParams(-1,dp(52)));
        gap(p,8);

        Button open=button("باز کردن رجا در Chrome",BLUE);
        open.setOnClickListener(v->openChrome());
        p.addView(open,new LinearLayout.LayoutParams(-1,dp(52)));
        gap(p,12);

        TextView state=label("پس از زدن «شروع ربات»، W20 اطلاعات مسیر، تاریخ و مسافران را در Chrome وارد می‌کند و رفرش را در همان Chrome انجام می‌دهد.");
        state.setGravity(Gravity.RIGHT);
        state.setTextSize(14);
        p.addView(state,new LinearLayout.LayoutParams(-1,dp(96)));
        return p;
    }

    private void setupWebView(){ }

    private boolean isChromeServiceEnabled(){
        try{
            String enabled=android.provider.Settings.Secure.getString(getContentResolver(),android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if(enabled==null)return false;
            String mine=new android.content.ComponentName(this,ChromeAutomationService.class).flattenToString();
            String e=enabled.toLowerCase(Locale.US);
            return e.contains(mine.toLowerCase(Locale.US))||e.contains(getPackageName().toLowerCase(Locale.US));
        }catch(Exception ex){return false;}
    }

    private void openAccessibilitySettings(){
        try{ startActivity(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)); }
        catch(Exception e){ toast("تنظیمات دسترسی باز نشد."); }
    }

    private boolean openChrome(){
        try{
            Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.raja.ir/"));
            i.setPackage("com.android.chrome");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            return true;
        }catch(Exception e){
            toast("Google Chrome روی گوشی پیدا نشد.");
            return false;
        }
    }

    private void startBot(){
        if(origin.getText().toString().trim().isEmpty()||destination.getText().toString().trim().isEmpty()||travelDate.getText().toString().trim().isEmpty()){ toast("مبدا، مقصد و تاریخ را کامل کن."); return; }
        if(phone.getText().toString().trim().isEmpty()||password.getText().toString().trim().isEmpty()){ toast("حساب رجا را وارد کن."); return; }
        if(!priceMode.isChecked()&&trainNumber.getText().toString().trim().isEmpty()){ toast("شماره قطار را وارد کن یا حالت بازه قیمت را فعال کن."); return; }

        saveAccount();
        long generation=System.currentTimeMillis();
        android.content.SharedPreferences.Editor e=getSharedPreferences("MASOUD_W20",MODE_PRIVATE).edit();
        e.putLong("generation",generation);
        e.putBoolean("running",true);
        e.putString("phone",phone.getText().toString().trim());
        e.putString("password",password.getText().toString());
        e.putString("origin",origin.getText().toString().trim());
        e.putString("destination",destination.getText().toString().trim());
        e.putString("date",travelDate.getText().toString().trim());
        e.putInt("adults",parseInt(adults.getText().toString(),1));
        e.putInt("children",parseInt(children.getText().toString(),0));
        e.putString("passengerType",passengerType);
        e.putBoolean("coupe",coupe.isChecked());
        e.putBoolean("priceMode",priceMode.isChecked());
        e.putString("train",trainNumber.getText().toString().trim());
        e.putInt("minPrice",parseInt(digitsOnly(minPrice.getText().toString()),0));
        e.putInt("maxPrice",parseInt(digitsOnly(maxPrice.getText().toString()),Integer.MAX_VALUE));
        e.putLong("refreshMs",Math.max(650L,Math.round(getRefreshSeconds()*1000.0)));
        e.putBoolean("alarm",alarm.isChecked());
        e.putString("alarmUri",selectedAlarmUri==null?"":selectedAlarmUri.toString());
        e.apply();

        running=true; actionState=1; reserved=false; alarmPlayed=false;
        updateActionButtons();
        logView.setText("");
        log("W20 شروع شد | Chrome | "+origin.getText()+" ← "+destination.getText()+" | "+travelDate.getText());
        status.setText("● W20 آماده اجرای Chrome");
        status.setTextColor(Color.rgb(0,130,75));
        showPanel(browserPanel);

        if(!isChromeServiceEnabled()){
            status.setText("■ ابتدا دسترسی MASOUD Raja W20 را فعال کن");
            status.setTextColor(RED);
            openAccessibilitySettings();
            return;
        }
        if(!openChrome()){
            getSharedPreferences("MASOUD_W20",MODE_PRIVATE).edit().putBoolean("running",false).apply();
            running=false; actionState=2; updateActionButtons();
        }
    }

    private void stopBot(String why){
        running=false; actionState=2;
        getSharedPreferences("MASOUD_W20",MODE_PRIVATE).edit().putBoolean("running",false).apply();
        updateActionButtons();
        status.setText("■ "+why);
        status.setTextColor(RED);
        log(why);
    }

    private void startMonitorLoop(){ }
    private double getRefreshSeconds(){ return refreshSlider==null?2.0:0.5+(refreshSlider.getProgress()*0.5); }
    private String passengerTypeLabel(){ return "men".equals(passengerType)?"ویژه برادران":"women".equals(passengerType)?"ویژه خواهران":"مسافران عادی"; }
    private void advanceAutomation(){ }

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
        log("بررسی فرم و تأیید ورود به حساب...");
        sessionDeadline=()->{sessionDeadline=null;if(running&&!loggedIn&&token==sessionGeneration)stopBot("ورود به حساب در مهلت تعیین‌شده تأیید نشد");};
        handler.postDelayed(sessionDeadline,45000);
        String js="(async function(){\nif(window.__masoudSessionCancel)window.__masoudSessionCancel();\nconst cfg="+"{\"token\":"+token+",\"phone\":"+q(phone.getText().toString().trim())+",\"password\":"+q(password.getText().toString())+"}"+"; let cancelled=false;\nwindow.__masoudSessionCancel=()=>{cancelled=true;};\nconst norm=s=>String(s||'').replace(/[\\u200c\\u200e\\u200f]/g,' ').replace(/ي/g,'ی').replace(/ك/g,'ک').replace(/\\s+/g,' ').trim();\nconst text=e=>norm(e&&(e.innerText||e.textContent));\nconst vis=e=>e&&e.getClientRects().length>0&&getComputedStyle(e).visibility!=='hidden';\nconst nodes=sel=>[...document.querySelectorAll(sel)].filter(vis);\nconst sleep=ms=>new Promise(r=>setTimeout(r,ms));\nconst wait=async(fn,ms=12000)=>{let end=Date.now()+ms;while(!cancelled&&Date.now()<end){let v=fn();if(v)return v;await sleep(100);}return null;};\nconst fail=m=>{if(!cancelled)MasoudBridge.moduleFailed(cfg.token,m);};\nconst loginButton=()=>nodes('a,button,span,div').filter(e=>/^ورود\\s*\\/\\s*عضویت$/.test(text(e))).sort((a,b)=>a.querySelectorAll('*').length-b.querySelectorAll('*').length)[0];\nconst accountReady=()=>!loginButton()&&!nodes('input[type=password]').length&&nodes('a,button,li,span').some(e=>/^(خروج|خروج از حساب|پروفایل شخصی|اطلاعات مشتریان)$/.test(text(e)));\nlet openedAccount=false;\nconst inspectAccount=()=>{if(accountReady())return true;if(!loginButton()&&!nodes('input[type=password]').length&&!openedAccount){let t=document.querySelector('#dropdownForm1');if(vis(t)&&text(t)&&!/ورود|عضویت/.test(text(t))){openedAccount=true;t.click();}}return accountReady();};\nfunction form(){\nfor(const pwd of nodes('input[type=password]')){\n for(let box=pwd.parentElement;box&&box!==document.body;box=box.parentElement){\n  const inputs=[...box.querySelectorAll('input')].filter(vis);\n  const mobile=inputs.find(e=>e!==pwd&&!['password','hidden','checkbox','radio','submit','button'].includes(e.type));\n  const submit=[...box.querySelectorAll('button,input[type=submit]')].filter(vis).find(e=>text(e)==='ورود'||e.value==='ورود');\n  if(mobile&&submit)return {box,mobile,pwd,submit};\n }\n}return null;}\ntry{\nlet state=await wait(()=>inspectAccount()?'account':form()?'form':loginButton()?'button':null);\nif(!state){fail('ورود / عضویت یا حساب فعال آماده نشد');return;}\nif(state==='account'){MasoudBridge.loginReady(cfg.token);return;}\nif(state==='button'){loginButton().click();await sleep(700);}\nlet f=await wait(form);if(!f){fail('فرم شماره و رمز ورود آماده نشد');return;}\nconst set=(e,value)=>{const d=Object.getOwnPropertyDescriptor(HTMLInputElement.prototype,'value');d.set.call(e,value);e.dispatchEvent(new Event('input',{bubbles:true}));e.dispatchEvent(new Event('change',{bubbles:true}));};\nset(f.mobile,cfg.phone);set(f.pwd,cfg.password);\nconst filled=await wait(()=>{let n=form();return n&&n.mobile.value===cfg.phone&&n.pwd.value===cfg.password&&n;},2000);\nif(!filled){fail('شماره یا رمز در فرم ورود ثبت نشد');return;}\nif(cancelled)return;\nif(filled.submit.disabled||filled.submit.getAttribute('aria-disabled')==='true'){fail('دکمه ورود هنوز فعال نیست');return;}\nfilled.submit.click();MasoudBridge.loginSent(cfg.token);\nawait sleep(2600);\nif(await wait(inspectAccount,15000)){if(!cancelled)MasoudBridge.loginReady(cfg.token);}\nelse fail('ورود به حساب تأیید نشد؛ مراحل جستجو اجرا نشد');\n}catch(e){fail('خطای ورود: '+e.message);}\n})();";
        webView.evaluateJavascript(js,null);
    }

    private void injectSearchForm(){
        int total=parseInt(adults.getText().toString(),1)+parseInt(children.getText().toString(),0);
        String js="(async function(){const sleep=ms=>new Promise(r=>setTimeout(r,ms));const fire=(el)=>{el.dispatchEvent(new Event('input',{bubbles:true}));el.dispatchEvent(new Event('change',{bubbles:true}));};try{let change=[...document.querySelectorAll('button,div,a')].find(x=>(x.innerText||'').trim()==='تغییر جستجو');if(change){change.click();await sleep(100);}async function station(kind,city){let comps=[...document.querySelectorAll('app-stations')];let c=comps.find(x=>((x.getAttribute('name')||'').toLowerCase()).includes(kind==='from'?'from':'to'));if(!c)return false;let clear=c.querySelector('.ng-clear-wrapper');if(clear)clear.click();let inp=c.querySelector('input[role=combobox]');if(!inp)return false;inp.focus();inp.value=city;fire(inp);await sleep(50);let opts=[...document.querySelectorAll('ng-dropdown-panel .ng-option')];let o=opts.find(x=>(x.innerText||'').trim()===city)||opts.find(x=>(x.innerText||'').includes(city));if(o){o.click();await sleep(80);return true;}return false;}let a=await station('from',"+q(origin.getText().toString().trim())+");let b=await station('to',"+q(destination.getText().toString().trim())+");if(!a||!b){MasoudBridge.log('مبدا یا مقصد تنظیم نشد');return;}let dateText="+q(travelDate.getText().toString().trim())+";let parts=dateText.replace(/-/g,'/').split('/').map(Number);let picker=document.querySelector('app-datepicker-single[name=oneWayDatePicker]');if(!picker||parts.length!==3){MasoudBridge.log('تقویم رجا پیدا نشد');return;}let cal=picker.querySelector('button.icon-calendar')||picker.querySelector('input[name=dp]');if(cal)cal.click();await sleep(50);let jy=parts[0],jm=parts[1],jd=parts[2];let monthNames=['','فروردین','اردیبهشت','خرداد','تیر','مرداد','شهریور','مهر','آبان','آذر','دی','بهمن','اسفند'];for(let sel of [...document.querySelectorAll('select')].filter(x=>x.offsetParent!==null)){let opts=[...sel.options];let yo=opts.find(o=>(o.textContent||'').includes(String(jy)));if(yo){sel.value=yo.value;fire(sel);await sleep(80);continue;}let mo=opts.find(o=>(o.textContent||'').includes(monthNames[jm])||(o.textContent||'').trim()===String(jm));if(mo){sel.value=mo.value;fire(sel);await sleep(80);}}let dayFa=String(jd).replace(/[0-9]/g,d=>'۰۱۲۳۴۵۶۷۸۹'[Number(d)]);let nodes=[...document.querySelectorAll('button,td,span,div')].filter(x=>x.offsetParent!==null);let day=nodes.find(x=>{let t=(x.innerText||'').trim();let c=(x.className||'').toString().toLowerCase();return (t===String(jd)||t===dayFa)&&!c.includes('disabled')&&!c.includes('muted')&&!c.includes('outside');});if(!day){MasoudBridge.log('روز تاریخ در تقویم پیدا نشد');return;}day.click();await sleep(50);let dateInp=picker.querySelector('input[name=dp]');if(!dateInp||!(dateInp.value||'').trim()){MasoudBridge.log('تاریخ در رجا ثبت نشد');return;}let pb=document.querySelector('#dropdownPassenger')||[...document.querySelectorAll('button,div,[role=button]')].filter(x=>x.offsetParent!==null).filter(x=>/مسافر/.test((x.innerText||'').trim())).sort((a,b)=>(a.innerText||'').length-(b.innerText||'').length)[0];if(pb){pb.click();await sleep(100);async function setPassenger(label,want){let candidates=[...document.querySelectorAll('div,li')].filter(x=>x.offsetParent!==null&&(x.innerText||'').includes(label));let row=candidates.sort((a,b)=>(a.innerText||'').length-(b.innerText||'').length).find(x=>x.querySelectorAll('button').length>=2)||candidates[0];if(!row)return false;for(let p=row;p&&p!==document.body&&p.querySelectorAll('button').length<2;p=p.parentElement)row=p;let buttons=[...row.querySelectorAll('button')].filter(x=>x.offsetParent!==null);let plus=buttons.find(x=>/\\+|افزایش/.test((x.innerText||'')+(x.getAttribute('aria-label')||'')))||buttons[buttons.length-1];let minus=buttons.find(x=>/−|-|کاهش/.test((x.innerText||'')+(x.getAttribute('aria-label')||'')))||buttons[0];let nums=((row.innerText||'').match(/[0-9۰-۹]+/g)||[]).map(n=>Number(n.replace(/[۰-۹]/g,d=>'۰۱۲۳۴۵۶۷۸۹'.indexOf(d))));let cur=nums.find(n=>n>=0&&n<=6);if(cur===undefined)cur=label==='بزرگسال'?1:0;while(cur<want&&plus){plus.click();cur++;await sleep(50);}while(cur>want&&minus){minus.click();cur--;await sleep(50);}return true;}let adultOk=await setPassenger('بزرگسال',"+parseInt(adults.getText().toString(),1)+");let childOk=await setPassenger('خردسال',"+parseInt(children.getText().toString(),0)+");if(!childOk)childOk=await setPassenger('کودک',"+parseInt(children.getText().toString(),0)+");if(adultOk&&childOk)MasoudBridge.log('تعداد بزرگسال و کودک در رجا تنظیم شد');else MasoudBridge.log('کنترل تعداد مسافران در رجا کامل پیدا نشد');}await sleep(100);let pt="+q(passengerType)+";let typeLabel=pt==='men'?'ویژه برادران':pt==='women'?'ویژه خواهران':'مسافران عادی';let allType=[...document.querySelectorAll('ng-select,.ng-select-container,[role=combobox],button,div,span')].filter(x=>x.offsetParent!==null);let leaf=allType.filter(x=>{let t=(x.innerText||x.value||'').trim();return t==='مسافران عادی'||t==='ویژه برادران'||t==='ویژه خواهران';}).sort((a,b)=>(a.innerText||'').length-(b.innerText||'').length)[0];let typeControl=leaf&&(leaf.closest('ng-select,[role=combobox],button,.ng-select-container')||leaf);if(!typeControl){MasoudBridge.selectionError('کادر نوع مسافر در رجا پیدا نشد');return;}typeControl.click();await sleep(100);let choices=[...document.querySelectorAll('.ng-option,[role=option],li,button,div,span')].filter(x=>x.offsetParent!==null&&(x.innerText||'').trim()===typeLabel).sort((a,b)=>(a.innerText||'').length-(b.innerText||'').length);let choice=choices[0];if(choice){let target=choice.closest('.ng-option,[role=option],li,button')||choice;target.click();await sleep(100);}let current=[...document.querySelectorAll('ng-select,.ng-select-container,[role=combobox],button,div,span')].filter(x=>x.offsetParent!==null).some(x=>(x.innerText||'').trim()===typeLabel);if(!choice||!current){MasoudBridge.selectionError('انتخاب نوع مسافر تأیید نشد: '+typeLabel);return;}MasoudBridge.log('نوع مسافر در رجا تأیید شد: '+typeLabel);"+(coupe.isChecked()?"let cc=[...document.querySelectorAll('input[type=checkbox]')].find(x=>((x.parentElement?.innerText)||'').includes('کوپه دربست'));if(cc&&!cc.checked)cc.click();":"")+"let bs=[...document.querySelectorAll('button')].filter(x=>x.offsetParent!==null);let s=bs.find(x=>(x.innerText||'').trim().includes('جستجو'));if(s){s.click();MasoudBridge.searchClicked();}else MasoudBridge.log('دکمه جستجو پیدا نشد');}catch(e){MasoudBridge.log('خطای تنظیم جستجو: '+e);}})();";
        webView.evaluateJavascript(js,null);
    }

    private void injectSearchFormV11(){
        if(formInProgress)return; formInProgress=true; final int token=sessionGeneration;
        final int adultCount=parseInt(adults.getText().toString(),1);
        final int childCount=parseInt(children.getText().toString(),0);
        String js="(async function(){const token="+token+";window.__masoudFormCancelled=false;"
            +"const sleep=ms=>new Promise(r=>setTimeout(r,ms)).then(()=>{if(window.__masoudFormCancelled)throw new Error('cancelled');});"
            +"const vis=e=>e&&e.offsetParent!==null;"
            +"const text=e=>String((e&&(e.innerText||e.textContent||e.value))||'').replace(/\\s+/g,' ').trim();"
            +"const fire=e=>{e.dispatchEvent(new Event('input',{bubbles:true}));e.dispatchEvent(new Event('change',{bubbles:true}));};"
            +"const wait=async fn=>{for(let i=0;i<80;i++){let v=fn();if(v)return v;await sleep(50);}return null;};"
            +"const fail=m=>{MasoudBridge.moduleFailed(token,m);return false;};"
            +"try{"
            +"let change=[...document.querySelectorAll('button,a,[role=button],div,span')].filter(vis).find(e=>text(e)==='تغییر جستجو');if(change){change.click();await sleep(100);}"
            +"async function station(kind,city){let c=await wait(()=>[...document.querySelectorAll('app-stations')].find(x=>String(x.getAttribute('name')||'').toLowerCase().includes(kind)));if(!c)return false;let clear=c.querySelector('.ng-clear-wrapper');if(clear)clear.click();let inp=c.querySelector('input[role=combobox],input');if(!inp)return false;inp.focus();let d=Object.getOwnPropertyDescriptor(HTMLInputElement.prototype,'value');if(d&&d.set)d.set.call(inp,city);else inp.value=city;fire(inp);let option=await wait(()=>[...document.querySelectorAll('ng-dropdown-panel .ng-option,[role=option]')].filter(vis).find(x=>text(x)===city||text(x).includes(city)));if(!option)return false;option.click();await sleep(100);return text(c).includes(city);}"
            +"let fromOk=await station('from',"+q(origin.getText().toString().trim())+");if(!fromOk){fail('مبدا در رجا تنظیم نشد');return;}MasoudBridge.log('مبدا در رجا تأیید شد');"
            +"let toOk=await station('to',"+q(destination.getText().toString().trim())+");if(!toOk){fail('مقصد در رجا تنظیم نشد');return;}MasoudBridge.log('مقصد در رجا تأیید شد');"
            +"let parts="+q(travelDate.getText().toString().trim())+".replace(/-/g,'/').split('/').map(Number);let picker=await wait(()=>document.querySelector('app-datepicker-single[name=oneWayDatePicker],app-datepicker-single'));if(!picker||parts.length!==3){fail('تقویم رجا پیدا نشد');return;}let cal=picker.querySelector('button.icon-calendar,input[name=dp],input');if(cal)cal.click();let jy=parts[0],jm=parts[1],jd=parts[2],months=['','فروردین','اردیبهشت','خرداد','تیر','مرداد','شهریور','مهر','آبان','آذر','دی','بهمن','اسفند'];await sleep(100);for(let sel of [...document.querySelectorAll('select')].filter(vis)){let opts=[...sel.options];let yo=opts.find(o=>text(o).includes(String(jy)));if(yo){sel.value=yo.value;fire(sel);await sleep(80);continue;}let mo=opts.find(o=>text(o).includes(months[jm])||text(o)===String(jm));if(mo){sel.value=mo.value;fire(sel);await sleep(80);}}let fa=String(jd).replace(/[0-9]/g,d=>'۰۱۲۳۴۵۶۷۸۹'[Number(d)]);let day=await wait(()=>[...document.querySelectorAll('button,td,[role=gridcell],span,div')].filter(vis).find(x=>{let t=text(x),c=String(x.className||'').toLowerCase();return(t===String(jd)||t===fa)&&!c.includes('disabled')&&!c.includes('muted')&&!c.includes('outside');}));if(!day){fail('روز تاریخ در تقویم پیدا نشد');return;}day.click();let dateInput=await wait(()=>picker.querySelector('input[name=dp],input'));if(!dateInput){fail('فیلد تاریخ رجا پیدا نشد');return;}await sleep(100);const dateNums=v=>String(v||'').replace(/[۰-۹]/g,d=>'۰۱۲۳۴۵۶۷۸۹'.indexOf(d)).replace(/[٠-٩]/g,d=>'٠١٢٣٤٥٦٧٨٩'.indexOf(d)).split(/[^0-9]+/).filter(Boolean).map(Number).join('-');if(!await wait(()=>dateNums(dateInput.value)===parts.join('-'))){fail('تاریخ انتخاب‌شده با تنظیمات ربات یکسان نیست');return;}MasoudBridge.log('تاریخ در رجا تأیید شد: '+dateInput.value);"
            +"let pb=await wait(()=>document.querySelector('#dropdownPassenger')||[...document.querySelectorAll('button,[role=button],div')].filter(vis).filter(x=>/مسافر/.test(text(x))).sort((a,b)=>text(a).length-text(b).length)[0]);if(!pb){fail('کادر تعداد مسافر در رجا پیدا نشد');return;}pb.click();await sleep(500);"
            +"async function setCount(label,want,def){let row=await wait(()=>{let cs=[...document.querySelectorAll('div,li')].filter(vis).filter(x=>text(x).includes(label));return cs.sort((a,b)=>text(a).length-text(b).length).find(x=>x.querySelectorAll('button').length>=2)||null;});if(!row)return false;const read=()=>{let input=[...row.querySelectorAll('input')].filter(vis).find(e=>/^[0-9۰-۹]+$/.test(e.value));if(input)return Number(input.value.replace(/[۰-۹]/g,d=>'۰۱۲۳۴۵۶۷۸۹'.indexOf(d)));let leaf=[...row.querySelectorAll('span,div,button')].filter(vis).find(e=>/^[0-9۰-۹]+$/.test(text(e)));return leaf?Number(text(leaf).replace(/[۰-۹]/g,d=>'۰۱۲۳۴۵۶۷۸۹'.indexOf(d))):null;};let buttons=[...row.querySelectorAll('button')].filter(vis),plus=buttons.find(x=>/+|افزایش/.test(text(x)+(x.getAttribute('aria-label')||'')))||buttons[buttons.length-1],minus=buttons.find(x=>/−|-|کاهش/.test(text(x)+(x.getAttribute('aria-label')||'')))||buttons[0];for(let tries=0;tries<12;tries++){let cur=read();if(cur===null)return false;if(cur===want)return true;let btn=cur<want?plus:minus;if(!btn||btn.disabled)return false;btn.click();if(!await wait(()=>read()!==cur))return false;}return read()===want;}"
            +"let adultOk=await setCount('بزرگسال',"+adultCount+",1),childOk=await setCount('خردسال',"+childCount+",0);if(!childOk)childOk=await setCount('کودک',"+childCount+",0);if(!adultOk||!childOk){fail('تنظیم تعداد بزرگسال یا کودک تأیید نشد');return;}let confirm=[...document.querySelectorAll('button,[role=button]')].filter(vis).find(x=>text(x)==='تأیید'||text(x)==='تایید');if(confirm){confirm.click();await sleep(800);}else{pb.click();await sleep(500);}MasoudBridge.log('تعداد بزرگسال و کودک در رجا تأیید شد');"
            +"let wanted="+q(passengerTypeLabel())+";"
            +"const canonical=s=>String(s||'').replace(/[\\u200c\\u200e\\u200f]/g,' ').replace(/ي/g,'ی').replace(/ك/g,'ک').replace(/مسافرین/g,'مسافران').replace(/\\s+/g,' ').trim();\nconst typeNames=['مسافران عادی','ویژه برادران','ویژه خواهران'];\nconst selected=e=>canonical(e.tagName==='SELECT'?e.options[e.selectedIndex]?.textContent:text(e));\nconst findType=()=>{\nlet native=[...document.querySelectorAll('select')].filter(vis).find(e=>[...e.options].filter(o=>typeNames.includes(canonical(o.textContent))).length>=2);if(native)return native;\nlet leaf=[...document.querySelectorAll('ng-select,.ng-value-label,.ng-select-container,[role=combobox],button,a,div,span,label')].filter(vis).filter(e=>typeNames.includes(canonical(text(e)))&&!e.closest('ng-dropdown-panel,[role=listbox],.dropdown-menu')).sort((a,b)=>a.querySelectorAll('*').length-b.querySelectorAll('*').length)[0];\nreturn leaf&&(leaf.closest('ng-select,button,a,[role=combobox],[data-toggle],.ng-select-container')||leaf);\n};\nlet typeControl=await wait(findType);if(!typeControl){fail('کادر نوع مسافر در رجا پیدا نشد');return;}\ntypeControl.scrollIntoView({block:'center',behavior:'instant'});\nif(selected(typeControl)!==wanted){\nif(typeControl.tagName==='SELECT'){let option=[...typeControl.options].find(o=>canonical(o.textContent)===wanted);if(!option||option.disabled){fail('گزینه نوع مسافر موجود نیست');return;}typeControl.value=option.value;fire(typeControl);}\nelse {typeControl.click();let choice=await wait(()=>[...document.querySelectorAll('ng-dropdown-panel .ng-option,[role=option],.dropdown-menu a,.dropdown-menu button,.dropdown-menu li,li,a,button,span')].filter(vis).filter(e=>e!==typeControl&&!typeControl.contains(e)&&canonical(text(e))===wanted).sort((a,b)=>a.querySelectorAll('*').length-b.querySelectorAll('*').length)[0]);if(!choice){fail('گزینه نوع مسافر پیدا نشد: '+wanted);return;}(choice.closest('.ng-option,[role=option],a,button,li')||choice).click();}\n}\nif(!await wait(()=>{const current=findType();return current&&selected(current)===wanted;})){fail('انتخاب نوع مسافر تأیید نشد: '+wanted);return;}\nMasoudBridge.log('نوع مسافر در رجا تأیید شد: '+wanted);"
            +(""+(coupe.isChecked()?"let cc=[...document.querySelectorAll('input[type=checkbox]')].find(x=>text(x.parentElement).includes('کوپه دربست'));if(cc&&!cc.checked){cc.click();await sleep(80);}MasoudBridge.log('کوپه دربست در رجا اعمال شد');":""))
            +"let search=await wait(()=>[...document.querySelectorAll('button,[role=button],input[type=submit]')].filter(vis).find(x=>/جستجو/.test(text(x))));if(!search){fail('دکمه جستجوی رجا پیدا نشد');return;}if(window.__masoudFormCancelled)return;search.scrollIntoView({block:'center'});search.click();MasoudBridge.searchReady(token);"
            +"}catch(e){if(!window.__masoudFormCancelled)MasoudBridge.moduleFailed(token,'خطای تنظیم جستجو: '+e);}})();";
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


    public static class ChromeAutomationService extends android.accessibilityservice.AccessibilityService {
        private final Handler h=new Handler(Looper.getMainLooper());
        private android.content.SharedPreferences cfg;
        private long seenGeneration=-1L;
        private int stage=0;
        private long stageAt=0L;
        private long lastRefresh=0L;
        private boolean alarmDone=false;
        private boolean typeOpened=false;

        private final Runnable loop=new Runnable(){
            @Override public void run(){
                try{ process(); }catch(Throwable ignored){}
                h.postDelayed(this,180);
            }
        };

        @Override protected void onServiceConnected(){
            super.onServiceConnected();
            cfg=getSharedPreferences("MASOUD_W20",MODE_PRIVATE);
            h.removeCallbacks(loop);
            h.post(loop);
        }

        @Override public void onAccessibilityEvent(android.view.accessibility.AccessibilityEvent event){
            if(event==null)return;
            CharSequence p=event.getPackageName();
            if(p!=null&&"com.android.chrome".contentEquals(p)) h.removeCallbacks(loop);
            if(p!=null&&"com.android.chrome".contentEquals(p)) h.postDelayed(loop,80);
        }

        @Override public void onInterrupt(){}

        @Override public void onDestroy(){
            h.removeCallbacksAndMessages(null);
            super.onDestroy();
        }

        private void setStage(int s){ stage=s; stageAt=SystemClock.elapsedRealtime(); }

        private void process(){
            if(cfg==null)cfg=getSharedPreferences("MASOUD_W20",MODE_PRIVATE);
            if(!cfg.getBoolean("running",false))return;
            long gen=cfg.getLong("generation",0L);
            if(gen!=seenGeneration){
                seenGeneration=gen; stage=0; stageAt=SystemClock.elapsedRealtime(); lastRefresh=0L; alarmDone=false; typeOpened=false;
            }
            android.view.accessibility.AccessibilityNodeInfo root=getRootInActiveWindow();
            if(root==null)return;
            CharSequence pkg=root.getPackageName();
            if(pkg!=null&&!"com.android.chrome".contentEquals(pkg))return;

            if(stage==0){
                if(routeReady(root)){setStage(2);return;}
                handleLogin(root);
                return;
            }

            if(stage==2){
                String v=cfg.getString("origin","");
                if(setField(root,new String[]{"مبدا","origin","from"},v)){setStage(3);return;}
                clickText(root,"مبدا");
                return;
            }
            if(stage==3){
                String v=cfg.getString("origin","");
                android.view.accessibility.AccessibilityNodeInfo n=findExactNonEditable(root,v);
                if(n!=null&&click(n)){setStage(4);return;}
                if(SystemClock.elapsedRealtime()-stageAt>7000&&fieldHasValue(root,new String[]{"مبدا","origin","from"},v)){setStage(4);}
                return;
            }
            if(stage==4){
                String v=cfg.getString("destination","");
                if(setField(root,new String[]{"مقصد","destination","to"},v)){setStage(5);return;}
                clickText(root,"مقصد");
                return;
            }
            if(stage==5){
                String v=cfg.getString("destination","");
                android.view.accessibility.AccessibilityNodeInfo n=findExactNonEditable(root,v);
                if(n!=null&&click(n)){setStage(6);return;}
                if(SystemClock.elapsedRealtime()-stageAt>7000&&fieldHasValue(root,new String[]{"مقصد","destination","to"},v)){setStage(6);}
                return;
            }
            if(stage==6){
                String date=cfg.getString("date","");
                if(setField(root,new String[]{"تاریخ","date"},date)){setStage(8);return;}
                if(clickText(root,"تاریخ رفت")||clickText(root,"تاریخ سفر")||clickText(root,"تاریخ")){setStage(7);}
                return;
            }
            if(stage==7){
                String date=cfg.getString("date","");
                int[] p=dateParts(date);
                if(p[2]>0){
                    String month=monthName(p[1]);
                    if(month.length()>0&&hasText(root,month)){
                        String d=String.valueOf(p[2]);
                        String fa=toFa(d);
                        android.view.accessibility.AccessibilityNodeInfo day=findExact(root,d,fa);
                        if(day!=null&&click(day)){setStage(8);return;}
                    }else{
                        if(clickText(root,"ماه بعد")||clickText(root,"بعدی")||clickText(root,"Next"))return;
                    }
                }
                if(SystemClock.elapsedRealtime()-stageAt>9000){
                    android.view.accessibility.AccessibilityNodeInfo ed=findEditable(root,new String[]{"تاریخ","date"});
                    if(ed!=null&&setText(ed,date)){setStage(8);}
                }
                return;
            }
            if(stage==8){
                if(hasText(root,"بزرگسال")&&(hasText(root,"کودک")||hasText(root,"خردسال"))){setStage(9);return;}
                clickShortest(root,new String[]{"مسافران","مسافر"});
                return;
            }
            if(stage==9){
                int adult=cfg.getInt("adults",1);
                int child=cfg.getInt("children",0);
                boolean a=adjustCounter(root,new String[]{"بزرگسال"},adult);
                if(!a)return;
                boolean c=adjustCounter(root,new String[]{"کودک","خردسال"},child);
                if(!c)return;
                clickText(root,"تأیید"); clickText(root,"اعمال");
                setStage(10); return;
            }
            if(stage==10){
                String pt=cfg.getString("passengerType","normal");
                if("normal".equals(pt)){setStage(11);return;}
                String wanted="men".equals(pt)?"ویژه برادران":"ویژه خواهران";
                if(hasText(root,wanted)){
                    android.view.accessibility.AccessibilityNodeInfo n=findText(root,wanted);
                    if(n!=null&&click(n)){setStage(11);return;}
                }
                if(!typeOpened){
                    typeOpened=clickShortest(root,new String[]{"مسافران عادی","نوع مسافر","ویژه"});
                    return;
                }
                if(SystemClock.elapsedRealtime()-stageAt>6000){setStage(11);}
                return;
            }
            if(stage==11){
                if(!cfg.getBoolean("coupe",false)){setStage(12);return;}
                android.view.accessibility.AccessibilityNodeInfo n=findText(root,"کوپه دربست");
                if(n!=null){
                    if(n.isCheckable()&&n.isChecked()){setStage(12);return;}
                    click(n); setStage(12); return;
                }
                if(SystemClock.elapsedRealtime()-stageAt>5000)setStage(12);
                return;
            }
            if(stage==12){
                clickText(root,"تأیید"); clickText(root,"اعمال");
                if(clickShortest(root,new String[]{"جستجو","جستجوی قطار","جستجوی بلیت"})){
                    lastRefresh=SystemClock.elapsedRealtime(); setStage(13); return;
                }
                return;
            }
            if(stage==13){
                if(isPassengerPage(root)){finishReserved();return;}
                if(findAndChooseTrain(root)){setStage(14);return;}
                long now=SystemClock.elapsedRealtime();
                long interval=Math.max(650L,cfg.getLong("refreshMs",2000L));
                if(now-lastRefresh>=interval){lastRefresh=now;pullRefresh();}
                return;
            }
            if(stage==14){
                if(isPassengerPage(root)){finishReserved();return;}
                if(clickShortest(root,new String[]{"ادامه خرید","ادامه","ثبت مسافر"})){setStage(15);return;}
                if(SystemClock.elapsedRealtime()-stageAt>10000){setStage(13);}
                return;
            }
            if(stage==15){
                if(isPassengerPage(root)){finishReserved();return;}
                if(SystemClock.elapsedRealtime()-stageAt>12000){finishReserved();}
            }
        }

        private void handleLogin(android.view.accessibility.AccessibilityNodeInfo root){
            android.view.accessibility.AccessibilityNodeInfo pwd=null, mobile=null;
            java.util.List<android.view.accessibility.AccessibilityNodeInfo> nodes=all(root);
            for(android.view.accessibility.AccessibilityNodeInfo n:nodes){
                if(!isEditable(n))continue;
                if(n.isPassword()){pwd=n;continue;}
                String z=nodeText(n);
                if(mobile==null||z.contains("موبایل")||z.contains("شماره")||z.contains("phone"))mobile=n;
            }
            if(pwd!=null){
                if(mobile!=null)setText(mobile,cfg.getString("phone",""));
                setText(pwd,cfg.getString("password",""));
                clickShortest(root,new String[]{"ورود"});
                return;
            }
            android.view.accessibility.AccessibilityNodeInfo login=findText(root,"ورود / عضویت","ورود/عضویت");
            if(login!=null){click(login);return;}
            if(routeReady(root))setStage(2);
        }

        private boolean routeReady(android.view.accessibility.AccessibilityNodeInfo root){
            return hasText(root,"مبدا")&&hasText(root,"مقصد")&&(hasText(root,"تاریخ")||findEditable(root,new String[]{"date"})!=null);
        }

        private boolean setField(android.view.accessibility.AccessibilityNodeInfo root,String[] hints,String value){
            android.view.accessibility.AccessibilityNodeInfo e=findEditable(root,hints);
            if(e!=null){
                click(e);
                if(setText(e,value))return true;
            }
            android.view.accessibility.AccessibilityNodeInfo focused=findFocusedEditable(root);
            return focused!=null&&setText(focused,value);
        }

        private boolean fieldHasValue(android.view.accessibility.AccessibilityNodeInfo root,String[] hints,String value){
            android.view.accessibility.AccessibilityNodeInfo e=findEditable(root,hints);
            return e!=null&&norm(nodeText(e)).contains(norm(value));
        }

        private android.view.accessibility.AccessibilityNodeInfo findEditable(android.view.accessibility.AccessibilityNodeInfo root,String[] hints){
            java.util.List<android.view.accessibility.AccessibilityNodeInfo> nodes=all(root);
            android.view.accessibility.AccessibilityNodeInfo fallback=null;
            for(android.view.accessibility.AccessibilityNodeInfo n:nodes){
                if(!isEditable(n)||n.isPassword())continue;
                if(fallback==null)fallback=n;
                String z=nodeInfo(n);
                for(String k:hints)if(z.contains(norm(k)))return n;
            }
            android.view.accessibility.AccessibilityNodeInfo label=findText(root,hints);
            if(label!=null){
                android.graphics.Rect lr=new android.graphics.Rect();label.getBoundsInScreen(lr);
                android.view.accessibility.AccessibilityNodeInfo best=null;int dist=Integer.MAX_VALUE;
                for(android.view.accessibility.AccessibilityNodeInfo n:nodes){
                    if(!isEditable(n)||n.isPassword())continue;
                    android.graphics.Rect r=new android.graphics.Rect();n.getBoundsInScreen(r);
                    int d=Math.abs(r.centerY()-lr.centerY())+Math.abs(r.centerX()-lr.centerX())/3;
                    if(d<dist){dist=d;best=n;}
                }
                if(best!=null&&dist<900)return best;
            }
            return fallback;
        }

        private android.view.accessibility.AccessibilityNodeInfo findFocusedEditable(android.view.accessibility.AccessibilityNodeInfo root){
            for(android.view.accessibility.AccessibilityNodeInfo n:all(root))if(isEditable(n)&&n.isFocused()&&!n.isPassword())return n;
            return null;
        }

        private boolean isEditable(android.view.accessibility.AccessibilityNodeInfo n){
            if(n==null)return false;
            if(n.isEditable())return true;
            CharSequence c=n.getClassName();
            return c!=null&&c.toString().toLowerCase(Locale.US).contains("edittext");
        }

        private boolean setText(android.view.accessibility.AccessibilityNodeInfo n,String value){
            if(n==null)return false;
            try{
                android.os.Bundle b=new android.os.Bundle();
                b.putCharSequence(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,value);
                n.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_FOCUS);
                return n.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT,b);
            }catch(Exception e){return false;}
        }

        private boolean adjustCounter(android.view.accessibility.AccessibilityNodeInfo root,String[] labels,int want){
            android.view.accessibility.AccessibilityNodeInfo label=findText(root,labels);
            if(label==null)return false;
            Integer cur=numberNear(root,label);
            if(cur==null)return false;
            if(cur==want)return true;
            boolean plus=cur<want;
            android.view.accessibility.AccessibilityNodeInfo action=counterActionNear(root,label,plus);
            if(action==null)return false;
            click(action);
            return false;
        }

        private Integer numberNear(android.view.accessibility.AccessibilityNodeInfo root,android.view.accessibility.AccessibilityNodeInfo label){
            android.graphics.Rect lr=new android.graphics.Rect();label.getBoundsInScreen(lr);
            Integer best=null;int bestD=Integer.MAX_VALUE;
            for(android.view.accessibility.AccessibilityNodeInfo n:all(root)){
                String z=toLatinDigits(norm(nodeText(n))).replaceAll("[^0-9]","");
                if(z.length()==0||z.length()>2)continue;
                int v;try{v=Integer.parseInt(z);}catch(Exception e){continue;}
                if(v<0||v>9)continue;
                android.graphics.Rect r=new android.graphics.Rect();n.getBoundsInScreen(r);
                int d=Math.abs(r.centerY()-lr.centerY())+Math.abs(r.centerX()-lr.centerX())/8;
                if(d<bestD){bestD=d;best=v;}
            }
            return bestD<500?best:null;
        }

        private android.view.accessibility.AccessibilityNodeInfo counterActionNear(android.view.accessibility.AccessibilityNodeInfo root,android.view.accessibility.AccessibilityNodeInfo label,boolean plus){
            android.graphics.Rect lr=new android.graphics.Rect();label.getBoundsInScreen(lr);
            android.view.accessibility.AccessibilityNodeInfo best=null;int bestD=Integer.MAX_VALUE;
            java.util.List<android.view.accessibility.AccessibilityNodeInfo> near=new java.util.ArrayList<>();
            for(android.view.accessibility.AccessibilityNodeInfo n:all(root)){
                android.view.accessibility.AccessibilityNodeInfo c=clickable(n);
                if(c==null)continue;
                android.graphics.Rect r=new android.graphics.Rect();c.getBoundsInScreen(r);
                if(Math.abs(r.centerY()-lr.centerY())>220)continue;
                String z=norm(nodeText(c)).toLowerCase(Locale.US);
                boolean match=plus?(z.equals("+")||z.contains("افزایش")||z.contains("plus")||z.contains("add")):(z.equals("-")||z.equals("−")||z.contains("کاهش")||z.contains("minus")||z.contains("remove"));
                if(match){
                    int d=Math.abs(r.centerY()-lr.centerY())+Math.abs(r.centerX()-lr.centerX())/5;
                    if(d<bestD){bestD=d;best=c;}
                }
                if(c.isClickable())near.add(c);
            }
            if(best!=null)return best;
            android.view.accessibility.AccessibilityNodeInfo left=null,right=null;int lx=Integer.MAX_VALUE,rx=Integer.MIN_VALUE;
            for(android.view.accessibility.AccessibilityNodeInfo c:near){
                android.graphics.Rect r=new android.graphics.Rect();c.getBoundsInScreen(r);
                if(r.centerX()<lx){lx=r.centerX();left=c;}
                if(r.centerX()>rx){rx=r.centerX();right=c;}
            }
            return plus?right:left;
        }

        private boolean findAndChooseTrain(android.view.accessibility.AccessibilityNodeInfo root){
            if(cfg.getBoolean("priceMode",false)){
                int min=cfg.getInt("minPrice",0),max=cfg.getInt("maxPrice",Integer.MAX_VALUE);
                for(android.view.accessibility.AccessibilityNodeInfo n:all(root)){
                    String z=toLatinDigits(norm(nodeText(n)));
                    if(!z.contains("تومان"))continue;
                    String d=z.replaceAll("[^0-9]","");
                    if(d.length()<4)continue;
                    try{
                        long p=Long.parseLong(d);
                        if(p>=min&&p<=max){
                            android.view.accessibility.AccessibilityNodeInfo a=findActionAround(n);
                            if(a!=null)return click(a);
                        }
                    }catch(Exception ignored){}
                }
                return false;
            }
            String train=cfg.getString("train","");
            if(train.length()==0)return false;
            android.view.accessibility.AccessibilityNodeInfo n=findExact(root,train,toFa(train));
            if(n==null){
                for(android.view.accessibility.AccessibilityNodeInfo x:all(root)){
                    String z=toLatinDigits(norm(nodeText(x)));
                    if(z.equals(train)||z.contains(" "+train+" ")){n=x;break;}
                }
            }
            if(n==null)return false;
            android.view.accessibility.AccessibilityNodeInfo a=findActionAround(n);
            return a!=null&&click(a);
        }

        private android.view.accessibility.AccessibilityNodeInfo findActionAround(android.view.accessibility.AccessibilityNodeInfo n){
            android.view.accessibility.AccessibilityNodeInfo box=n;
            for(int up=0;up<8&&box!=null;up++){
                for(android.view.accessibility.AccessibilityNodeInfo x:subtree(box,180)){
                    String z=norm(nodeText(x));
                    if(z.contains("انتخاب")||z.contains("رزرو")||z.contains("خرید")){
                        android.view.accessibility.AccessibilityNodeInfo c=clickable(x);
                        if(c!=null)return c;
                    }
                }
                box=box.getParent();
            }
            return null;
        }

        private boolean isPassengerPage(android.view.accessibility.AccessibilityNodeInfo root){
            return hasText(root,"مشخصات مسافر")||hasText(root,"اطلاعات مسافر")||hasText(root,"کد ملی مسافر");
        }

        private void finishReserved(){
            cfg.edit().putBoolean("running",false).apply();
            if(!alarmDone&&cfg.getBoolean("alarm",false)){
                alarmDone=true;
                try{
                    String u=cfg.getString("alarmUri","");
                    Uri uri=u.length()>0?Uri.parse(u):android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM);
                    android.media.Ringtone r=android.media.RingtoneManager.getRingtone(this,uri);
                    if(r!=null)r.play();
                }catch(Exception ignored){}
            }
            Toast.makeText(this,"W20: بلیت پیدا شد؛ صفحه مشخصات مسافر باز شد.",Toast.LENGTH_LONG).show();
            setStage(99);
        }

        private void pullRefresh(){
            try{
                android.view.accessibility.AccessibilityNodeInfo root=getRootInActiveWindow();
                if(root==null)return;
                for(android.view.accessibility.AccessibilityNodeInfo n:all(root)){
                    if(n.isScrollable()){n.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);break;}
                }
                android.graphics.Rect r=new android.graphics.Rect();root.getBoundsInScreen(r);
                float x=r.centerX();
                float y1=Math.max(r.top+260,300);
                float y2=Math.min(r.bottom-180,y1+700);
                android.graphics.Path p=new android.graphics.Path();p.moveTo(x,y1);p.lineTo(x,y2);
                android.accessibilityservice.GestureDescription.StrokeDescription stroke=new android.accessibilityservice.GestureDescription.StrokeDescription(p,0,340);
                android.accessibilityservice.GestureDescription g=new android.accessibilityservice.GestureDescription.Builder().addStroke(stroke).build();
                dispatchGesture(g,null,null);
            }catch(Exception ignored){}
        }

        private boolean clickShortest(android.view.accessibility.AccessibilityNodeInfo root,String[] terms){
            android.view.accessibility.AccessibilityNodeInfo best=null;int len=Integer.MAX_VALUE;
            for(android.view.accessibility.AccessibilityNodeInfo n:all(root)){
                String z=norm(nodeText(n));
                for(String term:terms){
                    String q=norm(term);
                    if(z.equals(q)||z.contains(q)){
                        android.view.accessibility.AccessibilityNodeInfo c=clickable(n);
                        if(c!=null&&z.length()<len){len=z.length();best=c;}
                    }
                }
            }
            return best!=null&&click(best);
        }

        private boolean clickText(android.view.accessibility.AccessibilityNodeInfo root,String term){return clickShortest(root,new String[]{term});}

        private boolean hasText(android.view.accessibility.AccessibilityNodeInfo root,String term){return findText(root,term)!=null;}

        private android.view.accessibility.AccessibilityNodeInfo findText(android.view.accessibility.AccessibilityNodeInfo root,String... terms){
            android.view.accessibility.AccessibilityNodeInfo best=null;int len=Integer.MAX_VALUE;
            for(android.view.accessibility.AccessibilityNodeInfo n:all(root)){
                String z=norm(nodeText(n));
                for(String term:terms){
                    String q=norm(term);
                    if(z.equals(q)||(q.length()>2&&z.contains(q))){
                        if(z.length()<len){len=z.length();best=n;}
                    }
                }
            }
            return best;
        }

        private android.view.accessibility.AccessibilityNodeInfo findExact(android.view.accessibility.AccessibilityNodeInfo root,String... terms){
            for(android.view.accessibility.AccessibilityNodeInfo n:all(root)){
                String z=norm(nodeText(n));
                for(String term:terms)if(z.equals(norm(term)))return n;
            }
            return null;
        }

        private android.view.accessibility.AccessibilityNodeInfo findExactNonEditable(android.view.accessibility.AccessibilityNodeInfo root,String term){
            android.view.accessibility.AccessibilityNodeInfo best=null;int len=Integer.MAX_VALUE;
            for(android.view.accessibility.AccessibilityNodeInfo n:all(root)){
                if(isEditable(n))continue;
                String z=norm(nodeText(n));
                if(z.equals(norm(term))){
                    android.view.accessibility.AccessibilityNodeInfo c=clickable(n);
                    if(c!=null&&z.length()<len){best=n;len=z.length();}
                }
            }
            return best;
        }

        private android.view.accessibility.AccessibilityNodeInfo clickable(android.view.accessibility.AccessibilityNodeInfo n){
            android.view.accessibility.AccessibilityNodeInfo x=n;
            for(int i=0;i<7&&x!=null;i++,x=x.getParent())if(x.isClickable())return x;
            return null;
        }

        private boolean click(android.view.accessibility.AccessibilityNodeInfo n){
            android.view.accessibility.AccessibilityNodeInfo c=clickable(n);
            if(c==null)c=n;
            try{return c!=null&&c.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK);}catch(Exception e){return false;}
        }

        private java.util.List<android.view.accessibility.AccessibilityNodeInfo> all(android.view.accessibility.AccessibilityNodeInfo root){
            java.util.ArrayList<android.view.accessibility.AccessibilityNodeInfo> out=new java.util.ArrayList<>();
            if(root==null)return out;
            java.util.ArrayDeque<android.view.accessibility.AccessibilityNodeInfo> q=new java.util.ArrayDeque<>();
            q.add(root);
            while(!q.isEmpty()&&out.size()<1400){
                android.view.accessibility.AccessibilityNodeInfo n=q.removeFirst();
                out.add(n);
                int c=n.getChildCount();
                for(int i=0;i<c;i++){android.view.accessibility.AccessibilityNodeInfo ch=n.getChild(i);if(ch!=null)q.add(ch);}
            }
            return out;
        }

        private java.util.List<android.view.accessibility.AccessibilityNodeInfo> subtree(android.view.accessibility.AccessibilityNodeInfo root,int max){
            java.util.ArrayList<android.view.accessibility.AccessibilityNodeInfo> out=new java.util.ArrayList<>();
            if(root==null)return out;
            java.util.ArrayDeque<android.view.accessibility.AccessibilityNodeInfo> q=new java.util.ArrayDeque<>();q.add(root);
            while(!q.isEmpty()&&out.size()<max){
                android.view.accessibility.AccessibilityNodeInfo n=q.removeFirst();out.add(n);
                for(int i=0;i<n.getChildCount();i++){android.view.accessibility.AccessibilityNodeInfo ch=n.getChild(i);if(ch!=null)q.add(ch);}
            }
            return out;
        }

        private String nodeText(android.view.accessibility.AccessibilityNodeInfo n){
            if(n==null)return "";
            StringBuilder b=new StringBuilder();
            if(n.getText()!=null)b.append(n.getText()).append(' ');
            if(n.getContentDescription()!=null)b.append(n.getContentDescription()).append(' ');
            if(android.os.Build.VERSION.SDK_INT>=26&&n.getHintText()!=null)b.append(n.getHintText()).append(' ');
            return b.toString();
        }

        private String nodeInfo(android.view.accessibility.AccessibilityNodeInfo n){
            String z=nodeText(n);
            try{if(n.getViewIdResourceName()!=null)z+=" "+n.getViewIdResourceName();}catch(Exception ignored){}
            return norm(z).toLowerCase(Locale.US);
        }

        private String norm(String s){
            if(s==null)return "";
            return s.replace('\u200c',' ').replace('\u200e',' ').replace('\u200f',' ').replace('ي','ی').replace('ك','ک').replaceAll("\\s+"," ").trim();
        }

        private String toLatinDigits(String s){
            if(s==null)return "";
            return s.replace('۰','0').replace('۱','1').replace('۲','2').replace('۳','3').replace('۴','4').replace('۵','5').replace('۶','6').replace('۷','7').replace('۸','8').replace('۹','9')
                    .replace('٠','0').replace('١','1').replace('٢','2').replace('٣','3').replace('٤','4').replace('٥','5').replace('٦','6').replace('٧','7').replace('٨','8').replace('٩','9');
        }

        private String toFa(String s){
            StringBuilder b=new StringBuilder();
            for(int i=0;i<s.length();i++){
                char c=s.charAt(i);
                if(c>='0'&&c<='9')b.append("۰۱۲۳۴۵۶۷۸۹".charAt(c-'0'));else b.append(c);
            }
            return b.toString();
        }

        private int[] dateParts(String s){
            String[] a=toLatinDigits(s==null?"":s).replace('-','/').split("/");
            int[] p={0,0,0};
            if(a.length==3)try{p[0]=Integer.parseInt(a[0]);p[1]=Integer.parseInt(a[1]);p[2]=Integer.parseInt(a[2]);}catch(Exception ignored){}
            return p;
        }

        private String monthName(int m){
            String[] a={"","فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند"};
            return m>=1&&m<=12?a[m]:"";
        }
    }

    private class JsBridge {
        @JavascriptInterface public void log(String m){ MainActivity.this.log(m); }
        @JavascriptInterface public void sessionFailed(int token,String message){runOnUiThread(()->{if(running&&reauthRequired&&token==sessionGeneration)stopBot(message);});}
        @JavascriptInterface public void sessionReady(int token){runOnUiThread(()->{if(!running||!reauthRequired||token!=sessionGeneration)return; if(sessionDeadline!=null)handler.removeCallbacks(sessionDeadline); reauthRequired=false;logoutInProgress=false;loggedIn=false;loginInProgress=false;loginRetryCount=0; log("فرم ورود آماده است؛ اجرای ورود W6"); sessionDeadline=()->{sessionDeadline=null;if(running&&!loggedIn&&token==sessionGeneration)stopBot("ارسال فرم ورود طی ۱۰ ثانیه انجام نشد");};handler.postDelayed(sessionDeadline,10000);advanceAutomation();});}
        @JavascriptInterface public void loginMissing(){ runOnUiThread(()->{ loginInProgress=false; loginRetryCount++; if(loginRetryCount<=5){ log("ورود/عضویت پیدا نشد؛ تلاش مجدد "+loginRetryCount+" از ۵"); handler.postDelayed(()->advanceAutomation(),100); }else{ stopBot("ورود به حساب رجا انجام نشد"); } }); }
        @JavascriptInterface public void moduleFailed(int token,String message){runOnUiThread(()->{if(running&&token==sessionGeneration)stopBot(message);});}
        @JavascriptInterface public void loginSent(int token){runOnUiThread(()->{if(running&&token==sessionGeneration)log("فرم ورود ارسال شد؛ منتظر تأیید حساب...");});}
        @JavascriptInterface public void loginReady(int token){runOnUiThread(()->{if(!running||token!=sessionGeneration||loggedIn)return;if(sessionDeadline!=null){handler.removeCallbacks(sessionDeadline);sessionDeadline=null;}loginInProgress=false;loggedIn=true;log("ورود به حساب رجا تأیید شد");advanceAutomation();});}
        @JavascriptInterface public void loginClicked(){log("کلیک ورود به‌تنهایی تأیید ورود نیست");}
        @JavascriptInterface public void searchReady(int token){runOnUiThread(()->{if(!running||token!=sessionGeneration)return;formInProgress=false;searchClicked();});}
        @JavascriptInterface public void searchClicked(){ runOnUiThread(()->{ searchSubmitted=true; nextRefreshAt=SystemClock.elapsedRealtime()+Math.round(getRefreshSeconds()*1000.0); log("جستجو ارسال شد؛ ناظر سریع در حال فعال‌شدن است."); handler.postDelayed(()->advanceAutomation(),100); }); }
        @JavascriptInterface public void reserveClicked(){ reserveFast("نامشخص"); }
        @JavascriptInterface public void reserveFast(String milliseconds){ runOnUiThread(()->{ reserved=true; status.setText("● بلیت پیدا شد؛ رزرو زده شد..."); log("رزرو بلیت کلیک شد | زمان واکنش داخلی="+milliseconds+" میلی‌ثانیه"); showPanel(browserPanel); if(alarm.isChecked()) playAlarmOnce(); }); }
        @JavascriptInterface public void selectionError(String message){ runOnUiThread(()->{ status.setText("■ "+message); status.setTextColor(RED); stopBot(message); }); }
        @JavascriptInterface public void continueClicked(){ runOnUiThread(()->log("ادامه خرید کلیک شد.")); }
        @JavascriptInterface public void notFound(){ runOnUiThread(()->{ reserved=false; status.setText("● یافت نشد؛ جستجوی مجدد..."); log("بلیط مطابق معیار فعلاً یافت نشد."); }); }
    }

}
