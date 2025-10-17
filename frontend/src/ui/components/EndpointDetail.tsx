import type { EndpointVM, InputVM } from "../types";
import { useEffect, useMemo, useState } from "react";

export function EndpointDetail({ endpoint }: { endpoint: EndpointVM }) {
    const storageKey = `autoUi.form.${endpoint.id ?? endpoint.paths?.[0] ?? "default"}`;

    const [form, setForm] = useState<Record<string, any>>(() => {
        try { return JSON.parse(localStorage.getItem(storageKey) || "{}"); } catch { return {}; }
    });
    const [token, setToken] = useState<string>(() => localStorage.getItem("autoUi.token") || "");
    const [loading, setLoading] = useState(false);
    const [resp, setResp] = useState<ResponseState | null>(null);
    const [activeTab, setActiveTab] = useState<TabKey>("preview");

    const pathTemplate = endpoint.paths?.[0] || "/";

    const pathVars   = useMemo(() => endpoint.inputs.filter(i => i.source==="PATH_VARIABLE"), [endpoint.inputs]);
    const queryVars  = useMemo(() => endpoint.inputs.filter(i => i.source==="REQUEST_PARAM"), [endpoint.inputs]);
    const headerVars = useMemo(() => endpoint.inputs.filter(i => i.source==="REQUEST_HEADER"), [endpoint.inputs]);
    const bodyVar    = useMemo(() => endpoint.inputs.find(i => i.source==="REQUEST_BODY") || null, [endpoint.inputs]);

    function setVal(name: string, value: any){
        setForm(prev=>{
            const next = {...prev, [name]: value};
            try{ localStorage.setItem(storageKey, JSON.stringify(next)); }catch{}
            return next;
        });
    }

    useEffect(()=>{ try{ localStorage.setItem("autoUi.token", token); }catch{} }, [token]);

    useEffect(()=>{
        function onKey(e: KeyboardEvent){
            const meta = e.ctrlKey || e.metaKey;
            if(meta && e.key.toLowerCase()==="enter"){ e.preventDefault(); send(); }
            if(e.key==="Escape") setResp(null);
        }
        window.addEventListener("keydown", onKey);
        return ()=>window.removeEventListener("keydown", onKey);
        // eslint-disable-next-line
    },[]);

    const requiredMissing = useMemo(()=>{
        const all = [...pathVars, ...queryVars, ...headerVars];
        return all.some(v => v.required && (form[v.name]==null || String(form[v.name])===""));
    },[form, pathVars, queryVars, headerVars]);

    async function send(){
        setLoading(true); setResp(null);
        const t0 = performance.now();
        try{
            const url = buildUrl(pathTemplate, pathVars, queryVars, form);
            const init = buildRequestInit(endpoint.httpMethod, headerVars, bodyVar, form, endpoint.consumes, endpoint.headers, token);
            const r = await fetch(url, init);
            const ms = Math.round(performance.now()-t0);

            const headers: Record<string,string> = {};
            r.headers.forEach((v,k)=>{ headers[k]=v; });

            const ct = r.headers.get("content-type")||"";
            const json = ct.indexOf("application/json")!==-1;
            const body = json ? await r.json().catch(()=>r.text()) : await r.text();

            setResp({status:r.status, ok:r.ok, timeMs:ms, headers, body});
        }catch(e:any){
            setResp({status:0, ok:false, timeMs:Math.round(performance.now()-t0), headers:{}, body:String(e?.message??e)});
        }finally{ setLoading(false); }
    }

    const curl = useMemo(
        ()=>makeCurl(endpoint, pathTemplate, pathVars, queryVars, headerVars, bodyVar, form, token),
        // eslint-disable-next-line
        [endpoint, pathTemplate, pathVars, queryVars, headerVars, bodyVar, form, token]
    );

    return (
        <div className="max-w-6xl mx-auto space-y-6">
            {/* TOP LINE */}
            <header className="flex flex-wrap items-center gap-3">
                <span className={`text-xs font-semibold px-2 py-0.5 rounded ${methodBadge(endpoint.httpMethod)}`}>{endpoint.httpMethod}</span>
                <h2 className="text-xl font-semibold text-[var(--text)]">{pathTemplate}</h2>
                <div className="ml-auto flex flex-wrap items-center gap-2">
                    {!!endpoint.produces?.length && <span className="badge">produces: {endpoint.produces[0]}</span>}
                    {!!endpoint.consumes?.length && <span className="badge">consumes: {endpoint.consumes[0]}</span>}
                    {!!endpoint.headers?.length  && <span className="badge">headers: {endpoint.headers.join(", ")}</span>}
                </div>
            </header>

            {/* AUTH */}
            <div className="card">
                <div className="flex items-center gap-3">
                    <div className="text-xs font-medium text-muted">Authorization</div>
                    <input className="input" placeholder="Bearer eyJ...  (or any header value)" value={token} onChange={e=>setToken(e.target.value)} />
                </div>
            </div>

            {/* GRID */}
            <section className="grid md:grid-cols-2 gap-6">
                <div className="space-y-6">
                    {pathVars.length>0 && (
                        <Section title="Path variables">
                            {pathVars.map(v=> <InputField key={v.name} v={v} value={form[v.name]} onChange={setVal} />)}
                        </Section>
                    )}
                    {queryVars.length>0 && (
                        <Section title="Query params">
                            {queryVars.map(v=> <InputField key={v.name} v={v} value={form[v.name]} onChange={setVal} />)}
                        </Section>
                    )}
                    {headerVars.length>0 && (
                        <Section title="Headers">
                            {headerVars.map(v=> <InputField key={v.name} v={v} value={form[v.name]} onChange={setVal} />)}
                        </Section>
                    )}
                </div>

                <div className="space-y-6">
                    <Section title={`Body${bodyVar?.type ? ` (${bodyVar.type})` : ""}`}>
                        {bodyVar ? (
                            <TextAreaJson
                                name={bodyVar.name}
                                value={form[bodyVar.name] ?? (bodyVar as any).exampleJson ?? ""}
                                onChange={v=>setVal(bodyVar.name, v)}
                                placeholder={(bodyVar as any).exampleJson ?? '{\n  "id": 0,\n  "name": ""\n}'}
                            />
                        ) : (
                            <div className="h-52 grid place-items-center text-muted">No body</div>
                        )}
                    </Section>
                </div>
            </section>

            {/* ACTIONS */}
            <div className="flex flex-wrap items-center gap-3">
                <button onClick={send} disabled={loading||requiredMissing} className="btn-primary disabled:opacity-50" title="Ctrl/Cmd + Enter">
                    {loading ? "Sending…" : "Send"}
                </button>
                <CopyButton label="Copy cURL" text={curl}/>
                {requiredMissing && <span className="text-xs text-red-500">Fill required fields</span>}
            </div>

            {/* RESPONSE */}
            {resp && (
                <div className="space-y-3">
                    <div className={`h-1 rounded ${resp.ok ? "bg-emerald-500/80" : "bg-rose-500/80"}`} />
                    <div className="card">
                        <div className="flex flex-wrap items-center gap-3 text-sm mb-3">
                            <span className="badge">Status: {resp.status}</span>
                            <span className="badge">Time: {resp.timeMs}ms</span>
                            <span className={`px-2 py-0.5 rounded text-sm ${resp.ok ? "bg-emerald-100 text-emerald-700" : "bg-rose-100 text-rose-700"}`}>
                {resp.ok ? "OK" : "Error"}
              </span>
                        </div>

                        <Tabs active={activeTab} onChange={setActiveTab}/>
                        {activeTab==="headers" ? (
                            <Pre>{JSON.stringify(resp.headers, null, 2)}</Pre>
                        ) : (
                            <Pre>{activeTab==="raw" ? String(resp.body) : formatBody(resp.body)}</Pre>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}

/* ===== small UI atoms ===== */
function Section({title, children}:{title:string; children:React.ReactNode}){
    return (
        <div className="card">
            <div className="field-title">{title}</div>
            <div className="space-y-4">{children}</div>
        </div>
    );
}
function Tabs({active, onChange}:{active:TabKey; onChange:(t:TabKey)=>void}){
    const tabs:TabKey[]=["preview","json","raw","headers"];
    return (
        <div className="flex items-center gap-2 mb-3">
            {tabs.map(t=>(
                <button key={t} onClick={()=>onChange(t)} className={`tab ${active===t?"tab-active":""}`}>
                    {t.toUpperCase()}
                </button>
            ))}
        </div>
    );
}
function InputField({v, value, onChange}:{v:InputVM; value:any; onChange:(k:string,v:any)=>void}){
    const t = pickInputType(v.type);
    if(t==="checkbox"){
        const checked = !!value;
        return (
            <label className="flex items-center gap-2">
                <input type="checkbox" checked={checked} onChange={e=>onChange(v.name, e.target.checked)} className="w-4 h-4"/>
                <span className="text-sm">{v.name} {v.required && <span className="text-rose-500">*</span>}</span>
            </label>
        );
    }
    const invalid = v.required && (value==null || String(value)==="");
    return (
        <label className="block">
            <div className="text-[12px] text-muted mb-1">
                {v.name} {v.required && <span className="text-rose-500">*</span>}
            </div>
            <input
                type={t}
                value={value ?? ""}
                onChange={e=>onChange(v.name, coerceValue(t, e.target.value))}
                placeholder={v.defaultValue ?? ""}
                className={`input ${invalid ? "border-rose-300" : ""}`}
            />
            {invalid && <div className="text-[11px] text-rose-500 mt-1">Required</div>}
        </label>
    );
}
function TextAreaJson({name,value,onChange,placeholder}:{name:string; value:string; onChange:(v:string)=>void; placeholder?:string;}){
    return (
        <textarea
            className="input h-64 font-mono"
            spellCheck={false}
            placeholder={placeholder}
            value={value}
            onChange={e=>onChange(e.target.value)}
        />
    );
}
function Pre({children}:{children:React.ReactNode}){
    return <pre className="input whitespace-pre-wrap overflow-auto font-mono text-[13px]">{children}</pre>;
}

/* ===== behavior utils & types ===== */
type ResponseState = { status:number; ok:boolean; timeMs:number; headers:Record<string,string>; body:unknown };
type TabKey = "preview" | "json" | "raw" | "headers";
function methodBadge(m:string){
    switch(m){
        case "GET": return "bg-method-get text-white";
        case "POST": return "bg-method-post text-white";
        case "PUT": return "bg-method-put text-white";
        case "DELETE": return "bg-method-delete text-white";
        case "PATCH": return "bg-method-put text-white";
        default: return "bg-zinc-200 text-zinc-700";
    }
}
function pickInputType(t?:string|null):"text"|"number"|"checkbox"|"date"{
    if(!t) return "text";
    const s=t.toLowerCase();
    if(["int","integer","long","short","double","float","bigdecimal"].some(k=>s.indexOf(k)!==-1)) return "number";
    if(["bool","boolean"].some(k=>s.indexOf(k)!==-1)) return "checkbox";
    if(["date","localdate","instant"].some(k=>s.indexOf(k)!==-1)) return "date";
    return "text";
}
function coerceValue(kind:"text"|"number"|"checkbox"|"date", v:any){
    if(kind==="number"){
        if(v===""||v==null||typeof v==="undefined") return undefined;
        const n=Number(v); return Number.isNaN(n)?undefined:n;
    }
    return v;
}
function buildUrl(path:string, pathVars:InputVM[], queryVars:InputVM[], form:Record<string,any>){
    let p=path;
    for(const v of pathVars){ p=p.replace(`{${v.name}}`, encodeURIComponent(String(form[v.name]??""))); }
    const qs:string[]=[];
    for(const v of queryVars){
        const val=form[v.name];
        if(val!=null && String(val)!=="") qs.push(`${encodeURIComponent(v.name)}=${encodeURIComponent(String(val))}`);
    }
    if(qs.length>0) p+=(p.indexOf("?")!==-1?"&":"?")+qs.join("&");
    return p;
}
function buildRequestInit(
    method:EndpointVM["httpMethod"],
    headerVars:InputVM[],
    bodyVar:InputVM|null,
    form:Record<string,any>,
    consumes?:string[],
    fixedHeaders?:string[],
    token?:string
):RequestInit{
    const headers:Record<string,string>={};

    if(fixedHeaders?.length){
        fixedHeaders.forEach(raw=>{
            const s=(raw||"").trim(); if(!s||s[0]==="!") return;
            const eq=s.indexOf("="); if(eq>-1){ const k=s.slice(0,eq).trim(); const v=s.slice(eq+1).trim(); if(k) headers[k]=v; }
            else headers[s]="true";
        });
    }
    if(token && token.trim()) headers["Authorization"]=token.trim();

    headerVars.forEach(v=>{
        const val=form[v.name];
        if(val!=null && String(val)!=="") headers[v.name]=String(val);
    });

    const init:RequestInit={method, headers};

    const canBody=!!bodyVar && method!=="GET";
    if(canBody && bodyVar){
        const raw=form[bodyVar.name];
        const ct=headers["Content-Type"] || (consumes?.[0] ?? "application/json");
        headers["Content-Type"]=ct;
        if(raw!=null){
            if(ct.indexOf("application/json")!==-1){
                if(typeof raw==="string") init.body=raw;
                else { try{ init.body=JSON.stringify(raw); }catch{ init.body=String(raw); } }
            }else{
                init.body=typeof raw==="string" ? raw : String(raw);
            }
        }
    }
    return init;
}
function makeCurl(
    endpoint:EndpointVM, pathTemplate:string, pathVars:InputVM[], queryVars:InputVM[], headerVars:InputVM[], bodyVar:InputVM|null,
    form:Record<string,any>, token?:string
){
    const url = new URL(buildUrl(pathTemplate, pathVars, queryVars, form), window.location.origin);
    const parts:string[]=["curl","-X",endpoint.httpMethod];

    if(endpoint.headers?.length){
        endpoint.headers.forEach(raw=>{
            const s=(raw||"").trim(); if(!s||s[0]==="!") return;
            const eq=s.indexOf("="); if(eq>-1){ const k=s.slice(0,eq).trim(); const v=s.slice(eq+1).trim(); if(k) parts.push("-H",`"${k}: ${v.replace(/"/g,'\\"')}"`); }
            else parts.push("-H",`"${s}: true"`);
        });
    }
    if(token && token.trim()) parts.push("-H",`"Authorization: ${token.trim().replace(/"/g,'\\"')}"`);
    headerVars.forEach(h=>{
        const v=form[h.name]; if(v!=null && String(v)!=="") parts.push("-H",`"${h.name}: ${String(v).replace(/"/g,'\\"')}"`);
    });

    if(bodyVar && endpoint.httpMethod!=="GET"){
        const raw=form[bodyVar.name];
        if(typeof raw==="string" && raw.length){ parts.push("-H",`"Content-Type: application/json"`); parts.push("-d",`'${raw.replace(/'/g,"'\\''")}'`); }
        else if(raw!=null){ const s=JSON.stringify(raw).replace(/'/g,"'\\''"); parts.push("-H",`"Content-Type: application/json"`); parts.push("-d",`'${s}'`); }
    }

    parts.push(`"${url.toString()}"`);
    return parts.join(" ");
}
function formatBody(b:unknown){
    if(b==null) return "null";
    if(typeof b==="object"){ try{ return JSON.stringify(b,null,2); }catch{ return String(b); } }
    return String(b);
}

/* ===== Copy button ===== */
function CopyButton({label, text}:{label:string; text:string}){
    const [copied,setCopied]=useState(false);
    async function copy(){ try{ await navigator.clipboard.writeText(text); setCopied(true); setTimeout(()=>setCopied(false),1200);}catch{} }
    return (
        <button onClick={copy} className="px-4 py-2 rounded-lg border border-line bg-surface text-[var(--text)] hover:bg-surface2 transition shadow-sm">
            {copied ? "Copied!" : label}
        </button>
    );
}
