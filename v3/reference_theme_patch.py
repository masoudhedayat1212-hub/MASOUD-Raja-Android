from pathlib import Path
import re
p=Path('buildsrc/MASOUD_Android/app/src/main/java/com/masoud/raja/MainActivity.java')
s=p.read_text(encoding='utf-8')

# Light reference palette; bot logic below remains untouched.
s=re.sub(r'private static final int BG = .*?;', 'private static final int BG = Color.rgb(244,250,255);', s)
s=re.sub(r'private static final int PANEL = .*?;', 'private static final int PANEL = Color.WHITE;', s)
s=re.sub(r'private static final int PANEL_2 = .*?;', 'private static final int PANEL_2 = Color.rgb(244,249,253);', s)
s=re.sub(r'private static final int FIELD = .*?;', 'private static final int FIELD = Color.WHITE;', s)
s=re.sub(r'private static final int TEXT = .*?;', 'private static final int TEXT = Color.rgb(10,18,34);', s)
s=re.sub(r'private static final int MUTED = .*?;', 'private static final int MUTED = Color.rgb(119,133,157);', s)
s=re.sub(r'private static final int BLUE = .*?;', 'private static final int BLUE = Color.rgb(13,120,246);', s)
s=re.sub(r'private static final int CYAN = .*?;', 'private static final int CYAN = Color.rgb(13,182,228);', s)
s=re.sub(r'private static final int GREEN = .*?;', 'private static final int GREEN = Color.rgb(13,120,246);', s)
s=re.sub(r'private static final int RED = .*?;', 'private static final int RED = Color.rgb(235,74,84);', s)
s=re.sub(r'private static final int BORDER = .*?;', 'private static final int BORDER = Color.rgb(222,232,241);', s)

# Helpers for reference cards.
anchor='    private void buildUi() {'
helper=r'''    private TextView refIcon(String text){
        TextView v=new TextView(this); v.setText(text); v.setTextColor(BLUE); v.setTextSize(30); v.setGravity(Gravity.CENTER);
        v.setTypeface(null,1); v.setBackground(bg(Color.rgb(237,247,255),28,Color.rgb(230,239,247),1)); v.setElevation(dp(4)); return v;
    }
    private TextView refArrow(){ TextView v=new TextView(this); v.setText("›"); v.setTextColor(Color.rgb(111,130,158)); v.setTextSize(42); v.setGravity(Gravity.CENTER); return v; }
    private LinearLayout refCard(String labelText, String valueText, String iconText){
        LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(14),dp(7),dp(14),dp(7));
        row.setBackground(bg(Color.WHITE,24,Color.rgb(228,235,242),1)); row.setElevation(dp(5)); row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        TextView icon=refIcon(iconText); row.addView(icon,new LinearLayout.LayoutParams(dp(62),dp(62))); gapH(row,10);
        LinearLayout txt=new LinearLayout(this); txt.setOrientation(LinearLayout.VERTICAL); txt.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); txt.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        TextView l=new TextView(this); l.setText(labelText); l.setTextColor(MUTED); l.setTextSize(15); l.setGravity(Gravity.RIGHT); txt.addView(l,new LinearLayout.LayoutParams(-1,dp(25)));
        TextView val=new TextView(this); val.setText(valueText); val.setTextColor(TEXT); val.setTextSize(23); val.setTypeface(null,1); val.setGravity(Gravity.RIGHT); txt.addView(val,new LinearLayout.LayoutParams(-1,dp(34)));
        row.addView(txt,new LinearLayout.LayoutParams(0,dp(62),1)); row.addView(refArrow(),new LinearLayout.LayoutParams(dp(34),dp(62))); return row;
    }
    private LinearLayout refInputCard(String labelText, EditText field, String iconText){
        LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(14),dp(7),dp(14),dp(7));
        row.setBackground(bg(Color.WHITE,24,Color.rgb(228,235,242),1)); row.setElevation(dp(5)); row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        TextView icon=refIcon(iconText); row.addView(icon,new LinearLayout.LayoutParams(dp(62),dp(62))); gapH(row,10);
        LinearLayout txt=new LinearLayout(this); txt.setOrientation(LinearLayout.VERTICAL); txt.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); txt.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        TextView l=new TextView(this); l.setText(labelText); l.setTextColor(MUTED); l.setTextSize(15); l.setGravity(Gravity.RIGHT); txt.addView(l,new LinearLayout.LayoutParams(-1,dp(23)));
        field.setTextColor(TEXT); field.setHintTextColor(Color.rgb(140,150,168)); field.setTextSize(22); field.setTypeface(null,1); field.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); field.setBackgroundColor(Color.TRANSPARENT); field.setPadding(0,0,0,0);
        txt.addView(field,new LinearLayout.LayoutParams(-1,dp(38))); row.addView(txt,new LinearLayout.LayoutParams(0,dp(62),1)); row.addView(refArrow(),new LinearLayout.LayoutParams(dp(34),dp(62))); return row;
    }

'''
if helper not in s:
    s=s.replace(anchor,helper+anchor,1)

