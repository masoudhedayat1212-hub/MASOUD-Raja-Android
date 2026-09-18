package com.masoud.raja;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Ringtone;
import android.app.RingtoneManager;
import android.content.ClipData;
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

    private final Handler handler = new Handler(Looper.getMainLooper());
    private WebView webView;
    private EditText phone, password, origin, destination, travelDate, trainNumber, minPrice, maxPrice, adults, children, refresh;
    private CheckBox priceMode, coupe, alarm;
    private TextView status, logView;
    private LinearLayout runPanel, browserPanel;
    private Button startBtn, stopBtn, tabRun, tabBrowser;
    private boolean running = false;
    private boolean loggedIn = false;
    private boolean searchSubmitted = false;
    private boolean reserved = false;
    private boolean alarmPlayed = false;
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
        version.setText("W2");
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
        LinearLayout r1=new LinearLayout(this); r1.setOrientation(LinearLayout.HORIZONTAL); r1.setGravity(Gravity.CENTER_VERTICAL); TextView fromIcon=label("●"); fromIcon.setTextColor(CYAN); origin=stationField("مبدا — تایپ یا انتخاب"); r1.addView(fromIcon,new LinearLayout.LayoutParams(dp(34),dp(50))); r1.addView(origin,new LinearLayout.LayoutParams(0,dp(50),1)); route.addView(r1);
        Button swap=button("⇅",PANEL_2); swap.setTextColor(TEXT); swap.setTextSize(23); LinearLayout swapRow=new LinearLayout(this); swapRow.setGravity(Gravity.CENTER); swapRow.addView(swap,new LinearLayout.LayoutParams(dp(58),dp(42))); route.addView(swapRow,new LinearLayout.LayoutParams(-1,dp(46)));
        swap.setOnClickListener(v->{ String a=origin.getText().toString(); origin.setText(destination.getText().toString()); destination.setText(a); toast("مبدا و مقصد جابه‌جا شد"); });
        LinearLayout r2=new LinearLayout(this); r2.setOrientation(LinearLayout.HORIZONTAL); r2.setGravity(Gravity.CENTER_VERTICAL); TextView toIcon=label("●"); toIcon.setTextColor(GREEN); destination=stationField("مقصد — تایپ یا انتخاب"); r2.addView(toIcon,new LinearLayout.LayoutParams(dp(34),dp(50))); r2.addView(destination,new LinearLayout.LayoutParams(0,dp(50),1)); route.addView(r2); gap(route,8);
        LinearLayout drow=new LinearLayout(this); drow.setOrientation(LinearLayout.HORIZONTAL); drow.setGravity(Gravity.CENTER_VERTICAL); Button cal=button("▣",PANEL_2); travelDate=field("تاریخ سفر"); travelDate.setFocusable(false); travelDate.setOnClickListener(v->showPersianCalendar()); cal.setOnClickListener(v->showPersianCalendar()); drow.addView(cal,new LinearLayout.LayoutParams(dp(54),dp(50))); gapH(drow,6); drow.addView(travelDate,new LinearLayout.LayoutParams(0,dp(50),1)); route.addView(drow); p.addView(route); gap(p,9);
        LinearLayout passengers=section("تعداد مسافر"); passengers.addView(makePassengerStepper("بزرگسال",true)); gap(passengers,8); passengers.addView(makePassengerStepper("کودک",false)); p.addView(passengers); gap(p,9);
        LinearLayout search=section("تنظیمات جستجو"); trainNumber=field("شماره قطار"); trainNumber.setInputType(InputType.TYPE_CLASS_NUMBER); search.addView(trainNumber); gap(search,7);
        priceMode=new CheckBox(this); priceMode.setText("جستجو با بازه قیمت"); priceMode.setTextColor(TEXT); priceMode.setButtonTintList(ColorStateList.valueOf(BLUE)); search.addView(priceMode); minPrice=field("از قیمت (ریال)"); minPrice.setInputType(InputType.TYPE_CLASS_NUMBER); maxPrice=field("تا قیمت (ریال)"); maxPrice.setInputType(InputType.TYPE_CLASS_NUMBER); search.addView(minPrice); gap(search,6); search.addView(maxPrice); gap(search,7);
        coupe=new CheckBox(this); coupe.setText("فقط کوپه دربست"); coupe.setTextColor(TEXT); coupe.setButtonTintList(ColorStateList.valueOf(BLUE)); alarm=new CheckBox(this); alarm.setText("آلارم صوتی پیدا شدن بلیت"); alarm.setTextColor(TEXT); alarm.setTypeface(null,1); alarm.setButtonTintList(ColorStateList.valueOf(BLUE)); alarm.setChecked(true); search.addView(coupe); search.addView(alarm); p.addView(search); gap(p,9);
        LinearLayout live=section("کنترل زنده"); LinearLayout rr=new LinearLayout(this); rr.setOrientation(LinearLayout.HORIZONTAL); rr.setGravity(Gravity.CENTER_VERTICAL); Button refreshIcon=button("↻",BLUE); refresh=field("رفرش"); refresh.setText("2"); refresh.setFocusable(false); refresh.setGravity(Gravity.CENTER); TextView sec=label("ثانیه"); sec.setGravity(Gravity.CENTER); View.OnClickListener openRefresh=v->showRefreshMenu(refreshIcon); refreshIcon.setOnClickListener(openRefresh); refresh.setOnClickListener(openRefresh); rr.addView(refreshIcon,new LinearLayout.LayoutParams(dp(58),dp(50))); gapH(rr,7); rr.addView(refresh,new LinearLayout.LayoutParams(dp(76),dp(50))); rr.addView(sec,new LinearLayout.LayoutParams(dp(65),dp(50))); live.addView(rr); p.addView(live); gap(p,9);
        LinearLayout account=section("حساب رجا"); phone=field("شماره موبایل حساب رجا"); phone.setInputType(InputType.TYPE_CLASS_PHONE); password=field("رمز عبور"); password.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD); account.addView(phone); gap(account,8); account.addView(password); p.addView(account); gap(p,9);
        LinearLayout actions=new LinearLayout(this); actions.setOrientation(LinearLayout.HORIZONTAL); startBtn=button("▶ شروع جستجو",GREEN); stopBtn=button("■ توقف",RED); stopBtn.setEnabled(false); actions.addView(stopBtn,new LinearLayout.LayoutParams(0,dp(54),1)); gapH(actions,7); actions.addView(startBtn,new LinearLayout.LayoutParams(0,dp(54),2)); p.addView(actions); gap(p,9);
        status=label("● آماده"); status.setTextColor(GREEN); status.setBackground(bg(PANEL,12,BORDER,1)); status.setPadding(dp(12),dp(10),dp(12),dp(10)); p.addView(status,new LinearLayout.LayoutParams(-1,dp(46))); gap(p,9);
        LinearLayout logs=section("لاگ زنده رجا / ربات"); Button copy=button("کپی لاگ",PANEL_2); logs.addView(copy,new LinearLayout.LayoutParams(-1,dp(44))); gap(logs,6); logView=new TextView(this); logView.setTextColor(TEXT); logView.setTextSize(12); logView.setGravity(Gravity.RIGHT); logView.setTextIsSelectable(true); logView.setBackground(bg(FIELD,12,BORDER,1)); logView.setPadding(dp(10),dp(10),dp(10),dp(10)); logView.setMinHeight(dp(170)); logs.addView(logView); p.addView(logs);
        copy.setOnClickListener(v->{ ClipboardManager cm=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE); cm.setPrimaryClip(ClipData.newPlainText("MASOUD Raja Log",logView.getText())); toast("لاگ کپی شد"); });
        startBtn.setOnClickListener(v->pressAction(startBtn,()->{ if(running){ toast("جستجو در حال اجراست"); return; } startBot(); })); stopBtn.setOnClickListener(v->pressAction(stopBtn,()->{ if(!running){ toast("ربات متوقف است"); return; } stopBot("توقف توسط کاربر"); })); priceMode.setOnCheckedChangeListener((b,checked)->{ trainNumber.setEnabled(!checked); minPrice.setEnabled(checked); maxPrice.setEnabled(checked); }); minPrice.setEnabled(false); maxPrice.setEnabled(false);
        updateActionButtons();
        LinearLayout container=new LinearLayout(this); container.setOrientation(LinearLayout.VERTICAL); container.addView(scroll,new LinearLayout.LayoutParams(-1,-1)); return container;
    }

    private LinearLayout makePassengerStepper(String text,boolean adult){
        LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR); row.setBackground(bg(FIELD,14,BORDER,1)); row.setPadding(dp(10),dp(5),dp(10),dp(5));
        TextView l=label(text); l.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL); l.setTextSize(16); l.setTypeface(null,1); row.addView(l,new LinearLayout.LayoutParams(0,dp(48),1));
        Button minus=button("−",PANEL_2); minus.setTextColor(TEXT); minus.setTextSize(22); minus.setTypeface(null,1); Button plus=button("+",BLUE); EditText val=field(""); val.setGravity(Gravity.CENTER); val.setInputType(InputType.TYPE_CLASS_NUMBER); val.setText(adult?"1":"0"); val.setFocusable(false); if(adult) adults=val; else children=val;
        minus.setOnClickListener(v->{ int n=parseInt(val.getText().toString(),adult?1:0); int min=adult?1:0; val.setText(String.valueOf(Math.max(min,n-1))); }); plus.setOnClickListener(v->{ int n=parseInt(val.getText().toString(),adult?1:0); val.setText(String.valueOf(Math.min(9,n+1))); });
        row.addView(minus,new LinearLayout.LayoutParams(dp(50),dp(44))); gapH(row,5); row.addView(val,new LinearLayout.LayoutParams(dp(56),dp(44))); gapH(row,5); row.addView(plus,new LinearLayout.LayoutParams(dp(50),dp(44))); return row;
    }

    private void pressAction(Button b,Runnable action){ b.setAlpha(0.55f); b.animate().scaleX(0.96f).scaleY(0.96f).setDuration(70).withEndAction(()->{ b.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(90).withEndAction(action).start(); }).start(); }
    private void updateActionButtons(){ if(startBtn==null||stopBtn==null)return; if(running){ startBtn.setText("✓ در حال جستجو"); startBtn.setBackground(bg(Color.rgb(0,145,85),14,Color.rgb(0,145,85),0)); stopBtn.setText("■ توقف"); stopBtn.setBackground(bg(RED,14,RED,0)); }else{ startBtn.setText("▶ شروع جستجو"); startBtn.setBackground(bg(GREEN,14,GREEN,0)); stopBtn.setText("■ متوقف"); stopBtn.setBackground(bg(Color.rgb(190,45,60),14,Color.rgb(190,45,60),0)); } startBtn.setTextColor(Color.WHITE); stopBtn.setTextColor(Color.WHITE); }

    private void showRefreshMenu(View anchor){ PopupMenu m=new PopupMenu(this,anchor); for(int i=1;i<=10;i++) m.getMenu().add(String.valueOf(i)); m.setOnMenuItemClickListener(item->{ refresh.setText(item.getTitle().toString()); return true; }); m.show(); }
    private void showPersianCalendar(){
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(14),dp(8),dp(14),dp(8)); LinearLayout top=new LinearLayout(this); top.setOrientation(LinearLayout.HORIZONTAL); top.setGravity(Gravity.CENTER);
        Spinner year=new Spinner(this), month=new Spinner(this); List<String> ys=new ArrayList<>(), ms=new ArrayList<>(); for(int y=1404;y<=1412;y++)ys.add(String.valueOf(y)); String[] mn={"فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند"}; for(String x:mn)ms.add(x); year.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,ys)); month.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,ms)); top.addView(year,new LinearLayout.LayoutParams(0,dp(52),1)); top.addView(month,new LinearLayout.LayoutParams(0,dp(52),1)); box.addView(top);
        GridLayout days=new GridLayout(this); days.setColumnCount(7); days.setAlignmentMode(GridLayout.ALIGN_BOUNDS); days.setUseDefaultMargins(false); final int[] selected={1};
        for(int i=1;i<=31;i++){ Button d=button(String.valueOf(i),PANEL_2); d.setTextColor(TEXT); d.setTextSize(14); d.setMinWidth(0); d.setMinimumWidth(0); d.setMinHeight(0); d.setMinimumHeight(0); d.setPadding(0,0,0,0); final int day=i; d.setOnClickListener(v->{ selected[0]=day; for(int j=0;j<days.getChildCount();j++){ View cv=days.getChildAt(j); cv.setBackground(bg(PANEL_2,8,BORDER,1)); if(cv instanceof Button)((Button)cv).setTextColor(TEXT); } v.setBackground(bg(BLUE,8,BLUE,0)); ((Button)v).setTextColor(Color.WHITE); }); GridLayout.LayoutParams lp=new GridLayout.LayoutParams(); lp.width=0; lp.height=dp(42); lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); lp.setMargins(dp(1),dp(1),dp(1),dp(1)); days.addView(d,lp); } box.addView(days,new LinearLayout.LayoutParams(-1,dp(220)));
        String old=travelDate.getText().toString(); try{ String[] p=old.split("/"); year.setSelection(Math.max(0,Integer.parseInt(p[0])-1404)); month.setSelection(Math.max(0,Integer.parseInt(p[1])-1)); selected[0]=Integer.parseInt(p[2]); }catch(Exception ignored){}
        new AlertDialog.Builder(this).setTitle("انتخاب تاریخ شمسی").setView(box).setNegativeButton("لغو",null).setPositiveButton("تأیید",(d,w)-> travelDate.setText(year.getSelectedItem()+"/"+String.format(Locale.US,"%02d",month.getSelectedItemPosition()+1)+"/"+String.format(Locale.US,"%02d",selected[0]))).show();
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
        saveAccount(); running=true; loggedIn=false; searchSubmitted=false; reserved=false; alarmPlayed=false; updateActionButtons(); status.setText("● فرمان شروع ثبت شد؛ در حال اجرا..."); status.setTextColor(Color.rgb(0,130,75)); logView.setText(""); log("شروع اجرا | "+origin.getText()+" ← "+destination.getText()+" | "+travelDate.getText());
        if(!webView.getUrl().startsWith("https://www.raja.ir")) webView.loadUrl("https://www.raja.ir/"); else webView.loadUrl("https://www.raja.ir/"); startMonitorLoop();
    }
    private void stopBot(String why){ running=false; if(monitorRunnable!=null) handler.removeCallbacks(monitorRunnable); updateActionButtons(); status.setText("■ "+why); status.setTextColor(RED); log(why); }

    private void startMonitorLoop(){ monitorRunnable=new Runnable(){ @Override public void run(){ if(!running) return; if(searchSubmitted&&!reserved){ long now=System.currentTimeMillis(); if(now>=nextRefreshAt){ log("رفرش طبق زمان زنده: "+getRefreshSeconds()+" ثانیه"); webView.reload(); nextRefreshAt=now+(long)(getRefreshSeconds()*1000); } } handler.postDelayed(this,150); } }; handler.post(monitorRunnable); }
    private double getRefreshSeconds(){ try{return Math.max(0.5,Math.min(60,Double.parseDouble(refresh.getText().toString().trim())));}catch(Exception e){return 2.0;} }

    private void advanceAutomation(){
        if(!running) return; String url=webView.getUrl()==null?"":webView.getUrl();
        if(url.contains("registerticket")){ reserved=true; status.setText("● وارد صفحه مشخصات مسافر شد."); log("رزرو و ادامه خرید انجام شد؛ صفحه مشخصات مسافر باز شد."); showPanel(browserPanel); if(alarm.isChecked()) playAlarmOnce(); return; }
        if(!loggedIn){ injectLogin(); return; }
        if(!searchSubmitted){ injectSearchForm(); return; }
        inspectResultsAndReserve();
    }

    private void injectLogin(){
        String js="(function(){try{const txt=s=>(s||'').trim();const els=[...document.querySelectorAll('button,a,span,div')];let op=els.find(e=>txt(e.innerText)==='ورود / عضویت')||els.find(e=>txt(e.innerText).includes('ورود / عضویت'));if(op){op.click();setTimeout(()=>{let ins=[...document.querySelectorAll('input')].filter(x=>x.offsetParent!==null);if(ins.length>=2){ins[0].focus();ins[0].value="+q(phone.getText().toString())+";ins[0].dispatchEvent(new Event('input',{bubbles:true}));ins[1].focus();ins[1].value="+q(password.getText().toString())+";ins[1].dispatchEvent(new Event('input',{bubbles:true}));let bs=[...document.querySelectorAll('button')].filter(x=>x.offsetParent!==null);let b=bs.find(x=>txt(x.innerText)==='ورود')||bs.find(x=>txt(x.innerText).includes('ورود'));if(b){b.click();MasoudBridge.loginClicked();}else MasoudBridge.log('دکمه ورود پیدا نشد');}else MasoudBridge.log('فیلدهای ورود پیدا نشد');},700);return 'opening';}MasoudBridge.log('ورود/عضویت پیدا نشد');return 'missing';}catch(e){MasoudBridge.log('خطای ورود: '+e);return 'error';}})();";
        webView.evaluateJavascript(js,null);
    }

    private void injectSearchForm(){
        int total=parseInt(adults.getText().toString(),1)+parseInt(children.getText().toString(),0);
        String js="(async function(){const sleep=ms=>new Promise(r=>setTimeout(r,ms));const fire=(el)=>{el.dispatchEvent(new Event('input',{bubbles:true}));el.dispatchEvent(new Event('change',{bubbles:true}));};try{let change=[...document.querySelectorAll('button,div,a')].find(x=>(x.innerText||'').trim()==='تغییر جستجو');if(change){change.click();await sleep(500);}async function station(kind,city){let comps=[...document.querySelectorAll('app-stations')];let c=comps.find(x=>((x.getAttribute('name')||'').toLowerCase()).includes(kind==='from'?'from':'to'));if(!c)return false;let clear=c.querySelector('.ng-clear-wrapper');if(clear)clear.click();let inp=c.querySelector('input[role=combobox]');if(!inp)return false;inp.focus();inp.value=city;fire(inp);await sleep(650);let opts=[...document.querySelectorAll('ng-dropdown-panel .ng-option')];let o=opts.find(x=>(x.innerText||'').trim()===city)||opts.find(x=>(x.innerText||'').includes(city));if(o){o.click();await sleep(250);return true;}return false;}let a=await station('from',"+q(origin.getText().toString().trim())+");let b=await station('to',"+q(destination.getText().toString().trim())+");if(!a||!b){MasoudBridge.log('مبدا یا مقصد تنظیم نشد');return;}let dateText="+q(travelDate.getText().toString().trim())+";let parts=dateText.replace(/-/g,'/').split('/').map(Number);let picker=document.querySelector('app-datepicker-single[name=oneWayDatePicker]');if(!picker||parts.length!==3){MasoudBridge.log('تقویم رجا پیدا نشد');return;}let cal=picker.querySelector('button.icon-calendar')||picker.querySelector('input[name=dp]');if(cal)cal.click();await sleep(650);let jy=parts[0],jm=parts[1],jd=parts[2];let monthNames=['','فروردین','اردیبهشت','خرداد','تیر','مرداد','شهریور','مهر','آبان','آذر','دی','بهمن','اسفند'];for(let sel of [...document.querySelectorAll('select')].filter(x=>x.offsetParent!==null)){let opts=[...sel.options];let yo=opts.find(o=>(o.textContent||'').includes(String(jy)));if(yo){sel.value=yo.value;fire(sel);await sleep(220);continue;}let mo=opts.find(o=>(o.textContent||'').includes(monthNames[jm])||(o.textContent||'').trim()===String(jm));if(mo){sel.value=mo.value;fire(sel);await sleep(220);}}let dayFa=String(jd).replace(/[0-9]/g,d=>'۰۱۲۳۴۵۶۷۸۹'[Number(d)]);let nodes=[...document.querySelectorAll('button,td,span,div')].filter(x=>x.offsetParent!==null);let day=nodes.find(x=>{let t=(x.innerText||'').trim();let c=(x.className||'').toString().toLowerCase();return (t===String(jd)||t===dayFa)&&!c.includes('disabled')&&!c.includes('muted')&&!c.includes('outside');});if(!day){MasoudBridge.log('روز تاریخ در تقویم پیدا نشد');return;}day.click();await sleep(650);let dateInp=picker.querySelector('input[name=dp]');if(!dateInp||!(dateInp.value||'').trim()){MasoudBridge.log('تاریخ در رجا ثبت نشد');return;}let pb=document.querySelector('#dropdownPassenger');if(pb){pb.click();await sleep(250);let target="+total+";let options=[...document.querySelectorAll('.dropdown-menu *')].filter(x=>x.offsetParent!==null);let exact=options.find(x=>{let t=(x.innerText||'').replace(/\\D/g,'');return t===String(target)});if(exact)exact.click();}await sleep(250);"+(coupe.isChecked()?"let cc=[...document.querySelectorAll('input[type=checkbox]')].find(x=>((x.parentElement?.innerText)||'').includes('کوپه دربست'));if(cc&&!cc.checked)cc.click();":"")+"let bs=[...document.querySelectorAll('button')].filter(x=>x.offsetParent!==null);let s=bs.find(x=>(x.innerText||'').trim().includes('جستجو'));if(s){s.click();MasoudBridge.searchClicked();}else MasoudBridge.log('دکمه جستجو پیدا نشد');}catch(e){MasoudBridge.log('خطای تنظیم جستجو: '+e);}})();";
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
    private void loadAccount(){ phone.setText(getPreferences(MODE_PRIVATE).getString("phone","")); password.setText(getPreferences(MODE_PRIVATE).getString("password","")); }
    private void playAlarmOnce(){ if(alarmPlayed) return; alarmPlayed=true; try{ Uri uri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM); if(uri==null) uri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); Ringtone r=RingtoneManager.getRingtone(this,uri); if(r!=null) r.play(); }catch(Exception e){ toast("بلیت پیدا شد و رزرو انجام شد"); } }

    private class JsBridge {
        @JavascriptInterface public void log(String m){ MainActivity.this.log(m); }
        @JavascriptInterface public void loginClicked(){ runOnUiThread(()->{ loggedIn=true; log("دکمه ورود زده شد؛ منتظر تکمیل ورود..."); handler.postDelayed(()->advanceAutomation(),2600); }); }
        @JavascriptInterface public void searchClicked(){ runOnUiThread(()->{ searchSubmitted=true; nextRefreshAt=System.currentTimeMillis()+(long)(getRefreshSeconds()*1000); log("جستجو ارسال شد؛ پایش مداوم فعال است."); handler.postDelayed(()->advanceAutomation(),1300); }); }
        @JavascriptInterface public void reserveClicked(){ runOnUiThread(()->{ reserved=true; status.setText("● بلیت پیدا شد؛ رزرو زده شد..."); log("رزرو بلیت کلیک شد."); showPanel(browserPanel); if(alarm.isChecked()) playAlarmOnce(); }); }
        @JavascriptInterface public void continueClicked(){ runOnUiThread(()->log("ادامه خرید کلیک شد.")); }
        @JavascriptInterface public void notFound(){ runOnUiThread(()->{ reserved=false; status.setText("● یافت نشد؛ جستجوی مجدد..."); log("بلیط مطابق معیار فعلاً یافت نشد."); }); }
    }

}
