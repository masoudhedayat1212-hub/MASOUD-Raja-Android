(async function(){
if(window.__masoudSessionCancel)window.__masoudSessionCancel();
const cfg=__CONFIG__; let cancelled=false;
window.__masoudSessionCancel=()=>{cancelled=true;};
const norm=s=>String(s||'').replace(/[\u200c\u200e\u200f]/g,' ').replace(/ي/g,'ی').replace(/ك/g,'ک').replace(/\s+/g,' ').trim();
const text=e=>norm(e&&(e.innerText||e.textContent));
const vis=e=>e&&e.getClientRects().length>0&&getComputedStyle(e).visibility!=='hidden';
const nodes=sel=>[...document.querySelectorAll(sel)].filter(vis);
const sleep=ms=>new Promise(r=>setTimeout(r,ms));
const wait=async(fn,ms=12000)=>{let end=Date.now()+ms;while(!cancelled&&Date.now()<end){let v=fn();if(v)return v;await sleep(100);}return null;};
const fail=m=>{if(!cancelled)MasoudBridge.moduleFailed(cfg.token,m);};
const loginButton=()=>nodes('a,button,span,div').filter(e=>/^ورود\s*\/\s*عضویت$/.test(text(e))).sort((a,b)=>a.querySelectorAll('*').length-b.querySelectorAll('*').length)[0];
const accountReady=()=>!loginButton()&&!nodes('input[type=password]').length&&nodes('a,button,li,span').some(e=>/^(خروج|خروج از حساب|پروفایل شخصی|اطلاعات مشتریان)$/.test(text(e)));
let openedAccount=false;
const inspectAccount=()=>{if(accountReady())return true;if(!loginButton()&&!nodes('input[type=password]').length&&!openedAccount){let t=document.querySelector('#dropdownForm1');if(vis(t)&&text(t)&&!/ورود|عضویت/.test(text(t))){openedAccount=true;t.click();}}return accountReady();};
function form(){
for(const pwd of nodes('input[type=password]')){
 for(let box=pwd.parentElement;box&&box!==document.body;box=box.parentElement){
  const inputs=[...box.querySelectorAll('input')].filter(vis);
  const mobile=inputs.find(e=>e!==pwd&&!['password','hidden','checkbox','radio','submit','button'].includes(e.type));
  const submit=[...box.querySelectorAll('button,input[type=submit]')].filter(vis).find(e=>text(e)==='ورود'||e.value==='ورود');
  if(mobile&&submit)return {box,mobile,pwd,submit};
 }
}return null;}
try{
let state=await wait(()=>inspectAccount()?'account':form()?'form':loginButton()?'button':null);
if(!state){fail('ورود / عضویت یا حساب فعال آماده نشد');return;}
if(state==='account'){MasoudBridge.loginReady(cfg.token);return;}
if(state==='button'){loginButton().click();await sleep(700);}
let f=await wait(form);if(!f){fail('فرم شماره و رمز ورود آماده نشد');return;}
const set=(e,value)=>{const d=Object.getOwnPropertyDescriptor(HTMLInputElement.prototype,'value');d.set.call(e,value);e.dispatchEvent(new Event('input',{bubbles:true}));e.dispatchEvent(new Event('change',{bubbles:true}));};
set(f.mobile,cfg.phone);set(f.pwd,cfg.password);
const filled=await wait(()=>{let n=form();return n&&n.mobile.value===cfg.phone&&n.pwd.value===cfg.password&&n;},2000);
if(!filled){fail('شماره یا رمز در فرم ورود ثبت نشد');return;}
if(cancelled)return;
if(filled.submit.disabled||filled.submit.getAttribute('aria-disabled')==='true'){fail('دکمه ورود هنوز فعال نیست');return;}
filled.submit.click();MasoudBridge.loginSent(cfg.token);
await sleep(2600);
if(await wait(inspectAccount,15000)){if(!cancelled)MasoudBridge.loginReady(cfg.token);}
else fail('ورود به حساب تأیید نشد؛ مراحل جستجو اجرا نشد');
}catch(e){fail('خطای ورود: '+e.message);}
})();