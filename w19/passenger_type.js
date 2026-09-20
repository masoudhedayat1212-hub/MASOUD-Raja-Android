const canonical=s=>String(s||'').replace(/[\u200c\u200e\u200f]/g,' ').replace(/ي/g,'ی').replace(/ك/g,'ک').replace(/مسافرین/g,'مسافران').replace(/\s+/g,' ').trim();
const typeNames=['مسافران عادی','ویژه برادران','ویژه خواهران'];
const selected=e=>canonical(e.tagName==='SELECT'?e.options[e.selectedIndex]?.textContent:text(e));
const findType=()=>{
let native=[...document.querySelectorAll('select')].filter(vis).find(e=>[...e.options].filter(o=>typeNames.includes(canonical(o.textContent))).length>=2);if(native)return native;
let leaf=[...document.querySelectorAll('ng-select,.ng-value-label,.ng-select-container,[role=combobox],button,a,div,span,label')].filter(vis).filter(e=>typeNames.includes(canonical(text(e)))&&!e.closest('ng-dropdown-panel,[role=listbox],.dropdown-menu')).sort((a,b)=>a.querySelectorAll('*').length-b.querySelectorAll('*').length)[0];
return leaf&&(leaf.closest('ng-select,button,a,[role=combobox],[data-toggle],.ng-select-container')||leaf);
};
let typeControl=await wait(findType);if(!typeControl){fail('کادر نوع مسافر در رجا پیدا نشد');return;}
typeControl.scrollIntoView({block:'center',behavior:'instant'});
if(selected(typeControl)!==wanted){
if(typeControl.tagName==='SELECT'){let option=[...typeControl.options].find(o=>canonical(o.textContent)===wanted);if(!option||option.disabled){fail('گزینه نوع مسافر موجود نیست');return;}typeControl.value=option.value;fire(typeControl);}
else {typeControl.click();let choice=await wait(()=>[...document.querySelectorAll('ng-dropdown-panel .ng-option,[role=option],.dropdown-menu a,.dropdown-menu button,.dropdown-menu li,li,a,button,span')].filter(vis).filter(e=>e!==typeControl&&!typeControl.contains(e)&&canonical(text(e))===wanted).sort((a,b)=>a.querySelectorAll('*').length-b.querySelectorAll('*').length)[0]);if(!choice){fail('گزینه نوع مسافر پیدا نشد: '+wanted);return;}(choice.closest('.ng-option,[role=option],a,button,li')||choice).click();}
}
if(!await wait(()=>{const current=findType();return current&&selected(current)===wanted;})){fail('انتخاب نوع مسافر تأیید نشد: '+wanted);return;}
MasoudBridge.log('نوع مسافر در رجا تأیید شد: '+wanted);