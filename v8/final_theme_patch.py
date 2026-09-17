from pathlib import Path
p=Path('buildsrc/MASOUD_Android/app/src/main/java/com/masoud/raja/MainActivity.java')
s=p.read_text(encoding='utf-8')

for imp in ['import android.graphics.Canvas;','import android.graphics.Paint;','import android.graphics.Path;','import android.graphics.RectF;']:
    if imp not in s:
        s=s.replace('import android.graphics.Color;', 'import android.graphics.Color;\n'+imp,1)

if 'private class RefIconView extends View' not in s:
    anchor='    private FrameLayout iconBubble(String glyph)'
    pos=s.index(anchor)
    helper='''    private class RefIconView extends View {\n        private final String kind; private final Paint p=new Paint(1);\n        RefIconView(String k){ super(MainActivity.this); kind=k; p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeJoin(Paint.Join.ROUND); }\n        @Override protected void onDraw(Canvas c){ super.onDraw(c); float w=getWidth(),h=getHeight(),cx=w/2f,cy=h/2f; p.setColor(BLUE); p.setStrokeWidth(dp(3)); p.setStyle(Paint.Style.STROKE);\n            if("↕".equals(kind)){ c.drawLine(cx-7,cy+11,cx-7,cy-11,p); c.drawLine(cx-13,cy-5,cx-7,cy-11,p); c.drawLine(cx-1,cy-5,cx-7,cy-11,p); c.drawLine(cx+7,cy-11,cx+7,cy+11,p); c.drawLine(cx+1,cy+5,cx+7,cy+11,p); c.drawLine(cx+13,cy+5,cx+7,cy+11,p); }\n            else if("●".equals(kind)){ p.setStyle(Paint.Style.FILL); c.drawCircle(cx,cy-4,dp(8),p); Path path=new Path(); path.moveTo(cx-9,cy); path.lineTo(cx,cy+14); path.lineTo(cx+9,cy); path.close(); c.drawPath(path,p); Paint q=new Paint(1); q.setColor(Color.rgb(236,247,255)); c.drawCircle(cx,cy-4,dp(3),q); }\n            else if("▣".equals(kind)){ RectF r=new RectF(cx-11,cy-12,cx+11,cy+10); c.drawRoundRect(r,dp(2),dp(2),p); c.drawLine(cx-7,cy-15,cx-7,cy-11,p); c.drawLine(cx+7,cy-15,cx+7,cy-11,p); c.drawLine(cx-8,cy-5,cx+8,cy-5,p); p.setStyle(Paint.Style.FILL); c.drawCircle(cx-5,cy+4,dp(2),p); c.drawCircle(cx+5,cy+4,dp(2),p); }\n            else if("$".equals(kind)){ p.setStyle(Paint.Style.FILL); p.setTextAlign(Paint.Align.CENTER); p.setTextSize(dp(29)); p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD); c.drawText("$",cx,cy+dp(10),p); }\n            else if("▦".equals(kind)){ RectF r=new RectF(cx-12,cy-10,cx+12,cy+12); c.drawRoundRect(r,dp(2),dp(2),p); c.drawLine(cx-7,cy-14,cx-7,cy-8,p); c.drawLine(cx+7,cy-14,cx+7,cy-8,p); c.drawLine(cx-11,cy-3,cx+11,cy-3,p); }\n            else { p.setStyle(Paint.Style.FILL); c.drawCircle(cx-7,cy-4,dp(6),p); c.drawCircle(cx+6,cy-4,dp(6),p); c.drawRoundRect(new RectF(cx-17,cy+4,cx+17,cy+12),dp(6),dp(6),p); }\n        }\n    }\n\n    private class ReferenceHeroView extends View {\n        private final Paint p=new Paint(1);\n        ReferenceHeroView(){ super(MainActivity.this); setLayerType(View.LAYER_TYPE_SOFTWARE,null); }\n        private void text(Canvas c,String v,float x,float y,float size,int color,boolean bold){ p.setStyle(Paint.Style.FILL); p.setColor(color); p.setTextSize(dp((int)size)); p.setTypeface(bold?android.graphics.Typeface.DEFAULT_BOLD:android.graphics.Typeface.DEFAULT); p.setTextAlign(Paint.Align.LEFT); c.drawText(v,x,y,p); }\n        @Override protected void onDraw(Canvas c){ super.onDraw(c); float w=getWidth(),h=getHeight(); p.setStyle(Paint.Style.FILL); p.setColor(Color.rgb(234,248,255)); c.drawRect(0,0,w,h,p);\n            p.setColor(Color.rgb(216,239,252)); Path m=new Path(); m.moveTo(0,h*.48f); m.lineTo(w*.14f,h*.22f); m.lineTo(w*.24f,h*.44f); m.lineTo(w*.36f,h*.18f); m.lineTo(w*.50f,h*.46f); m.lineTo(w*.62f,h*.28f); m.lineTo(w*.72f,h*.52f); m.lineTo(0,h*.52f); m.close(); c.drawPath(m,p);\n            p.setColor(Color.rgb(199,231,247)); c.drawRect(0,h*.61f,w,h,p);\n            float mx=w*.47f; p.setColor(Color.rgb(22,132,238)); c.drawRect(mx-2,h*.30f,mx+2,h*.55f,p); c.drawRect(mx+48,h*.28f,mx+52,h*.55f,p); c.drawCircle(mx,h*.29f,dp(4),p); c.drawCircle(mx+50,h*.27f,dp(4),p);\n            p.setColor(Color.rgb(245,252,255)); c.drawRect(mx-25,h*.49f,mx+75,h*.63f,p); p.setColor(Color.rgb(27,145,239)); c.drawCircle(mx+25,h*.46f,dp(24),p); c.drawRect(mx+2,h*.46f,mx+48,h*.53f,p);\n            float tx=w*.02f,ty=h*.39f; p.setColor(Color.WHITE); c.drawRoundRect(new RectF(tx,ty,w*.43f,h*.75f),dp(26),dp(26),p); p.setColor(Color.rgb(23,124,236)); c.drawRoundRect(new RectF(tx,h*.61f,w*.43f,h*.75f),dp(20),dp(20),p);\n            p.setColor(Color.rgb(42,78,109)); for(int i=0;i<7;i++) c.drawRoundRect(new RectF(tx+dp(18)+i*dp(17),ty+dp(18),tx+dp(30)+i*dp(17),ty+dp(35)),dp(3),dp(3),p);\n            p.setColor(Color.rgb(46,91,126)); c.drawCircle(tx+dp(48),h*.78f,dp(10),p); c.drawCircle(tx+dp(125),h*.78f,dp(10),p);\n            p.setStrokeWidth(dp(2)); p.setColor(Color.rgb(95,165,210)); c.drawLine(0,h*.81f,w*.60f,h*.81f,p);\n            text(c,"قطار",w*.63f,h*.30f,33,BLUE,true); p.setColor(Color.rgb(231,243,253)); c.drawRoundRect(new RectF(w*.84f,h*.14f,w*.96f,h*.34f),dp(22),dp(22),p); text(c,"V8",w*.87f,h*.27f,17,BLUE,true);\n            text(c,"MASOUD Raja",w*.63f,h*.48f,20,Color.rgb(15,19,31),true); text(c,"سفر بهتر، همیشه نزدیک‌تر",w*.63f,h*.63f,12,Color.rgb(105,119,143),false); p.setColor(Color.rgb(17,170,229)); c.drawRect(w*.79f,h*.70f,w*.94f,h*.72f,p);\n        }\n    }\n\n'''
    s=s[:pos]+helper+s[pos:]

