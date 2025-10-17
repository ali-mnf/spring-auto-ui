import type { EndpointVM } from '../types'


export function Sidebar({ items, query, onQuery, onSelect, selectedId }:{
    items: EndpointVM[]
    query: string
    onQuery: (v:string)=>void
    onSelect: (e: EndpointVM)=>void
    selectedId: string
}){
// group by controller
    const groups = items.reduce<Record<string, EndpointVM[]>>((acc, e) => {
        (acc[e.controller] ||= []).push(e); return acc
    }, {})


    return (
        <aside className="h-full border-r bg-zinc-50">
            <div className="p-4 border-b">
                <input value={query} onChange={e=>onQuery(e.target.value)}
                       placeholder="Search method/path/controller…"
                       className="w-full px-3 py-2 rounded-lg border bg-white focus:outline-none focus:ring-2 focus:ring-brand-primary" />
            </div>
            <div className="overflow-auto p-2 space-y-4">
                {Object.entries(groups).map(([controller, eps]) => (
                    <div key={controller}>
                        <div className="px-2 py-1 text-xs uppercase tracking-wider text-zinc-500">{controller}</div>
                        <ul className="mt-1">
                            {eps.map(e => (
                                <li key={e.id}>
                                    <button onClick={()=>onSelect(e)}
                                            className={`w-full text-left px-3 py-2 rounded-lg hover:bg-white transition flex items-center gap-2 ${selectedId===e.id? 'bg-white shadow-sm' : ''}`}>
                                        <span className={`text-[11px] font-semibold px-2 py-0.5 rounded ${methodBadge(e.httpMethod)}`}>{e.httpMethod}</span>
                                        <span className="text-sm text-zinc-800 truncate">{e.paths[0]}</span>
                                    </button>
                                </li>
                            ))}
                        </ul>
                    </div>
                ))}
            </div>
        </aside>
    )
}


function methodBadge(m:string){
    switch(m){
        case 'GET': return 'bg-method-get text-white'
        case 'POST': return 'bg-method-post text-white'
        case 'PUT': return 'bg-method-put text-white'
        case 'DELETE': return 'bg-method-delete text-white'
        default: return 'bg-zinc-200 text-zinc-700'
    }
}