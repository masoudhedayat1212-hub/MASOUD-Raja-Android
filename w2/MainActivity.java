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
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
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
    private String passengerType = "normal";
    private CheckBox priceMode, coupe, alarm;
    private TextView status, logView, passengerSummary;
    private LinearLayout runPanel, browserPanel;
    private Button startBtn, stopBtn, tabRun, tabBrowser;
    private boolean running = false;
    private boolean loggedIn = false;
    private boolean searchSubmitted = false;
    private boolean reserved = false;
    private boolean alarmPlayed = false;
    private boolean reauthRequired = false;
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
        ArrayAdapter<String> a=new ArrayAdapter<>(this,android.R.layout.simple_dropdown_item_1line,cities); e.setAdapter(a); e.setOnClickListener(v->e.showDropDown()); e.setOnFocusChangeListener((v,has)->{if(has)e.showDropDown();}); return e;
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
        version.setText("W6");
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
        priceMode=new CheckBox(this); priceMode.setText("جستجو با بازه قیمت"); priceMode.setTextColor(TEXT); priceMode.setButtonTintList(ColorStateList.valueOf(BLUE)); search.addView(priceMode); minPrice=field("از قیمت (ریال)"); minPrice.setInputType(InputType.TYPE_CLASS_NUMBER); maxPrice=field("تا قیمت (ریال)"); maxPrice.setInputType(InputType.TYPE_CLASS_NUMBER); search.addView(minPrice); gap(search,6); search.addView(maxPrice); gap(search,7);
        coupe=new CheckBox(this); coupe.setText("فقط کوپه دربست"); coupe.setTextColor(TEXT); coupe.setButtonTintList(ColorStateList.valueOf(BLUE)); alarm=new CheckBox(this); alarm.setText("آلارم صوتی پیدا شدن بلیت"); alarm.setTextColor(TEXT); alarm.setTypeface(null,1); alarm.setButtonTintList(ColorStateList.valueOf(BLUE)); alarm.setChecked(true); search.addView(coupe); search.addView(alarm); Button alarmTone=button("♫ انتخاب صدای آلارم",PANEL_2); alarmTone.setTextColor(TEXT); alarmTone.setOnClickListener(v->chooseAlarmTone()); search.addView(alarmTone,new LinearLayout.LayoutParams(-1,dp(46))); p.addView(search); gap(p,9);
        LinearLayout live=section("کنترل زنده"); LinearLayout rr=new LinearLayout(this); rr.setOrientation(LinearLayout.HORIZONTAL); rr.setGravity(Gravity.CENTER_VERTICAL); Button refreshIcon=button("↻",BLUE); refresh=field("رفرش"); refresh.setText("2"); refresh.setFocusable(false); refresh.setGravity(Gravity.CENTER); TextView sec=label("ثانیه"); sec.setGravity(Gravity.CENTER); View.OnClickListener openRefresh=v->showRefreshMenu(refreshIcon); refreshIcon.setOnClickListener(openRefresh); refresh.setOnClickListener(openRefresh); rr.addView(refreshIcon,new LinearLayout.LayoutParams(dp(58),dp(50))); gapH(rr,7); rr.addView(refresh,new LinearLayout.LayoutParams(dp(76),dp(50))); rr.addView(sec,new LinearLayout.LayoutParams(dp(65),dp(50))); live.addView(rr); p.addView(live); gap(p,9);
        LinearLayout account=section("حساب رجا"); phone=field("شماره موبایل حساب رجا"); phone.setInputType(InputType.TYPE_CLASS_PHONE); password=field("رمز عبور"); password.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD); account.addView(phone); gap(account,8); account.addView(password); p.addView(account); gap(p,9);
        LinearLayout actions=new LinearLayout(this); actions.setOrientation(LinearLayout.HORIZONTAL); startBtn=button("▶ شروع جستجو",GREEN); stopBtn=button("■ توقف",RED); actions.addView(stopBtn,new LinearLayout.LayoutParams(0,dp(54),1)); gapH(actions,7); actions.addView(startBtn,new LinearLayout.LayoutParams(0,dp(54),2)); p.addView(actions); gap(p,9);
        status=label("● آماده"); status.setTextColor(GREEN); status.setBackground(bg(PANEL,12,BORDER,1)); status.setPadding(dp(12),dp(10),dp(12),dp(10)); p.addView(status,new LinearLayout.LayoutParams(-1,dp(46))); gap(p,9);
        LinearLayout logs=section("لاگ زنده رجا / ربات"); Button copy=button("کپی لاگ",PANEL_2); logs.addView(copy,new LinearLayout.LayoutParams(-1,dp(44))); gap(logs,6); logView=new TextView(this); logView.setTextColor(TEXT); logView.setTextSize(12); logView.setGravity(Gravity.RIGHT); logView.setTextIsSelectable(true); logView.setBackground(bg(FIELD,12,BORDER,1)); logView.setPadding(dp(10),dp(10),dp(10),dp(10)); logView.setMinHeight(dp(170)); logs.addView(logView); p.addView(logs);
        copy.setOnClickListener(v->{ ClipboardManager cm=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE); cm.setPrimaryClip(ClipData.newPlainText("MASOUD Raja Log",logView.getText())); toast("لاگ کپی شد"); });
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
    private void updateActionButtons(){ if(startBtn==null||stopBtn==null)return; int paleGreen=Color.rgb(157,225,195), paleRed=Color.rgb(255,170,178); startBtn.setText(running?"✓ در حال جستجو":"▶ شروع جستجو"); stopBtn.setText(actionState==2?"■ متوقف":"■ توقف"); startBtn.setBackground(bg(actionState==1?Color.rgb(0,165,95):paleGreen,14,actionState==1?Color.rgb(0,165,95):paleGreen,0)); stopBtn.setBackground(bg(actionState==2?Color.rgb(225,45,62):paleRed,14,actionState==2?Color.rgb(225,45,62):paleRed,0)); startBtn.setTextColor(actionState==1?Color.WHITE:TEXT); stopBtn.setTextColor(actionState==2?Color.WHITE:TEXT); startBtn.setEnabled(true); stopBtn.setEnabled(true); }

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
        int offset=(first.get(java.util.Calendar.DAY_OF_WEEK)+6)%7;
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
        webView.addJavascriptInterface(new JsBridge(),"MasoudBridge"); webView.setWebChromeClient(new WebChromeClient()); webView.setWebViewClient(new WebViewClient(){ @Override public void onPageFinished(WebView v,String url){ log("صفحه باز شد: "+url); if(running) handler.postDelayed(()->advanceAutomation(),500); } });
        webView.loadUrl("https://www.raja.ir/");
    }


    private void startBot(){
        if(origin.getText().toString().trim().isEmpty()||destination.getText().toString().trim().isEmpty()||travelDate.getText().toString().trim().isEmpty()){ toast("مبدا، مقصد و تاریخ را کامل کن."); return; }
        if(phone.getText().toString().trim().isEmpty()||password.getText().toString().trim().isEmpty()){ toast("حساب رجا را وارد کن."); return; }
        if(!priceMode.isChecked() && trainNumber.getText().toString().trim().isEmpty()){ toast("شماره قطار را وارد کن یا حالت بازه قیمت را فعال کن."); return; }
        saveAccount(); running=true; actionState=1; reauthRequired=true; loggedIn=false; searchSubmitted=false; reserved=false; alarmPlayed=false; updateActionButtons(); status.setText("● فرمان شروع ثبت شد؛ در حال اجرا..."); status.setTextColor(Color.rgb(0,130,75)); logView.setText(""); log("شروع اجرا | "+origin.getText()+" ← "+destination.getText()+" | "+travelDate.getText());
        if(!webView.getUrl().startsWith("https://www.raja.ir")) webView.loadUrl("https://www.raja.ir/"); else webView.loadUrl("https://www.raja.ir/"); startMonitorLoop();
    }
    private void stopBot(String why){ running=false; actionState=2; if(monitorRunnable!=null) handler.removeCallbacks(monitorRunnable); updateActionButtons(); status.setText("■ "+why); status.setTextColor(RED); log(why); }

    private void startMonitorLoop(){ monitorRunnable=new Runnable(){ @Override public void run(){ if(!running) return; if(searchSubmitted&&!reserved){ long now=System.currentTimeMillis(); if(now>=nextRefreshAt){ log("رفرش طبق زمان زنده: "+getRefreshSeconds()+" ثانیه"); webView.reload(); nextRefreshAt=now+(long)(getRefreshSeconds()*1000); } } handler.postDelayed(this,150); } }; handler.post(monitorRunnable); }
    private double getRefreshSeconds(){ try{return Math.max(0.5,Math.min(60,Double.parseDouble(refresh.getText().toString().trim())));}catch(Exception e){return 2.0;} }

    private void advanceAutomation(){
        if(!running) return; String url=webView.getUrl()==null?"":webView.getUrl();
        if(reauthRequired){ injectLogout(); return; }
        if(url.contains("registerticket")){ reserved=true; status.setText("● وارد صفحه مشخصات مسافر شد."); log("رزرو و ادامه خرید انجام شد؛ صفحه مشخصات مسافر باز شد."); showPanel(browserPanel); if(alarm.isChecked()) playAlarmOnce(); return; }
        if(!loggedIn){ injectLogin(); return; }
        if(!searchSubmitted){ injectSearchForm(); return; }
        inspectResultsAndReserve();
    }

    private void injectLogout(){
        log("خروج از نشست قبلی رجا...");
        String js="(function(){try{const t=s=>(s||'').trim();let all=[...document.querySelectorAll('button,a,span,div')].filter(x=>x.offsetParent!==null);let account=all.find(x=>t(x.innerText).includes('حساب کاربری')||t(x.innerText).includes('پروفایل'));if(account)account.click();setTimeout(()=>{let els=[...document.querySelectorAll('button,a,span,div')].filter(x=>x.offsetParent!==null);let out=els.find(x=>t(x.innerText)==='خروج')||els.find(x=>t(x.innerText).includes('خروج از حساب'));if(out)out.click();setTimeout(()=>MasoudBridge.logoutDone(),700);},450);}catch(e){MasoudBridge.log('خطای خروج: '+e);MasoudBridge.logoutDone();}})();";
        webView.evaluateJavascript(js,null);
    }

    private void injectLogin(){
        String js="(function(){try{const txt=s=>(s||'').trim();const els=[...document.querySelectorAll('button,a,span,div')];let op=els.find(e=>txt(e.innerText)==='ورود / عضویت')||els.find(e=>txt(e.innerText).includes('ورود / عضویت'));if(op){op.click();setTimeout(()=>{let ins=[...document.querySelectorAll('input')].filter(x=>x.offsetParent!==null);if(ins.length>=2){ins[0].focus();ins[0].value="+q(phone.getText().toString())+";ins[0].dispatchEvent(new Event('input',{bubbles:true}));ins[1].focus();ins[1].value="+q(password.getText().toString())+";ins[1].dispatchEvent(new Event('input',{bubbles:true}));let bs=[...document.querySelectorAll('button')].filter(x=>x.offsetParent!==null);let b=bs.find(x=>txt(x.innerText)==='ورود')||bs.find(x=>txt(x.innerText).includes('ورود'));if(b){b.click();MasoudBridge.loginClicked();}else MasoudBridge.log('دکمه ورود پیدا نشد');}else MasoudBridge.log('فیلدهای ورود پیدا نشد');},700);return 'opening';}MasoudBridge.log('ورود/عضویت پیدا نشد');return 'missing';}catch(e){MasoudBridge.log('خطای ورود: '+e);return 'error';}})();";
        webView.evaluateJavascript(js,null);
    }

    private void injectSearchForm(){
        int total=parseInt(adults.getText().toString(),1)+parseInt(children.getText().toString(),0);
        String js="(async function(){const sleep=ms=>new Promise(r=>setTimeout(r,ms));const fire=(el)=>{el.dispatchEvent(new Event('input',{bubbles:true}));el.dispatchEvent(new Event('change',{bubbles:true}));};try{let change=[...document.querySelectorAll('button,div,a')].find(x=>(x.innerText||'').trim()==='تغییر جستجو');if(change){change.click();await sleep(500);}async function station(kind,city){let comps=[...document.querySelectorAll('app-stations')];let c=comps.find(x=>((x.getAttribute('name')||'').toLowerCase()).includes(kind==='from'?'from':'to'));if(!c)return false;let clear=c.querySelector('.ng-clear-wrapper');if(clear)clear.click();let inp=c.querySelector('input[role=combobox]');if(!inp)return false;inp.focus();inp.value=city;fire(inp);await sleep(650);let opts=[...document.querySelectorAll('ng-dropdown-panel .ng-option')];let o=opts.find(x=>(x.innerText||'').trim()===city)||opts.find(x=>(x.innerText||'').includes(city));if(o){o.click();await sleep(250);return true;}return false;}let a=await station('from',"+q(origin.getText().toString().trim())+");let b=await station('to',"+q(destination.getText().toString().trim())+");if(!a||!b){MasoudBridge.log('مبدا یا مقصد تنظیم نشد');return;}let dateText="+q(travelDate.getText().toString().trim())+";let parts=dateText.replace(/-/g,'/').split('/').map(Number);let picker=document.querySelector('app-datepicker-single[name=oneWayDatePicker]');if(!picker||parts.length!==3){MasoudBridge.log('تقویم رجا پیدا نشد');return;}let cal=picker.querySelector('button.icon-calendar')||picker.querySelector('input[name=dp]');if(cal)cal.click();await sleep(650);let jy=parts[0],jm=parts[1],jd=parts[2];let monthNames=['','فروردین','اردیبهشت','خرداد','تیر','مرداد','شهریور','مهر','آبان','آذر','دی','بهمن','اسفند'];for(let sel of [...document.querySelectorAll('select')].filter(x=>x.offsetParent!==null)){let opts=[...sel.options];let yo=opts.find(o=>(o.textContent||'').includes(String(jy)));if(yo){sel.value=yo.value;fire(sel);await sleep(220);continue;}let mo=opts.find(o=>(o.textContent||'').includes(monthNames[jm])||(o.textContent||'').trim()===String(jm));if(mo){sel.value=mo.value;fire(sel);await sleep(220);}}let dayFa=String(jd).replace(/[0-9]/g,d=>'۰۱۲۳۴۵۶۷۸۹'[Number(d)]);let nodes=[...document.querySelectorAll('button,td,span,div')].filter(x=>x.offsetParent!==null);let day=nodes.find(x=>{let t=(x.innerText||'').trim();let c=(x.className||'').toString().toLowerCase();return (t===String(jd)||t===dayFa)&&!c.includes('disabled')&&!c.includes('muted')&&!c.includes('outside');});if(!day){MasoudBridge.log('روز تاریخ در تقویم پیدا نشد');return;}day.click();await sleep(650);let dateInp=picker.querySelector('input[name=dp]');if(!dateInp||!(dateInp.value||'').trim()){MasoudBridge.log('تاریخ در رجا ثبت نشد');return;}let pb=document.querySelector('#dropdownPassenger');if(pb){pb.click();await sleep(350);let target="+total+";let options=[...document.querySelectorAll('.dropdown-menu *,[role=option]')].filter(x=>x.offsetParent!==null);let exact=options.find(x=>{let t=(x.innerText||'').trim();let n=(t.match(/[0-9۰-۹]+/)||[])[0]||'';n=n.replace(/[۰-۹]/g,d=>'۰۱۲۳۴۵۶۷۸۹'.indexOf(d));return n===String(target);});if(exact){exact.click();MasoudBridge.log('تعداد مسافر در رجا انتخاب شد: '+target);}else MasoudBridge.log('گزینه تعداد مسافر در رجا پیدا نشد');}await sleep(350);let pt="+q(passengerType)+";let typeLabel=pt==='men'?'ویژه برادران':pt==='women'?'ویژه خواهران':'مسافران عادی';let visible=[...document.querySelectorAll('button,div,span,input')].filter(x=>x.offsetParent!==null);let typeControl=visible.find(x=>{let t=(x.innerText||x.value||'').trim();return t==='مسافران عادی'||t==='ویژه برادران'||t==='ویژه خواهران';});if(typeControl){typeControl.click();await sleep(300);let choices=[...document.querySelectorAll('button,li,div,span,[role=option]')].filter(x=>x.offsetParent!==null);let choice=choices.find(x=>(x.innerText||'').trim()===typeLabel);if(choice){choice.click();MasoudBridge.log('نوع مسافر در رجا انتخاب شد: '+typeLabel);}else MasoudBridge.log('نوع مسافر در رجا پیدا نشد');}else MasoudBridge.log('کادر نوع مسافر در رجا پیدا نشد');await sleep(300);"+(coupe.isChecked()?"let cc=[...document.querySelectorAll('input[type=checkbox]')].find(x=>((x.parentElement?.innerText)||'').includes('کوپه دربست'));if(cc&&!cc.checked)cc.click();":"")+"let bs=[...document.querySelectorAll('button')].filter(x=>x.offsetParent!==null);let s=bs.find(x=>(x.innerText||'').trim().includes('جستجو'));if(s){s.click();MasoudBridge.searchClicked();}else MasoudBridge.log('دکمه جستجو پیدا نشد');}catch(e){MasoudBridge.log('خطای تنظیم جستجو: '+e);}})();";
        webView.evaluateJavascript(js,null);
    }

    private void inspectResultsAndReserve(){
        String min=digitsOnly(minPrice.getText().toString()), max=digitsOnly(maxPrice.getText().toString()), wanted=digitsOnly(trainNumber.getText().toString());
        String js="(function(){try{const ds=s=>String(s||'').replace(/[^0-9۰-۹٠-٩]/g,'').replace(/[۰-۹]/g,d=>'۰۱۲۳۴۵۶۷۸۹'.indexOf(d)).replace(/[٠-٩]/g,d=>'٠١٢٣٤٥٦٧٨٩'.indexOf(d));let buttons=[...document.querySelectorAll('button')].filter(b=>b.offsetParent!==null&&(b.innerText||'').includes('رزرو بلیت'));MasoudBridge.log('پاسخ رجا: '+buttons.length+' دکمه رزرو دیده شد');for(let b of buttons){let card=b.closest('div.train-result')||b.closest('.train-result');if(!card)continue;let priceEl=card.querySelector('span.price');let price=priceEl?Number(ds(priceEl.textContent)):0;let timeline=card.querySelector('app-timeline');let tn='';if(timeline){tn=ds(timeline.getAttribute('data-trainnumber-to')||timeline.getAttribute('data-trainnumber-from')||'');}if(!tn){let m=(card.innerText||'').match(/شماره\\s*قطار\\s*[:：]?\\s*([0-9۰-۹٠-٩]+)/);if(m)tn=ds(m[1]);}MasoudBridge.log('کارت: قطار='+tn+' | قیمت='+price);let match="+(priceMode.isChecked()?"(("+(min.isEmpty()?"true":"price>=Number('"+min+"')")+")&&("+(max.isEmpty()?"true":"price<=Number('"+max+"')")+"))":"tn==='"+wanted+"'")+";if(match){b.click();MasoudBridge.reserveClicked();setTimeout(()=>{let c=[...document.querySelectorAll('button')].find(x=>(x.innerText||'').trim()==='ادامه خرید');if(c){c.click();MasoudBridge.continueClicked();}else MasoudBridge.log('رزرو زده شد ولی ادامه خرید پیدا نشد');},900);return 'reserved';}}MasoudBridge.notFound();return 'none';}catch(e){MasoudBridge.log('خطای بررسی نتیجه: '+e);return 'error';}})();";
        webView.evaluateJavascript(js,null);
    }

    private int parseInt(String s,int def){ try{return Math.max(0,Integer.parseInt(s.trim()));}catch(Exception e){return def;} }
    private String digitsOnly(String s){ return s==null?"":s.replaceAll("[^0-9]",""); }
    private String q(String s){ return JSONObject.quote(s==null?"":s); }
    private void log(String m){ runOnUiThread(()->{ String old=logView.getText().toString(); if(old.length()>20000) old=old.substring(old.length()-15000); logView.setText(old+"["+new java.text.SimpleDateFormat("HH:mm:ss",Locale.US).format(new java.util.Date())+"] "+m+"\n"); }); }
    private void toast(String m){ Toast.makeText(this,m,Toast.LENGTH_SHORT).show(); }
    private void saveAccount(){ getPreferences(MODE_PRIVATE).edit().putString("phone",phone.getText().toString()).putString("password",password.getText().toString()).apply(); }
    private void loadAccount(){ phone.setText(getPreferences(MODE_PRIVATE).getString("phone","")); password.setText(getPreferences(MODE_PRIVATE).getString("password","")); String u=getPreferences(MODE_PRIVATE).getString("alarm_uri",""); if(!u.isEmpty())selectedAlarmUri=Uri.parse(u); }
    private void chooseAlarmTone(){ Intent i=new Intent(RingtoneManager.ACTION_RINGTONE_PICKER); i.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_ALARM|RingtoneManager.TYPE_NOTIFICATION); i.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,"انتخاب صدای آلارم ربات"); i.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,selectedAlarmUri); startActivityForResult(i,RINGTONE_REQUEST); }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){ super.onActivityResult(requestCode,resultCode,data); if(requestCode==RINGTONE_REQUEST&&resultCode==RESULT_OK&&data!=null){ selectedAlarmUri=data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI); getPreferences(MODE_PRIVATE).edit().putString("alarm_uri",selectedAlarmUri==null?"":selectedAlarmUri.toString()).apply(); toast("صدای آلارم انتخاب شد"); } }

    private void playAlarmOnce(){ if(alarmPlayed) return; alarmPlayed=true; try{ Uri uri=selectedAlarmUri!=null?selectedAlarmUri:RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM); if(uri==null) uri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); Ringtone r=RingtoneManager.getRingtone(this,uri); if(r!=null) r.play(); }catch(Exception e){ toast("بلیت پیدا شد و رزرو انجام شد"); } }

    private class JsBridge {
        @JavascriptInterface public void log(String m){ MainActivity.this.log(m); }
        @JavascriptInterface public void logoutDone(){ runOnUiThread(()->{ reauthRequired=false; loggedIn=false; log("خروج انجام شد؛ ورود مجدد..."); webView.loadUrl("https://www.raja.ir/"); }); }
        @JavascriptInterface public void loginClicked(){ runOnUiThread(()->{ loggedIn=true; log("دکمه ورود زده شد؛ منتظر تکمیل ورود..."); handler.postDelayed(()->advanceAutomation(),2600); }); }
        @JavascriptInterface public void searchClicked(){ runOnUiThread(()->{ searchSubmitted=true; nextRefreshAt=System.currentTimeMillis()+(long)(getRefreshSeconds()*1000); log("جستجو ارسال شد؛ پایش مداوم فعال است."); handler.postDelayed(()->advanceAutomation(),1300); }); }
        @JavascriptInterface public void reserveClicked(){ runOnUiThread(()->{ reserved=true; status.setText("● بلیت پیدا شد؛ رزرو زده شد..."); log("رزرو بلیت کلیک شد."); showPanel(browserPanel); if(alarm.isChecked()) playAlarmOnce(); }); }
        @JavascriptInterface public void continueClicked(){ runOnUiThread(()->log("ادامه خرید کلیک شد.")); }
        @JavascriptInterface public void notFound(){ runOnUiThread(()->{ reserved=false; status.setText("● یافت نشد؛ جستجوی مجدد..."); log("بلیط مطابق معیار فعلاً یافت نشد."); }); }
    }

}