start=s.index('    private FrameLayout iconBubble(String glyph)')
brace=s.index('{',start); depth=0; end=None
for i in range(brace,len(s)):
    if s[i]=='{': depth+=1
    elif s[i]=='}':
        depth-=1
        if depth==0: end=i+1; break
new='''    private FrameLayout iconBubble(String glyph){\n        FrameLayout b=new FrameLayout(this); b.setBackground(rounded(Color.rgb(236,247,255),Color.TRANSPARENT,24)); b.setElevation(dp(2));\n        RefIconView i=new RefIconView(glyph); b.addView(i,new FrameLayout.LayoutParams(-1,-1)); return b;\n    }'''
s=s[:start]+new+s[end:]

old_start='        FrameLayout hero=new FrameLayout(this);'
for old_end in ['        root.addView(hero,new LinearLayout.LayoutParams(-1,dp(155)));','        root.addView(hero,new LinearLayout.LayoutParams(-1,dp(190)));']:
    if old_start in s and old_end in s:
        a=s.index(old_start); b=s.index(old_end,a)+len(old_end)
        s=s[:a]+'''        ReferenceHeroView hero=new ReferenceHeroView();\n        root.addView(hero,new LinearLayout.LayoutParams(-1,dp(168)));'''+s[b:]
        break

p.write_text(s,encoding='utf-8')
