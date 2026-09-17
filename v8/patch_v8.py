from pathlib import Path
import re

p = Path('buildsrc/MASOUD_Android/app/src/main/java/com/masoud/raja/MainActivity.java')
s = p.read_text(encoding='utf-8')

# Keep V6 Raja login behavior, only move product/UI version to V8.
s = s.replace('private static final String VERSION = "V6"', 'private static final String VERSION = "V8"')

if 'import android.graphics.drawable.GradientDrawable;' not in s:
    s = s.replace('import android.graphics.Color;', 'import android.graphics.Color;\nimport android.graphics.drawable.GradientDrawable;')

s = s.replace('private static final int BG = Color.rgb(7,16,24);', 'private static final int BG = Color.rgb(246,251,255);')
s = s.replace('private static final int PANEL = Color.rgb(11,21,30);', 'private static final int PANEL = Color.rgb(255,255,255);')
s = s.replace('private static final int FIELD = Color.rgb(10,20,28);', 'private static final int FIELD = Color.rgb(255,255,255);')
s = s.replace('private static final int TEXT = Color.rgb(244,247,251);', 'private static final int TEXT = Color.rgb(25,35,52);')
s = s.replace('private static final int MUTED = Color.rgb(154,171,186);', 'private static final int MUTED = Color.rgb(125,139,160);')
s = s.replace('private static final int BLUE = Color.rgb(13,125,245);', 'private static final int BLUE = Color.rgb(17,125,241);')
s = s.replace('private static final int GREEN = Color.rgb(0,200,83);', 'private static final int GREEN = Color.rgb(17,125,241);')


def replace_method(src, signature, new_method):
    start = src.index(signature)
    brace = src.index('{', start)
    depth = 0
    end = None
    for i in range(brace, len(src)):
        if src[i] == '{':
            depth += 1
        elif src[i] == '}':
            depth -= 1
            if depth == 0:
                end = i + 1
                break
    if end is None:
        raise RuntimeError(signature)
    return src[:start] + new_method + src[end:]


dp_sig = '    private int dp(int value)'
dp_start = s.index(dp_sig)
dp_end = s.index('\n', s.index('}', dp_start)) + 1
helper = '''    private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }

    private GradientDrawable rounded(int color, int strokeColor, int radius) {
        GradientDrawable g=new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp(radius));
        if(strokeColor!=Color.TRANSPARENT) g.setStroke(dp(1),strokeColor); return g;
    }

    private LinearLayout refCard(String labelText, View child) {
        LinearLayout c=new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(dp(16),dp(8),dp(16),dp(8));
        c.setBackground(rounded(Color.WHITE,Color.rgb(225,234,244),15));
        c.setElevation(dp(3));
        if(labelText!=null && !labelText.isEmpty()){ TextView l=label(labelText); l.setTextColor(MUTED); l.setTextSize(13); c.addView(l,new LinearLayout.LayoutParams(-1,dp(21))); }
        c.addView(child,new LinearLayout.LayoutParams(-1,dp(40))); return c;
    }

    private TextView refValue(String value){
        TextView t=label(value); t.setTextColor(TEXT); t.setTextSize(20); t.setTypeface(null,1); return t;
    }
'''
s = s[:dp_start] + helper + s[dp_end:]

s = replace_method(s, '    private TextView label(String text)', '''    private TextView label(String text) {
        TextView v=new TextView(this); v.setText(text); v.setTextColor(TEXT); v.setTextSize(14); v.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); v.setPadding(0,0,0,0); v.setTextDirection(View.TEXT_DIRECTION_RTL); return v;
    }''')

s = replace_method(s, '    private EditText field(String hint)', '''    private EditText field(String hint) {
        EditText e=new EditText(this); e.setHint(hint); e.setHintTextColor(MUTED); e.setTextColor(TEXT); e.setTextSize(19); e.setSingleLine(true);
        e.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); e.setBackgroundColor(Color.TRANSPARENT); e.setPadding(0,0,0,0); e.setTextDirection(View.TEXT_DIRECTION_RTL); e.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(40))); return e;
    }''')

s = replace_method(s, '    private Button button(String text, int color)', '''    private Button button(String text, int color) {
        Button b=new Button(this); b.setText(text); b.setTextColor(Color.WHITE); b.setTextSize(17); b.setAllCaps(false); b.setBackground(rounded(color,Color.TRANSPARENT,18)); b.setMinHeight(dp(52)); return b;
    }''')

