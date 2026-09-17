from pathlib import Path
import re
p=Path('buildsrc/MASOUD_Android/app/src/main/java/com/masoud/raja/MainActivity.java')
s=p.read_text(encoding='utf-8')
s=s.replace('private static final String VERSION = "V5"','private static final String VERSION = "V6"')
marker='    private void injectSearchForm()'
if marker not in s: raise SystemExit('injectSearchForm marker missing')
fixed='''    private void injectLoginFixed(){
        injectLogin();
        handler.postDelayed(()->web.evaluateJavascript("(function(){try{var vis=function(e){if(!e)return false;var r=e.getBoundingClientRect(),st=getComputedStyle(e);return r.width>0&&r.height>0&&st.display!='none'&&st.visibility!='hidden';};var els=[].slice.call(document.querySelectorAll('button,a,[role=button]'));var b=els.find(function(e){var t=(e.innerText||e.textContent||'').replace(/\\\\s+/g,' ').trim();return vis(e)&&(t==='ورود'||t.indexOf('ورود به حساب')>=0||t.indexOf('ورود / ثبت نام')>=0||t.indexOf('ورود/ثبت نام')>=0);});if(b){b.click();return 'LOGIN_FALLBACK_CLICKED';}return 'LOGIN_BUTTON_NOT_FOUND';}catch(e){return 'LOGIN_FALLBACK_ERROR:'+e.message;}})();",null),900);
    }

'''
s=s.replace(marker,fixed+marker,1)
idx=s.find('private void injectLogin()')
if idx<0: raise SystemExit('injectLogin missing')
before,after=s[:idx],s[idx:]
if 'injectLogin();' not in before: raise SystemExit('injectLogin call site missing')
before=before.replace('injectLogin();','injectLoginFixed();')
p.write_text(before+after,encoding='utf-8')

manifest=Path('buildsrc/MASOUD_Android/app/src/main/AndroidManifest.xml')
m=manifest.read_text(encoding='utf-8')
m=m.replace('android:label="MASOUD"','android:label="MASOUD Raja"')
if 'android:icon=' not in m:
    m=m.replace('<application ','<application android:icon="@drawable/ic_train" android:roundIcon="@drawable/ic_train" ',1)
manifest.write_text(m,encoding='utf-8')

gradle=Path('buildsrc/MASOUD_Android/app/build.gradle')
g=gradle.read_text(encoding='utf-8')
g=re.sub(r'versionCode\s+\d+','versionCode 6',g)
g=re.sub(r"versionName\s+'[^']+'","versionName '6.0.0'",g)
gradle.write_text(g,encoding='utf-8')
