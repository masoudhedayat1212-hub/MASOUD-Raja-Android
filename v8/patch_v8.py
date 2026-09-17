from pathlib import Path
import re

p = Path('buildsrc/MASOUD_Android/app/src/main/java/com/masoud/raja/MainActivity.java')
s = p.read_text(encoding='utf-8')
s = s.replace('private static final String VERSION = "V6"', 'private static final String VERSION = "V8"')

if 'import android.graphics.drawable.GradientDrawable;' not in s:
    s = s.replace('import android.graphics.Color;', 'import android.graphics.Color;\nimport android.graphics.drawable.GradientDrawable;')

s = s.replace('private static final int BG = Color.rgb(7,16,24);', 'private static final int BG = Color.rgb(246,251,255);')
s = s.replace('private static final int PANEL = Color.rgb(11,21,30);', 'private static final int PANEL = Color.rgb(255,255,255);')
s = s.replace('private static final int FIELD = Color.rgb(10,20,28);', 'private static final int FIELD = Color.rgb(255,255,255);')
s = s.replace('private static final int TEXT = Color.rgb(244,247,251);', 'private static final int TEXT = Color.rgb(20,28,44);')
s = s.replace('private static final int MUTED = Color.rgb(154,171,186);', 'private static final int MUTED = Color.rgb(127,139,160);')
s = s.replace('private static final int BLUE = Color.rgb(13,125,245);', 'private static final int BLUE = Color.rgb(18,126,242);')
s = s.replace('private static final int GREEN = Color.rgb(0,200,83);', 'private static final int GREEN = Color.rgb(18,126,242);')

def replace_method(src, signature, new_method):
    start = src.index(signature)
    brace = src.index('{', start)
    depth = 0
    for i in range(brace, len(src)):
        if src[i] == '{': depth += 1
        elif src[i] == '}':
            depth -= 1
            if depth == 0:
                return src[:start] + new_method + src[i+1:]
    raise RuntimeError(signature)

dp_sig = '    private int dp(int value)'
dp_start = s.index(dp_sig)
dp_end = s.index('\n', s.index('}', dp_start)) + 1
helper = '''    private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }

    private GradientDrawable rounded(int color, int strokeColor, int radius) {
        GradientDrawable g=new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp(radius));
        if(strokeColor!=Color.TRANSPARENT) g.setStroke(dp(1),strokeColor); return g;
    }

    private GradientDrawable gradient(int c1,int c2,int radius){
        GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{c1,c2});
        g.setCornerRadius(dp(radius)); return g;
    }

    private TextView txt(String value,float size,int color,int gravity,boolean bold){
        TextView t=new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color); t.setGravity(gravity); t.setIncludeFontPadding(false); t.setTextDirection(View.TEXT_DIRECTION_RTL); if(bold)t.setTypeface(null,1); return t;
    }

    private FrameLayout iconBubble(String glyph){
        FrameLayout b=new FrameLayout(this); b.setBackground(rounded(Color.rgb(236,247,255),Color.TRANSPARENT,24)); b.setElevation(dp(2));
        TextView i=txt(glyph,27,BLUE,Gravity.CENTER,true); b.addView(i,new FrameLayout.LayoutParams(-1,-1)); return b;
    }

    private FrameLayout refCard(String labelText, View child, String iconGlyph, boolean arrow) {
        FrameLayout card=new FrameLayout(this); card.setBackground(rounded(Color.WHITE,Color.rgb(228,235,244),16)); card.setElevation(dp(3));

        FrameLayout bubble=iconBubble(iconGlyph); FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(dp(50),dp(50),Gravity.LEFT|Gravity.CENTER_VERTICAL); bp.leftMargin=dp(12); card.addView(bubble,bp);

        LinearLayout center=new LinearLayout(this); center.setOrientation(LinearLayout.VERTICAL); center.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); center.setPadding(0,dp(6),0,dp(5)); center.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        TextView lab=txt(labelText,13,MUTED,Gravity.RIGHT|Gravity.CENTER_VERTICAL,false); center.addView(lab,new LinearLayout.LayoutParams(-1,dp(20)));
        center.addView(child,new LinearLayout.LayoutParams(-1,dp(34)));
        FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(-1,-1,Gravity.CENTER); cp.leftMargin=dp(74); cp.rightMargin=dp(42); card.addView(center,cp);

        if(arrow){ TextView ar=txt("›",34,Color.rgb(111,126,153),Gravity.CENTER,true); FrameLayout.LayoutParams ap=new FrameLayout.LayoutParams(dp(34),-1,Gravity.RIGHT|Gravity.CENTER_VERTICAL); ap.rightMargin=dp(6); card.addView(ar,ap); }
        return card;
    }

    private TextView refValue(String value){ TextView t=txt(value,20,TEXT,Gravity.RIGHT|Gravity.CENTER_VERTICAL,true); return t; }
'''
s = s[:dp_start] + helper + s[dp_end:]