s = replace_method(s, '    private LinearLayout section(String title)', '''    private LinearLayout section(String title) {
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(14),dp(12),dp(14),dp(14)); box.setBackground(rounded(Color.WHITE,Color.rgb(222,231,241),15));
        TextView t=label(title); t.setTextSize(17); t.setTypeface(null,1); box.addView(t,new LinearLayout.LayoutParams(-1,-2)); return box;
    }''')

s = replace_method(s, '    private void buildUi()', '''    private void buildUi() {
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(BG); root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        ImageView hero=new ImageView(this); hero.setImageResource(R.drawable.header_reference); hero.setScaleType(ImageView.ScaleType.CENTER_CROP); hero.setAdjustViewBounds(false);
        root.addView(hero,new LinearLayout.LayoutParams(-1,dp(190)));

        LinearLayout tabs=new LinearLayout(this); tabs.setOrientation(LinearLayout.HORIZONTAL); tabs.setGravity(Gravity.CENTER); tabs.setPadding(dp(16),dp(7),dp(16),dp(7)); tabs.setBackgroundColor(Color.TRANSPARENT);
        Button tabRun=button("یک طرفه",BLUE);
        Button tabBrowser=button("اکانت رجا",Color.rgb(245,249,253)); tabBrowser.setTextColor(Color.rgb(42,55,73)); tabBrowser.setBackground(rounded(Color.rgb(248,251,255),Color.rgb(219,230,242),18));
        tabs.addView(tabRun,new LinearLayout.LayoutParams(0,dp(52),1));
        Space ts=new Space(this); tabs.addView(ts,new LinearLayout.LayoutParams(dp(8),1));
        tabs.addView(tabBrowser,new LinearLayout.LayoutParams(0,dp(52),1));
        root.addView(tabs,new LinearLayout.LayoutParams(-1,dp(68)));

        FrameLayout body=new FrameLayout(this); root.addView(body,new LinearLayout.LayoutParams(-1,0,1));
        runPanel=makeRunPanel(); passengerPanel=makePassengerPanel(); browserPanel=makeBrowserPanel();
        body.addView(runPanel); body.addView(passengerPanel); body.addView(browserPanel);
        tabRun.setOnClickListener(v->showPanel(runPanel));
        tabBrowser.setOnClickListener(v->showPanel(browserPanel));
        setContentView(root);
    }''')

