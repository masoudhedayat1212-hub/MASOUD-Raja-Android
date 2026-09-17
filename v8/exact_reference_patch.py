from pathlib import Path
p=Path('buildsrc/MASOUD_Android/app/src/main/java/com/masoud/raja/MainActivity.java')
s=p.read_text(encoding='utf-8')

def replace_method(src, signature, new_method):
    start=src.index(signature)
    brace=src.index('{',start)
    depth=0
    for i in range(brace,len(src)):
        if src[i]=='{': depth+=1
        elif src[i]=='}':
            depth-=1
            if depth==0:
                return src[:start]+new_method+src[i+1:]
    raise RuntimeError(signature)

if 'private void addHotspot(' not in s:
    anchor='    private TextView refValue(String value)'
    pos=s.index(anchor)
    end=s.index('\n',s.index('}',pos))+1
    helper='''\n    private void addHotspot(FrameLayout root,float l,float t,float r,float b,View.OnClickListener click){\n        View v=new View(this); v.setBackgroundColor(Color.TRANSPARENT); v.setOnClickListener(click); root.addView(v,new FrameLayout.LayoutParams(1,1));\n        root.post(()->{ int w=root.getWidth(),h=root.getHeight(); FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams((int)((r-l)*w),(int)((b-t)*h)); lp.leftMargin=(int)(l*w); lp.topMargin=(int)(t*h); v.setLayoutParams(lp); });\n    }\n\n    private EditText hiddenField(String value){ EditText e=new EditText(this); e.setText(value); e.setVisibility(View.GONE); return e; }\n'''
    s=s[:end]+helper+s[end:]

s=replace_method(s,'    private void buildUi()','''    private void buildUi() {\n        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);\n        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);\n        FrameLayout root=new FrameLayout(this); root.setBackgroundColor(Color.rgb(246,251,255));\n        ImageView ref=new ImageView(this); ref.setImageResource(R.drawable.reference_screen); ref.setScaleType(ImageView.ScaleType.FIT_XY); root.addView(ref,new FrameLayout.LayoutParams(-1,-1));\n\n        runPanel=makeRunPanel(); passengerPanel=makePassengerPanel(); browserPanel=makeBrowserPanel();\n        runPanel.setVisibility(View.GONE); passengerPanel.setVisibility(View.GONE); browserPanel.setVisibility(View.GONE);\n        root.addView(runPanel,new FrameLayout.LayoutParams(1,1)); root.addView(passengerPanel,new FrameLayout.LayoutParams(1,1)); root.addView(browserPanel,new FrameLayout.LayoutParams(1,1));\n\n        addHotspot(root,0.04f,0.255f,0.49f,0.330f,v->{});\n        addHotspot(root,0.51f,0.255f,0.96f,0.330f,v->showPanel(browserPanel));\n        addHotspot(root,0.04f,0.342f,0.96f,0.420f,v->origin.requestFocus());\n        addHotspot(root,0.04f,0.426f,0.96f,0.505f,v->destination.requestFocus());\n        addHotspot(root,0.04f,0.512f,0.96f,0.592f,v->trainNumber.requestFocus());\n        addHotspot(root,0.04f,0.620f,0.96f,0.699f,v->minPrice.requestFocus());\n        addHotspot(root,0.04f,0.706f,0.96f,0.785f,v->maxPrice.requestFocus());\n        addHotspot(root,0.04f,0.791f,0.96f,0.870f,v->travelDate.requestFocus());\n        addHotspot(root,0.04f,0.876f,0.96f,0.948f,v->showPanel(passengerPanel));\n        addHotspot(root,0.04f,0.949f,0.96f,0.997f,v->startBot());\n        setContentView(root);\n    }''')

s=replace_method(s,'    private LinearLayout makeRunPanel()','''    private LinearLayout makeRunPanel(){\n        LinearLayout p=new LinearLayout(this); p.setOrientation(LinearLayout.VERTICAL);\n        origin=hiddenField("اصفهان"); destination=hiddenField("مشهد"); trainNumber=hiddenField(""); minPrice=hiddenField(""); maxPrice=hiddenField(""); travelDate=hiddenField("");\n        adults=hiddenField("1"); children=hiddenField("0"); phone=hiddenField(""); password=hiddenField(""); refresh=hiddenField("2");\n        priceMode=new CheckBox(this); priceMode.setChecked(true); priceMode.setVisibility(View.GONE); coupe=new CheckBox(this); coupe.setVisibility(View.GONE); alarm=new CheckBox(this); alarm.setChecked(true); alarm.setVisibility(View.GONE);\n        passengerCountLabel=label("1 مسافر"); passengerCountLabel.setVisibility(View.GONE);\n        startBtn=button("اجرای ربات قطار",BLUE); startBtn.setVisibility(View.GONE); stopBtn=button("توقف",RED); stopBtn.setVisibility(View.GONE);\n        status=label("● آماده"); status.setVisibility(View.GONE); logView=new TextView(this); logView.setVisibility(View.GONE);\n        p.addView(origin); p.addView(destination); p.addView(trainNumber); p.addView(minPrice); p.addView(maxPrice); p.addView(travelDate); p.addView(adults); p.addView(children); p.addView(phone); p.addView(password); p.addView(refresh); p.addView(priceMode); p.addView(coupe); p.addView(alarm); p.addView(passengerCountLabel); p.addView(startBtn); p.addView(stopBtn); p.addView(status); p.addView(logView);\n        return p;\n    }''')

p.write_text(s,encoding='utf-8')