s = replace_method(s, '    private TextView label(String text)', '''    private TextView label(String text) {
        return txt(text,14,TEXT,Gravity.RIGHT|Gravity.CENTER_VERTICAL,false);
    }''')

s = replace_method(s, '    private EditText field(String hint)', '''    private EditText field(String hint) {
        EditText e=new EditText(this); e.setHint(hint); e.setHintTextColor(Color.rgb(20,28,44)); e.setTextColor(TEXT); e.setTextSize(20); e.setSingleLine(true);
        e.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); e.setBackgroundColor(Color.TRANSPARENT); e.setPadding(0,0,0,0); e.setTextDirection(View.TEXT_DIRECTION_RTL); return e;
    }''')

s = replace_method(s, '    private Button button(String text, int color)', '''    private Button button(String text, int color) {
        Button b=new Button(this); b.setText(text); b.setTextColor(Color.WHITE); b.setTextSize(18); b.setAllCaps(false); b.setTypeface(null,1); b.setBackground(rounded(color,Color.TRANSPARENT,22)); b.setMinHeight(dp(52)); return b;
    }''')

s = replace_method(s, '    private LinearLayout section(String title)', '''    private LinearLayout section(String title) {
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(14),dp(12),dp(14),dp(14)); box.setBackground(rounded(Color.WHITE,Color.rgb(225,234,244),16));
        TextView t=txt(title,17,TEXT,Gravity.RIGHT|Gravity.CENTER_VERTICAL,true); box.addView(t,new LinearLayout.LayoutParams(-1,-2)); return box;
    }''')

s = replace_method(s, '    private void buildUi()', '''    private void buildUi() {
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(BG); root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        FrameLayout hero=new FrameLayout(this); hero.setBackground(gradient(Color.rgb(224,246,255),Color.rgb(249,253,255),0));
        LinearLayout heroText=new LinearLayout(this); heroText.setOrientation(LinearLayout.VERTICAL); heroText.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); heroText.setPadding(0,dp(16),dp(26),dp(12));
        LinearLayout titleRow=new LinearLayout(this); titleRow.setOrientation(LinearLayout.HORIZONTAL); titleRow.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); titleRow.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        TextView title=txt("قطار",36,BLUE,Gravity.CENTER,true); TextView badge=txt("V8",18,BLUE,Gravity.CENTER,true); badge.setBackground(rounded(Color.rgb(233,244,253),Color.TRANSPARENT,24));
        titleRow.addView(title,new LinearLayout.LayoutParams(-2,dp(48))); LinearLayout.LayoutParams bpl=new LinearLayout.LayoutParams(dp(70),dp(42)); bpl.setMargins(dp(8),0,0,0); titleRow.addView(badge,bpl);
        heroText.addView(titleRow,new LinearLayout.LayoutParams(-1,dp(52)));
        TextView brand=txt("MASOUD Raja",24,Color.rgb(15,19,31),Gravity.RIGHT|Gravity.CENTER_VERTICAL,true); heroText.addView(brand,new LinearLayout.LayoutParams(-1,dp(38)));
        TextView slogan=txt("سفر بهتر، همیشه نزدیک‌تر",14,Color.rgb(102,116,139),Gravity.RIGHT|Gravity.CENTER_VERTICAL,false); heroText.addView(slogan,new LinearLayout.LayoutParams(-1,dp(32)));
        View underline=new View(this); underline.setBackgroundColor(Color.rgb(21,170,229)); LinearLayout.LayoutParams ulp=new LinearLayout.LayoutParams(dp(74),dp(3)); ulp.gravity=Gravity.RIGHT; heroText.addView(underline,ulp);
        FrameLayout.LayoutParams htp=new FrameLayout.LayoutParams(dp(285),-1,Gravity.RIGHT|Gravity.CENTER_VERTICAL); hero.addView(heroText,htp);

        LinearLayout art=new LinearLayout(this); art.setGravity(Gravity.CENTER); art.setOrientation(LinearLayout.VERTICAL);
        ImageView train=new ImageView(this); train.setImageResource(R.drawable.ic_train); train.setColorFilter(BLUE); train.setScaleType(ImageView.ScaleType.CENTER_INSIDE); art.addView(train,new LinearLayout.LayoutParams(dp(145),dp(100)));
        TextView track=txt("━━━━━━━━━━━━",18,Color.rgb(139,194,233),Gravity.CENTER,false); art.addView(track,new LinearLayout.LayoutParams(-1,dp(25)));
        FrameLayout.LayoutParams arp=new FrameLayout.LayoutParams(dp(230),-1,Gravity.LEFT|Gravity.CENTER_VERTICAL); arp.leftMargin=dp(8); hero.addView(art,arp);
        root.addView(hero,new LinearLayout.LayoutParams(-1,dp(190)));

        LinearLayout tabs=new LinearLayout(this); tabs.setOrientation(LinearLayout.HORIZONTAL); tabs.setGravity(Gravity.CENTER); tabs.setPadding(dp(18),dp(8),dp(18),dp(8)); tabs.setBackgroundColor(Color.TRANSPARENT);
        Button tabRun=button("یک طرفه",BLUE);
        Button tabBrowser=button("اکانت رجا",Color.rgb(248,251,255)); tabBrowser.setTextColor(Color.rgb(39,50,69)); tabBrowser.setBackground(rounded(Color.rgb(249,252,255),Color.rgb(218,228,240),22));
        tabs.addView(tabRun,new LinearLayout.LayoutParams(0,dp(54),1)); Space ts=new Space(this); tabs.addView(ts,new LinearLayout.LayoutParams(dp(8),1)); tabs.addView(tabBrowser,new LinearLayout.LayoutParams(0,dp(54),1));
        root.addView(tabs,new LinearLayout.LayoutParams(-1,dp(70)));

        FrameLayout body=new FrameLayout(this); root.addView(body,new LinearLayout.LayoutParams(-1,0,1));
        runPanel=makeRunPanel(); passengerPanel=makePassengerPanel(); browserPanel=makeBrowserPanel(); body.addView(runPanel); body.addView(passengerPanel); body.addView(browserPanel);
        tabRun.setOnClickListener(v->showPanel(runPanel)); tabBrowser.setOnClickListener(v->showPanel(browserPanel)); setContentView(root);
    }''')

