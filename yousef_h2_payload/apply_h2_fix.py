from pathlib import Path
p=Path('app/src/main/java/ir/masoud/hamrah/MainActivity.java')
s=p.read_text(encoding='utf-8')
# Legacy H1 shortcut is no longer shown automatically; method remains intact.
s=s.replace('        ensureNotification();\n','',1)
start=s.index('        TextView samples=text("نمونه فرمان‌ها:')
end=s.index('        samples.setLineSpacing(',start)
replacement=r'''        TextView samples=text("نمونه فرمان‌ها:\n«یوسف، با خط یک به علی زنگ بزن»\n«یوسف، یادداشت کن جلسه سه‌شنبه»\n«یوسف، فردا ساعت ۱۰ یادم بنداز تماس بگیرم»\n«یوسف، درباره بازار طلا تحقیق کن»\n\nتحقیق پیشرفته نیازمند کلید API و اتصال اینترنت است. گوش‌دادن مداوم آزمایشی است و ممکن است مصرف باتری و اینترنت داشته باشد.",13,false,0xff8ba6c5);
'''
s=s[:start]+replacement+s[end:]
p.write_text(s,encoding='utf-8')
