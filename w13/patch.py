from pathlib import Path
import json
import re

source = Path('w2/MainActivity.java').read_text()
source = source.replace('version.setText("W12")', 'version.setText("W13")')

def java_js(js):
    return json.dumps(js, ensure_ascii=False)

logout_js = r'''(async function(){
 const sleep=ms=>new Promise(r=>setTimeout(r,ms));
 const text=e=>String((e&&(e.innerText||e.textContent))||'').replace(/\s+/g,' ').trim();
 const vis=e=>e&&e.getClientRects().length>0;
 const options=()=>[...document.querySelectorAll('a,button,[role=button]')].filter(vis);
 const exit=()=>options().find(e=>/^(خروج|خروج از حساب)$/.test(text(e)));
 try{
   let out=exit();
   if(!out){
     let account=options().filter(e=>/^(حساب کاربری|پروفایل|پروفایل شخصی)$/.test(text(e))||/profile|account/i.test((e.getAttribute('href')||'')+' '+(e.getAttribute('aria-label')||''))).sort((a,b)=>text(a).length-text(b).length)[0];
     if(account){account.click();for(let i=0;i<30&&!out;i++){await sleep(100);out=exit();}}
   }
   if(!out){MasoudBridge.log('نشست فعال رجا تأیید نشد؛ فرم ورود بررسی می‌شود');MasoudBridge.logoutDone();return;}
   out.click();
   for(let i=0;i<60;i++){
     await sleep(100);
     if(options().some(e=>/ورود\s*\/\s*عضویت|^ورود$/.test(text(e)))&&!exit()){
       MasoudBridge.log('خروج از حساب رجا تأیید شد');MasoudBridge.logoutDone();return;
     }
   }
   MasoudBridge.selectionError('خروج از حساب رجا تأیید نشد');
 }catch(e){MasoudBridge.selectionError('خطای خروج از رجا: '+e);}
})();'''

login_js = r'''(async function(){
 const sleep=ms=>new Promise(r=>setTimeout(r,ms));
 const text=e=>String((e&&(e.innerText||e.textContent||e.value))||'').replace(/\s+/g,' ').trim();
 const vis=e=>e&&e.getClientRects().length>0;
 const options=()=>[...document.querySelectorAll('a,button,[role=button]')].filter(vis);
 const exit=()=>options().find(e=>/^(خروج|خروج از حساب)$/.test(text(e)));
 const account=()=>options().filter(e=>/^(حساب کاربری|پروفایل|پروفایل شخصی)$/.test(text(e))||/profile|account/i.test((e.getAttribute('href')||'')+' '+(e.getAttribute('aria-label')||''))).sort((a,b)=>text(a).length-text(b).length)[0];
 const authenticated=async()=>{let menu=account();if(menu){menu.click();await sleep(200);}return !!exit();};
 const setv=(e,v)=>{let d=Object.getOwnPropertyDescriptor(HTMLInputElement.prototype,'value');if(d&&d.set)d.set.call(e,v);else e.value=v;e.dispatchEvent(new InputEvent('input',{bubbles:true,inputType:'insertText',data:v}));e.dispatchEvent(new Event('change',{bubbles:true}));e.dispatchEvent(new Event('blur',{bubbles:true}));};
 try{
   if(await authenticated()){MasoudBridge.loginClicked();return;}
   let open=null;
   for(let i=0;i<40&&!open;i++){
     open=options().find(e=>/ورود/.test(text(e))&&(/عضویت|ثبت نام|ثبت‌نام/.test(text(e))||text(e)==='ورود'))||document.querySelector('a[href*=login],a[href*=signin],button[aria-label*=ورود],[title*=ورود]');
     if(!open)await sleep(100);
   }
   if(!open){MasoudBridge.log('دکمه ورود/عضویت پیدا نشد');MasoudBridge.loginMissing();return;}
   open.click();let ph=null,pw=null;
   for(let i=0;i<60&&(!ph||!pw);i++){
     let ins=[...document.querySelectorAll('input')].filter(vis);
     pw=ins.find(e=>e.type==='password'||/کلمه عبور|رمز عبور|رمز/.test((e.placeholder||'')+' '+(e.name||'')+' '+(e.getAttribute('aria-label')||'')));
     ph=ins.find(e=>e!==pw&&(e.type==='tel'||/شماره همراه|موبایل|تلفن/.test((e.placeholder||'')+' '+(e.name||'')+' '+(e.getAttribute('aria-label')||''))))||ins.find(e=>e!==pw&&!['hidden','submit','button'].includes(e.type));
     if(!ph||!pw)await sleep(100);
   }
   if(!ph||!pw){MasoudBridge.log('فیلد شماره موبایل یا رمز پیدا نشد');MasoudBridge.loginMissing();return;}
   let phone=PHONE_VALUE,password=PASSWORD_VALUE;
   ph.focus();setv(ph,phone);await sleep(300);pw.focus();setv(pw,password);await sleep(300);
   if(String(ph.value).replace(/\s/g,'')!==phone.replace(/\s/g,'')||String(pw.value)!==password){MasoudBridge.log('شماره موبایل یا رمز داخل فرم ثبت نشد');MasoudBridge.loginMissing();return;}
   MasoudBridge.log('شماره موبایل و رمز داخل فرم رجا ثبت شد');
   let button=options().filter(e=>text(e)==='ورود').sort((a,b)=>text(a).length-text(b).length)[0];
   if(!button){MasoudBridge.log('دکمه تأیید ورود پیدا نشد');MasoudBridge.loginMissing();return;}
   button.click();
   for(let i=0;i<100;i++){
     await sleep(100);
     if(await authenticated()){MasoudBridge.loginClicked();return;}
   }
   MasoudBridge.log('ورود حساب رجا تأیید نشد');MasoudBridge.loginMissing();
 }catch(e){MasoudBridge.log('خطای ورود: '+e);MasoudBridge.loginMissing();}
})();'''