s = replace_method(s, '    private LinearLayout makeRunPanel()', '''    private LinearLayout makeRunPanel(){
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.setVerticalScrollBarEnabled(false);
        LinearLayout p=new LinearLayout(this); p.setOrientation(LinearLayout.VERTICAL); p.setPadding(dp(18),dp(2),dp(18),dp(22)); p.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); scroll.addView(p);

        origin=field("اصفهان"); destination=field("مشهد");
        p.addView(refCard("مبدا",origin,"↕",true),new LinearLayout.LayoutParams(-1,dp(72))); gap(p,7);
        p.addView(refCard("مقصد",destination,"●",true),new LinearLayout.LayoutParams(-1,dp(72))); gap(p,7);

        trainNumber=field("همه"); trainNumber.setInputType(InputType.TYPE_CLASS_NUMBER); p.addView(refCard("شماره قطار",trainNumber,"▣",true),new LinearLayout.LayoutParams(-1,dp(72)));

        priceMode=new CheckBox(this); priceMode.setText(""); priceMode.setChecked(true); priceMode.setButtonTintList(android.content.res.ColorStateList.valueOf(BLUE)); priceMode.setLayoutDirection(View.LAYOUT_DIRECTION_LTR); priceMode.setPadding(0,0,0,0);
        LinearLayout tickRow=new LinearLayout(this); tickRow.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL); tickRow.setPadding(dp(4),0,0,0); tickRow.addView(priceMode,new LinearLayout.LayoutParams(dp(42),dp(34))); p.addView(tickRow,new LinearLayout.LayoutParams(-1,dp(36)));

        minPrice=field("همه"); maxPrice=field("همه"); minPrice.setInputType(InputType.TYPE_CLASS_NUMBER); maxPrice.setInputType(InputType.TYPE_CLASS_NUMBER);
        p.addView(refCard("از قیمت",minPrice,"$",true),new LinearLayout.LayoutParams(-1,dp(72))); gap(p,7);
        p.addView(refCard("تا قیمت",maxPrice,"$",true),new LinearLayout.LayoutParams(-1,dp(72))); gap(p,7);

        travelDate=field(""); p.addView(refCard("تاریخ حرکت",travelDate,"▦",true),new LinearLayout.LayoutParams(-1,dp(72))); gap(p,7);

        adults=field("1"); adults.setInputType(InputType.TYPE_CLASS_NUMBER); adults.setText("1"); adults.setVisibility(View.GONE); p.addView(adults,new LinearLayout.LayoutParams(1,1));
        children=field("0"); children.setInputType(InputType.TYPE_CLASS_NUMBER); children.setText("0"); children.setVisibility(View.GONE); p.addView(children,new LinearLayout.LayoutParams(1,1));
        TextView passengerDisplay=refValue("1 مسافر"); FrameLayout passengerCard=refCard("مسافران",passengerDisplay,"●●",true); passengerCard.setOnClickListener(v->showPanel(passengerPanel)); passengerDisplay.setOnClickListener(v->showPanel(passengerPanel)); p.addView(passengerCard,new LinearLayout.LayoutParams(-1,dp(72))); gap(p,12);

        coupe=new CheckBox(this); coupe.setChecked(false); coupe.setVisibility(View.GONE); p.addView(coupe,new LinearLayout.LayoutParams(1,1));
        alarm=new CheckBox(this); alarm.setChecked(true); alarm.setVisibility(View.GONE); p.addView(alarm,new LinearLayout.LayoutParams(1,1));
        refresh=field("2"); refresh.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL); refresh.setText("2"); refresh.setVisibility(View.GONE); p.addView(refresh,new LinearLayout.LayoutParams(1,1));
        passengerCountLabel=label("1 مسافر"); passengerCountLabel.setVisibility(View.GONE); p.addView(passengerCountLabel,new LinearLayout.LayoutParams(1,1));

        startBtn=button("⌕  اجرای ربات قطار",BLUE); startBtn.setTextSize(19); startBtn.setBackground(gradient(Color.rgb(10,172,232),Color.rgb(19,118,245),24)); stopBtn=button("توقف",RED); stopBtn.setEnabled(false); stopBtn.setVisibility(View.GONE);
        p.addView(startBtn,new LinearLayout.LayoutParams(-1,dp(58))); p.addView(stopBtn,new LinearLayout.LayoutParams(-1,dp(1)));
        status=label("● آماده"); status.setVisibility(View.GONE); p.addView(status,new LinearLayout.LayoutParams(1,1)); logView=new TextView(this); logView.setVisibility(View.GONE); p.addView(logView,new LinearLayout.LayoutParams(1,1));

        startBtn.setOnClickListener(v->startBot()); stopBtn.setOnClickListener(v->stopBot("توقف توسط کاربر"));
        priceMode.setOnCheckedChangeListener((b,checked)->{ trainNumber.setEnabled(!checked); minPrice.setEnabled(checked); maxPrice.setEnabled(checked); });
        trainNumber.setEnabled(false); minPrice.setEnabled(true); maxPrice.setEnabled(true);
        LinearLayout container=new LinearLayout(this); container.setOrientation(LinearLayout.VERTICAL); container.addView(scroll,new LinearLayout.LayoutParams(-1,-1)); return container;
    }''')

