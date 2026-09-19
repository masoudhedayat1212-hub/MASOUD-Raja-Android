from pathlib import Path

original = Path('w13/patch.py').read_text()
original = original.replace('version.setText("W13")', 'version.setText("W14")')
original = original.replace("Path('w13/MainActivity.java').write_text(source)", "Path('w14/MainActivity.java').write_text(source)")

before = '''   let open=null;
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
   }'''
after = '''   let ph=null,pw=null;
   const findFields=()=>{
     let ins=[...document.querySelectorAll('input')].filter(vis);
     pw=ins.find(e=>e.type==='password'||/کلمه عبور|رمز عبور|رمز/.test((e.placeholder||'')+' '+(e.name||'')+' '+(e.getAttribute('aria-label')||'')));
     ph=ins.find(e=>e!==pw&&(e.type==='tel'||/شماره همراه|موبایل|تلفن/.test((e.placeholder||'')+' '+(e.name||'')+' '+(e.getAttribute('aria-label')||''))))||ins.find(e=>e!==pw&&e.type==='text'&&/phone|mobile|user|login/i.test((e.id||'')+' '+(e.name||'')));
     if(!ph&&pw)ph=ins.find(e=>e!==pw&&!['hidden','submit','button'].includes(e.type));
     return !!(ph&&pw);
   };
   if(!findFields()){
     let open=null;
     for(let i=0;i<40&&!open;i++){
       open=options().find(e=>/ورود/.test(text(e))&&(/عضویت|ثبت نام|ثبت‌نام/.test(text(e))||text(e)==='ورود'))||document.querySelector('a[href*=login],a[href*=signin],button[aria-label*=ورود],[title*=ورود]');
       if(!open)await sleep(100);
     }
     if(!open){MasoudBridge.log('فرم یا دکمه ورود پیدا نشد');MasoudBridge.loginMissing();return;}
     open.click();
     for(let i=0;i<60&&!findFields();i++)await sleep(100);
   }'''
assert before in original
original = original.replace(before, after)
original = original.replace("Path('w14/MainActivity.java').write_text(source)", '''source = source.replace(
    'lastFinishedUrl=url; lastFinishedAt=now; log("صفحه باز شد: "+url);',
    'boolean changed=!url.equals(lastFinishedUrl); lastFinishedUrl=url; lastFinishedAt=now; if(changed && loginInProgress){ loginInProgress=false; log("صفحه ورود عوض شد؛ فرم جدید بررسی می‌شود"); } log("صفحه باز شد: "+url);'
)
Path('w14/MainActivity.java').write_text(source)''')
exec(compile(original, 'w14/generated_patch.py', 'exec'))
