import { useEffect, useMemo, useState } from 'react'
import { fetchEndpoints } from './api'
import type { EndpointVM } from './types'
import { Sidebar } from './components/Sidebar'
import { EndpointDetail } from './components/EndpointDetail'
import '../index.css';


export default function App() {
    const [endpoints, setEndpoints] = useState<EndpointVM[]>([])
    const [query, setQuery] = useState('')
    const [selected, setSelected] = useState<EndpointVM | null>(null)


    useEffect(() => { fetchEndpoints().then(setEndpoints) }, [])


    const filtered = useMemo(() => {
        const q = query.trim().toLowerCase()
        if (!q) return endpoints
        return endpoints.filter(e =>
            e.controller.toLowerCase().includes(q) ||
            (e.paths[0] || '').toLowerCase().includes(q) ||
            e.httpMethod.toLowerCase().includes(q)
        )
    }, [endpoints, query])


    return (
        <div className="h-screen grid grid-cols-[320px_1fr]">
            <Sidebar
                items={filtered}
                query={query}
                onQuery={setQuery}
                onSelect={setSelected}
                selectedId={selected?.id || ''}
            />
            <main className="p-6 overflow-auto bg-white">
                {selected ? (
                    <EndpointDetail endpoint={selected} />
                ) : (
                    <div className="h-full grid place-items-center text-zinc-500">
                        <div>
                            <h1 className="text-2xl font-semibold text-zinc-800 mb-2">Auto UI</h1>
                            <p>Search or pick an endpoint from the left to get started.</p>
                        </div>
                    </div>
                )}
            </main>
        </div>
    )
}