s = replace_method(s, '    private LinearLayout makeBrowserPanel()', '''    private LinearLayout makeBrowserPanel(){
        LinearLayout p=new LinearLayout(this); p.setOrientation(LinearLayout.VERTICAL); p.setPadding(dp(18),dp(10),dp(18),dp(18)); p.setBackgroundColor(BG);
        TextView t=txt("اکانت رجا",22,TEXT,Gravity.RIGHT|Gravity.CENTER_VERTICAL,true); p.addView(t,new LinearLayout.LayoutParams(-1,dp(42)));
        phone=field("شماره موبایل حساب رجا"); phone.setInputType(InputType.TYPE_CLASS_PHONE); p.addView(refCard("شماره موبایل",phone,"●",false),new LinearLayout.LayoutParams(-1,dp(72))); gap(p,9);
        password=field("رمز عبور"); password.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD); p.addView(refCard("رمز عبور",password,"●",false),new LinearLayout.LayoutParams(-1,dp(72))); gap(p,12);
        Button save=button("ذخیره اکانت",BLUE); save.setOnClickListener(v->{ saveAccount(); toast("اکانت ذخیره شد"); }); p.addView(save,new LinearLayout.LayoutParams(-1,dp(54)));
        webView=new WebView(this); webView.setVisibility(View.GONE); p.addView(webView,new LinearLayout.LayoutParams(1,1)); return p;
    }''')

p.write_text(s, encoding='utf-8')

app = Path('buildsrc/MASOUD_Android')
manifest = app / 'app/src/main/AndroidManifest.xml'
m = manifest.read_text(encoding='utf-8')
m = m.replace('android:label="MASOUD Raja"', 'android:label="MASOUD Raja V8"').replace('android:label="MASOUD"', 'android:label="MASOUD Raja V8"')
if 'android:icon=' not in m:
    m = m.replace('<application ', '<application android:icon="@drawable/ic_train" android:roundIcon="@drawable/ic_train" ', 1)
manifest.write_text(m, encoding='utf-8')

styles = app / 'app/src/main/res/values/styles.xml'
st = styles.read_text(encoding='utf-8')
st = st.replace('<item name="android:windowLightStatusBar">false</item>', '<item name="android:windowLightStatusBar">true</item>').replace('#071018', '#F6FBFF')
styles.write_text(st, encoding='utf-8')

gradle = app / 'app/build.gradle'
g = gradle.read_text(encoding='utf-8')
g = re.sub(r'versionCode\s+\d+', 'versionCode 8', g)
g = re.sub(r"versionName\s+'[^']+'", "versionName '8.0.0'", g)
gradle.write_text(g, encoding='utf-8')
