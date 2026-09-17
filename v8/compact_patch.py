from pathlib import Path
p=Path('buildsrc/MASOUD_Android/app/src/main/java/com/masoud/raja/MainActivity.java')
s=p.read_text(encoding='utf-8')
repls={
'new FrameLayout.LayoutParams(dp(50),dp(50),Gravity.LEFT|Gravity.CENTER_VERTICAL)':'new FrameLayout.LayoutParams(dp(42),dp(42),Gravity.LEFT|Gravity.CENTER_VERTICAL)',
'bp.leftMargin=dp(12)':'bp.leftMargin=dp(10)',
'txt(glyph,27,BLUE':'txt(glyph,23,BLUE',
'center.setPadding(0,dp(6),0,dp(5))':'center.setPadding(0,dp(3),0,dp(3))',
'TextView lab=txt(labelText,13,MUTED':'TextView lab=txt(labelText,12,MUTED',
'new LinearLayout.LayoutParams(-1,dp(20))':'new LinearLayout.LayoutParams(-1,dp(17))',
'new LinearLayout.LayoutParams(-1,dp(34))':'new LinearLayout.LayoutParams(-1,dp(29))',
'cp.leftMargin=dp(74)':'cp.leftMargin=dp(62)',
'cp.rightMargin=dp(42)':'cp.rightMargin=dp(34)',
'txt("›",34':'txt("›",30',
'new FrameLayout.LayoutParams(dp(34),-1':'new FrameLayout.LayoutParams(dp(30),-1',
'ap.rightMargin=dp(6)':'ap.rightMargin=dp(4)',
'return txt(text,14,TEXT':'return txt(text,13,TEXT',
'e.setTextSize(20)':'e.setTextSize(17)',
'b.setTextSize(18)':'b.setTextSize(16)',
'b.setMinHeight(dp(52))':'b.setMinHeight(dp(44))',
'root.addView(hero,new LinearLayout.LayoutParams(-1,dp(190)))':'root.addView(hero,new LinearLayout.LayoutParams(-1,dp(155)))',
'tabs.setPadding(dp(18),dp(8),dp(18),dp(8))':'tabs.setPadding(dp(18),dp(5),dp(18),dp(5))',
'new LinearLayout.LayoutParams(0,dp(54),1)':'new LinearLayout.LayoutParams(0,dp(46),1)',
'root.addView(tabs,new LinearLayout.LayoutParams(-1,dp(70)))':'root.addView(tabs,new LinearLayout.LayoutParams(-1,dp(58)))',
'p.setPadding(dp(18),dp(2),dp(18),dp(22))':'p.setPadding(dp(18),dp(1),dp(18),dp(8))',
'new LinearLayout.LayoutParams(-1,dp(72))':'new LinearLayout.LayoutParams(-1,dp(58))',
'gap(p,7)':'gap(p,4)',
'tickRow.addView(priceMode,new LinearLayout.LayoutParams(dp(42),dp(34)))':'tickRow.addView(priceMode,new LinearLayout.LayoutParams(dp(38),dp(27)))',
'p.addView(tickRow,new LinearLayout.LayoutParams(-1,dp(36)))':'p.addView(tickRow,new LinearLayout.LayoutParams(-1,dp(28)))',
'gap(p,12)':'gap(p,6)',
'startBtn.setTextSize(19)':'startBtn.setTextSize(17)',
'p.addView(startBtn,new LinearLayout.LayoutParams(-1,dp(58)))':'p.addView(startBtn,new LinearLayout.LayoutParams(-1,dp(50)))',
'private TextView refValue(String value){ TextView t=txt(value,20':'private TextView refValue(String value){ TextView t=txt(value,18'
}
for a,b in repls.items():
    s=s.replace(a,b)
p.write_text(s,encoding='utf-8')