def replace_method(src, signature, body):
    start=src.index(signature)
    brace=src.index('{',start); depth=0; end=None
    for i in range(brace,len(src)):
        if src[i]=='{': depth+=1
        elif src[i]=='}':
            depth-=1
            if depth==0: end=i+1; break
    return src[:start]+body+src[end:]

build=r'''    private void buildUi() {
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(BG); root.setPadding(0,0,0,0);
        ImageView hero=new ImageView(this); hero.setImageResource(R.drawable.header_reference_v3); hero.setScaleType(ImageView.ScaleType.CENTER_CROP); hero.setAdjustViewBounds(false); root.addView(hero,new LinearLayout.LayoutParams(-1,dp(185)));
        LinearLayout tabs=new LinearLayout(this); tabs.setOrientation(LinearLayout.HORIZONTAL); tabs.setPadding(dp(18),dp(8),dp(18),dp(8)); tabs.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        tabRun=button("یک طرفه",BLUE); tabRun.setTextSize(23); tabRun.setTypeface(null,1); tabRun.setElevation(dp(4));
        tabBrowser=button("اکانت رجا",Color.rgb(248,251,253)); tabBrowser.setTextColor(TEXT); tabBrowser.setTextSize(22); tabBrowser.setTypeface(null,1); tabBrowser.setElevation(dp(3));
        tabPassengers=button("",Color.TRANSPARENT); tabPassengers.setVisibility(View.GONE);
        tabs.addView(tabRun,new LinearLayout.LayoutParams(0,dp(62),1)); gapH(tabs,8); tabs.addView(tabBrowser,new LinearLayout.LayoutParams(0,dp(62),1)); root.addView(tabs);
        FrameLayout body=new FrameLayout(this); root.addView(body,new LinearLayout.LayoutParams(-1,0,1));
        runPanel=makeRunPanel(); passengerPanel=makePassengerPanel(); browserPanel=makeBrowserPanel(); body.addView(runPanel); body.addView(passengerPanel); body.addView(browserPanel);
        tabRun.setOnClickListener(v->showPanel(runPanel)); tabBrowser.setOnClickListener(v->showPanel(browserPanel)); setContentView(root);
    }'''
s=replace_method(s,'    private void buildUi() {',build)

show=r'''    private void showPanel(View target){
        if(runPanel!=null) runPanel.setVisibility(target==runPanel?View.VISIBLE:View.GONE);
        if(passengerPanel!=null) passengerPanel.setVisibility(target==passengerPanel?View.VISIBLE:View.GONE);
        if(browserPanel!=null) browserPanel.setVisibility(target==browserPanel?View.VISIBLE:View.GONE);
        if(tabRun!=null){ tabRun.setBackground(bg(target==runPanel?BLUE:Color.rgb(248,251,253),26,BORDER,1)); tabRun.setTextColor(target==runPanel?Color.WHITE:TEXT); }
        if(tabBrowser!=null){ tabBrowser.setBackground(bg(target==browserPanel?BLUE:Color.rgb(248,251,253),26,BORDER,1)); tabBrowser.setTextColor(target==browserPanel?Color.WHITE:TEXT); }
    }'''
s=replace_method(s,'    private void showPanel(View target){',show)