s = replace_method(s, '    private LinearLayout makeRunPanel()', '''    private LinearLayout makeRunPanel(){
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true);
        LinearLayout p=new LinearLayout(this); p.setOrientation(LinearLayout.VERTICAL); p.setPadding(dp(18),dp(4),dp(18),dp(24)); p.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); scroll.addView(p);

        LinearLayout stations=new LinearLayout(this); stations.setOrientation(LinearLayout.VERTICAL); stations.setBackground(rounded(Color.WHITE,Color.rgb(225,234,244),15)); stations.setElevation(dp(3)); stations.setPadding(dp(16),dp(7),dp(16),dp(7));
        origin=field("اصفهان"); destination=field("مشهد");
        LinearLayout oWrap=new LinearLayout(this); oWrap.setOrientation(LinearLayout.VERTICAL); TextView ol=label("مبدا"); ol.setTextColor(MUTED); ol.setTextSize(13); oWrap.addView(ol,new LinearLayout.LayoutParams(-1,dp(20))); oWrap.addView(origin,new LinearLayout.LayoutParams(-1,dp(38)));
        LinearLayout dWrap=new LinearLayout(this); dWrap.setOrientation(LinearLayout.VERTICAL); TextView dl=label("مقصد"); dl.setTextColor(MUTED); dl.setTextSize(13); dWrap.addView(dl,new LinearLayout.LayoutParams(-1,dp(20))); dWrap.addView(destination,new LinearLayout.LayoutParams(-1,dp(38)));
        stations.addView(oWrap,new LinearLayout.LayoutParams(-1,dp(60))); View line=new View(this); line.setBackgroundColor(Color.rgb(228,235,242)); stations.addView(line,new LinearLayout.LayoutParams(-1,dp(1))); stations.addView(dWrap,new LinearLayout.LayoutParams(-1,dp(60))); p.addView(stations,new LinearLayout.LayoutParams(-1,dp(121))); gap(p,10);

        trainNumber=field(""); trainNumber.setInputType(InputType.TYPE_CLASS_NUMBER); p.addView(refCard("شماره قطار",trainNumber),new LinearLayout.LayoutParams(-1,dp(76)));

        priceMode=new CheckBox(this); priceMode.setText(""); priceMode.setButtonTintList(android.content.res.ColorStateList.valueOf(BLUE)); priceMode.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        LinearLayout tickRow=new LinearLayout(this); tickRow.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL); tickRow.addView(priceMode,new LinearLayout.LayoutParams(dp(44),dp(38))); p.addView(tickRow,new LinearLayout.LayoutParams(-1,dp(38)));

        minPrice=field(""); maxPrice=field(""); minPrice.setInputType(InputType.TYPE_CLASS_NUMBER); maxPrice.setInputType(InputType.TYPE_CLASS_NUMBER);
        p.addView(refCard("از قیمت",minPrice),new LinearLayout.LayoutParams(-1,dp(76))); gap(p,10);
        p.addView(refCard("تا قیمت",maxPrice),new LinearLayout.LayoutParams(-1,dp(76))); gap(p,10);

        travelDate=field("تاریخ حرکت"); p.addView(refCard("تاریخ حرکت",travelDate),new LinearLayout.LayoutParams(-1,dp(76))); gap(p,10);

        adults=field("1"); adults.setInputType(InputType.TYPE_CLASS_NUMBER); adults.setText("1"); adults.setVisibility(View.GONE); p.addView(adults,new LinearLayout.LayoutParams(1,1));
        children=field("0"); children.setInputType(InputType.TYPE_CLASS_NUMBER); children.setText("0"); children.setVisibility(View.GONE); p.addView(children,new LinearLayout.LayoutParams(1,1));
        TextView passengerDisplay=refValue("1 مسافر"); LinearLayout passengerCard=refCard("مسافران",passengerDisplay); passengerCard.setOnClickListener(v->showPanel(passengerPanel)); passengerDisplay.setOnClickListener(v->showPanel(passengerPanel)); p.addView(passengerCard,new LinearLayout.LayoutParams(-1,dp(80))); gap(p,16);

        coupe=new CheckBox(this); coupe.setChecked(false); coupe.setVisibility(View.GONE); p.addView(coupe,new LinearLayout.LayoutParams(1,1));
        alarm=new CheckBox(this); alarm.setChecked(true); alarm.setVisibility(View.GONE); p.addView(alarm,new LinearLayout.LayoutParams(1,1));
        refresh=field("2"); refresh.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL); refresh.setText("2"); refresh.setVisibility(View.GONE); p.addView(refresh,new LinearLayout.LayoutParams(1,1));
        passengerCountLabel=label("1 مسافر"); passengerCountLabel.setVisibility(View.GONE); p.addView(passengerCountLabel,new LinearLayout.LayoutParams(1,1));

        startBtn=button("اجرای ربات قطار",BLUE); stopBtn=button("توقف",RED); stopBtn.setEnabled(false); stopBtn.setVisibility(View.GONE); p.addView(startBtn,new LinearLayout.LayoutParams(-1,dp(58))); p.addView(stopBtn,new LinearLayout.LayoutParams(-1,dp(1)));
        status=label("● آماده"); status.setVisibility(View.GONE); p.addView(status,new LinearLayout.LayoutParams(1,1));
        logView=new TextView(this); logView.setVisibility(View.GONE); p.addView(logView,new LinearLayout.LayoutParams(1,1));

        startBtn.setOnClickListener(v->startBot()); stopBtn.setOnClickListener(v->stopBot("توقف توسط کاربر"));
        priceMode.setOnCheckedChangeListener((b,checked)->{ trainNumber.setEnabled(!checked); minPrice.setEnabled(checked); maxPrice.setEnabled(checked); });
        minPrice.setEnabled(false); maxPrice.setEnabled(false);
        LinearLayout container=new LinearLayout(this); container.setOrientation(LinearLayout.VERTICAL); container.addView(scroll,new LinearLayout.LayoutParams(-1,-1)); return container;
    }''')

s = replace_method(s, '    private LinearLayout makeBrowserPanel()', '''    private LinearLayout makeBrowserPanel(){
        LinearLayout p=new LinearLayout(this); p.setOrientation(LinearLayout.VERTICAL); p.setPadding(dp(18),dp(12),dp(18),dp(18)); p.setBackgroundColor(BG);
        TextView t=label("اکانت رجا"); t.setTextSize(20); t.setTypeface(null,1); p.addView(t,new LinearLayout.LayoutParams(-1,dp(42)));
        phone=field("شماره موبایل حساب رجا"); phone.setInputType(InputType.TYPE_CLASS_PHONE); p.addView(refCard("شماره موبایل",phone),new LinearLayout.LayoutParams(-1,dp(78))); gap(p,10);
        password=field("رمز عبور"); password.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD); p.addView(refCard("رمز عبور",password),new LinearLayout.LayoutParams(-1,dp(78))); gap(p,12);
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
