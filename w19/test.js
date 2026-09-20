const fs=require('fs'),assert=require('node:assert/strict');
const {JSDOM}=require('jsdom');
const login=fs.readFileSync('w19/login.js','utf8');
const type=fs.readFileSync('w19/passenger_type.js','utf8');
function env(html){
 const d=new JSDOM(html,{runScripts:'outside-only',url:'https://www.raja.ir/'}),w=d.window;
 Object.defineProperty(w.HTMLElement.prototype,'innerText',{get(){return this.textContent}});
 w.HTMLElement.prototype.getClientRects=function(){return this.closest('[hidden]')?[]:[{}]};
 w.HTMLElement.prototype.scrollIntoView=function(){};
 // Accelerate bounded waits, retaining asynchronous behavior.
 let clock=0;w.Date.now=()=>clock;
 w.setTimeout=fn=>setTimeout(()=>{clock+=1000;fn()},0);
 w.events=[];w.MasoudBridge={log:()=>{},moduleFailed:(t,m)=>w.events.push(['fail',m]),loginReady:t=>w.events.push(['ready',t]),loginSent:t=>w.events.push(['sent',t])};
 return w;
}
async function loginCase(success,existing=false,cancel=false){
 const w=env(`<input id="unrelated" value="keep"><a id="dropdownForm1">ورود / عضویت</a><form hidden><input id="phone"><input type="password"><button type="button">ورود</button></form><a id="logout" hidden>خروج</a>`);
 const open=w.document.querySelector('a'),form=w.document.querySelector('form'),logout=w.document.querySelector('#logout');
 open.onclick=()=>form.hidden=false;
 form.querySelector('button').onclick=()=>{assert.equal(form.querySelector('#phone').value,'09000000000');assert.equal(form.querySelector('[type=password]').value,'test');if(success){form.hidden=true;open.hidden=true;logout.hidden=false;}};
 if(existing){open.hidden=true;logout.hidden=false;}
 const job=w.eval(login.replace('__CONFIG__',JSON.stringify({token:7,phone:'09000000000',password:'test'})));
 if(cancel)w.__masoudSessionCancel();
 await job;
 assert.equal(w.document.querySelector('#unrelated').value,'keep');
 if(cancel)assert(!w.events.some(e=>e[0]==='ready'));
 else if(success||existing)assert(w.events.some(e=>e[0]==='ready'));
 else {assert(w.events.some(e=>e[0]==='fail'));assert(!w.events.some(e=>e[0]==='ready'));}
 w.close();
}
async function typeCase(native,wanted,ignore=false){
 const w=env(native?'<select><option value="0">مسافرین عادی</option><option value="1">ویژه برادران</option><option value="2">ویژه خواهران</option></select>':'<a id="type"><span>مسافرین عادی</span></a><ul class="dropdown-menu" hidden><li><a>ویژه برادران</a></li><li><a>ویژه خواهران</a></li><li><a>مسافرین عادی</a></li></ul>');
 if(!native){let toggle=w.document.querySelector('#type'),menu=w.document.querySelector('ul');toggle.onclick=()=>menu.hidden=false;for(const option of menu.querySelectorAll('a'))option.onclick=()=>{if(!ignore)toggle.querySelector('span').textContent=option.textContent;menu.hidden=true;};}
 w.wanted=wanted;
 const prefix=`const vis=e=>e&&e.getClientRects().length>0;const text=e=>e.innerText||e.textContent||'';const fire=e=>e.dispatchEvent(new Event('change',{bubbles:true}));const wait=async fn=>{for(let i=0;i<3;i++){const r=fn();if(r)return r;await new Promise(r=>setTimeout(r,1));}return null;};const fail=m=>events.push(['fail',m]);`;
 await w.eval('(async function(){'+prefix+type+'events.push(["done"]);})();');
 assert.equal(w.events.some(e=>e[0]==='done'),!ignore);
 if(ignore)assert(w.events.some(e=>e[0]==='fail'));
 w.close();
}
(async()=>{
 await loginCase(true);await loginCase(false);await loginCase(false,true);await loginCase(false,false,true);
 for(const native of [true,false])for(const wanted of ['مسافران عادی','ویژه برادران','ویژه خواهران'])await typeCase(native,wanted);
 await typeCase(false,'ویژه برادران',true);
 console.log('11 regression scenarios passed: scoped login, rejected login, existing session, cancellation, three passenger types, failed selection.');
})().catch(e=>{console.error(e);process.exitCode=1});