run=r'''    private LinearLayout makeRunPanel(){
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(BG);
        LinearLayout p=new LinearLayout(this); p.setOrientation(LinearLayout.VERTICAL); p.setPadding(dp(18),dp(4),dp(18),dp(22)); scroll.addView(p);
        origin=stationField("اصفهان"); origin.setText("اصفهان"); destination=stationField("مشهد"); destination.setText("مشهد");
        trainNumber=field("همه"); trainNumber.setInputType(InputType.TYPE_CLASS_NUMBER); minPrice=field("همه"); minPrice.setInputType(InputType.TYPE_CLASS_NUMBER); maxPrice=field("همه"); maxPrice.setInputType(InputType.TYPE_CLASS_NUMBER);
        travelDate=field("انتخاب تاریخ"); travelDate.setFocusable(false); travelDate.setOnClickListener(v->showPersianCalendar());
        adults=field(""); adults.setText("1"); adults.setInputType(InputType.TYPE_CLASS_NUMBER); children=field(""); children.setText("0"); children.setVisibility(View.GONE);
        p.addView(refInputCard("مبدا",origin,"⇅"),new LinearLayout.LayoutParams(-1,dp(82))); gap(p,8);
        p.addView(refInputCard("مقصد",destination,"●"),new LinearLayout.LayoutParams(-1,dp(82))); gap(p,8);
        p.addView(refInputCard("شماره قطار",trainNumber,"▣"),new LinearLayout.LayoutParams(-1,dp(82))); gap(p,3);
        priceMode=new CheckBox(this); priceMode.setChecked(true); priceMode.setButtonTintList(android.content.res.ColorStateList.valueOf(BLUE)); priceMode.setPadding(dp(8),0,0,0); p.addView(priceMode,new LinearLayout.LayoutParams(dp(55),dp(34)));
        p.addView(refInputCard("از قیمت",minPrice,"$"),new LinearLayout.LayoutParams(-1,dp(82))); gap(p,8);
        p.addView(refInputCard("تا قیمت",maxPrice,"$"),new LinearLayout.LayoutParams(-1,dp(82))); gap(p,8);
        p.addView(refInputCard("تاریخ حرکت",travelDate,"▣"),new LinearLayout.LayoutParams(-1,dp(82))); gap(p,8);
        p.addView(refInputCard("مسافران",adults,"●●"),new LinearLayout.LayoutParams(-1,dp(82))); gap(p,12);
        startBtn=button("⌕  اجرای ربات قطار",BLUE); startBtn.setTextSize(22); startBtn.setTypeface(null,1); startBtn.setElevation(dp(5)); p.addView(startBtn,new LinearLayout.LayoutParams(-1,dp(62))); gap(p,10);
        stopBtn=button("توقف",RED); stopBtn.setVisibility(View.GONE);
        phone=field("شماره موبایل حساب رجا"); password=field("رمز عبور"); password.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD); phone.setVisibility(View.GONE); password.setVisibility(View.GONE); p.addView(phone,new LinearLayout.LayoutParams(1,1)); p.addView(password,new LinearLayout.LayoutParams(1,1));
        refresh=field(""); refresh.setText("2"); refresh.setVisibility(View.GONE); p.addView(refresh,new LinearLayout.LayoutParams(1,1));
        coupe=new CheckBox(this); coupe.setChecked(false); coupe.setVisibility(View.GONE); alarm=new CheckBox(this); alarm.setChecked(true); alarm.setVisibility(View.GONE); p.addView(coupe,new LinearLayout.LayoutParams(1,1)); p.addView(alarm,new LinearLayout.LayoutParams(1,1)); p.addView(children,new LinearLayout.LayoutParams(1,1));
        status=label("آماده"); status.setVisibility(View.GONE); p.addView(status,new LinearLayout.LayoutParams(1,1)); logView=new TextView(this); logView.setVisibility(View.GONE); p.addView(logView,new LinearLayout.LayoutParams(1,1)); passengerCountLabel=label(""); passengerCountLabel.setVisibility(View.GONE); p.addView(passengerCountLabel,new LinearLayout.LayoutParams(1,1));
        startBtn.setOnClickListener(v->startBot()); priceMode.setOnCheckedChangeListener((b,checked)->{ trainNumber.setEnabled(!checked); minPrice.setEnabled(checked); maxPrice.setEnabled(checked); }); trainNumber.setEnabled(false); minPrice.setEnabled(true); maxPrice.setEnabled(true);
        LinearLayout container=new LinearLayout(this); container.setOrientation(LinearLayout.VERTICAL); container.addView(scroll,new LinearLayout.LayoutParams(-1,-1)); return container;
    }'''
s=replace_method(s,'    private LinearLayout makeRunPanel(){',run)

# Account tab: keep credentials available in the reference palette, with a button to open Raja browser.
acc=r'''    private LinearLayout makeBrowserPanel(){
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(18),dp(18),dp(18),dp(18)); box.setBackgroundColor(BG);
        TextView title=label("اکانت رجا"); title.setTextColor(TEXT); title.setTextSize(24); title.setTypeface(null,1); title.setGravity(Gravity.RIGHT); box.addView(title,new LinearLayout.LayoutParams(-1,dp(50)));
        EditText ph=field("شماره موبایل"); EditText pw=field("رمز عبور"); pw.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD); box.addView(ph,new LinearLayout.LayoutParams(-1,dp(58))); gap(box,10); box.addView(pw,new LinearLayout.LayoutParams(-1,dp(58))); gap(box,14);
        Button save=button("ذخیره و بازگشت",BLUE); box.addView(save,new LinearLayout.LayoutParams(-1,dp(58))); gap(box,10);
        Button open=button("نمایش سایت رجا",Color.rgb(35,153,233)); box.addView(open,new LinearLayout.LayoutParams(-1,dp(58)));
        save.setOnClickListener(v->{ phone.setText(ph.getText()); password.setText(pw.getText()); showPanel(runPanel); }); open.setOnClickListener(v->{ if(webView!=null){ webView.setVisibility(View.VISIBLE); } });
        return box;
    }'''
s=replace_method(s,'    private LinearLayout makeBrowserPanel(){',acc)

# Preserve V3 identity in title/visible build strings if present.
s=s.replace('MASOUD Raja Bot  •  V3','MASOUD Raja Bot  •  V3')
p.write_text(s,encoding='utf-8')