logout_pattern = r'(    private void injectLogout\(\)\{.*?\n        String js=).*?(;\n        webView\.evaluateJavascript\(js,null\);\n    \})'
source, count = re.subn(logout_pattern, lambda m: m[1]+java_js(logout_js)+m[2], source, count=1, flags=re.S)
assert count == 1
login_pattern = r'(    private void injectLogin\(\)\{.*?\n        String js=).*?(;\n        webView\.evaluateJavascript\(js,null\);\n    \})'
login_expr = '"(async function(){"'  # replaced below with a Java concatenation of safe literal pieces
parts = login_js.split('PHONE_VALUE')
assert len(parts)==2
parts2 = parts[1].split('PASSWORD_VALUE')
assert len(parts2)==2
login_expr = java_js(parts[0])+'+q(phone.getText().toString().trim())+'+java_js(parts2[0])+'+q(password.getText().toString())+'+java_js(parts2[1])
source, count = re.subn(login_pattern, lambda m: m[1]+login_expr+m[2], source, count=1, flags=re.S)
assert count == 1

old = '"let wanted="+q(passengerTypeLabel())+' 
begin = source.index(old, source.index('private void injectSearchFormV11()'))
end = source.index('\n            +(""+(coupe.isChecked()', begin)
replacement = r'''"let wanted="+q(passengerTypeLabel())+";let typeControl=await wait(()=>{let exact=[...document.querySelectorAll('ng-select,.ng-select-container,[role=combobox],button,div,span')].filter(vis).filter(x=>['مسافران عادی','ویژه برادران','ویژه خواهران'].includes(text(x))).sort((a,b)=>text(a).length-text(b).length)[0];return exact&&(exact.closest('ng-select,[role=combobox],button,.ng-select-container')||exact);});if(!typeControl){fail('کادر نوع مسافر در رجا پیدا نشد');return;}typeControl.scrollIntoView({block:'center',behavior:'instant'});await sleep(300);typeControl.click();let choice=await wait(()=>[...document.querySelectorAll('ng-dropdown-panel .ng-option,[role=option]')].filter(vis).find(x=>text(x)===wanted));if(!choice){fail('گزینه نوع مسافر پیدا نشد: '+wanted);return;}choice.scrollIntoView({block:'nearest',behavior:'instant'});choice.click();await sleep(350);if(!text(typeControl).includes(wanted)){fail('انتخاب نوع مسافر تأیید نشد: '+wanted);return;}MasoudBridge.log('نوع مسافر در رجا تأیید شد: '+wanted);"'''
source = source[:begin] + replacement + source[end:]
source = source.replace('log("خروج انجام شد؛ ورود مجدد...")', 'log("بررسی نشست پایان یافت؛ ورود حساب رجا...")')
Path('w13/MainActivity.java').write_text(source)
