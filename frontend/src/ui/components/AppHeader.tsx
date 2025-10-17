import { useEffect, useState } from "react";

export function AppHeader({
                              onSend,
                              canSend,
                          }: {
    onSend?: () => void;
    canSend?: boolean;
}) {
    const [token, setToken] = useState<string>(() => localStorage.getItem("autoUi.token") || "");
    const [dark, setDark] = useState<boolean>(() => document.documentElement.classList.contains("dark"));

    useEffect(() => {
        localStorage.setItem("autoUi.token", token);
    }, [token]);

    useEffect(() => {
        const root = document.documentElement;
        if (dark) root.classList.add("dark");
        else root.classList.remove("dark");
    }, [dark]);

    return (
        <div className="sticky top-0 z-30 border-b bg-[var(--surface)]/80 backdrop-blur supports-[backdrop-filter]:bg-[var(--surface)]/60">
            <div className="max-w-6xl mx-auto px-4 py-3 flex items-center gap-3">
                <div className="font-semibold text-lg tracking-tight text-[var(--text)]">Auto-UI</div>

                <div className="ml-auto flex items-center gap-2">
                    <input
                        className="px-3 py-1.5 rounded-lg border bg-[var(--surface-2)] text-sm text-[var(--text)] placeholder-[var(--muted)] focus:outline-none focus:ring-2 focus:ring-brand/60"
                        placeholder="Authorization: Bearer …"
                        value={token}
                        onChange={(e) => setToken(e.target.value)}
                    />
                    <button
                        onClick={() => setDark((v) => !v)}
                        className="px-3 py-1.5 rounded-lg border text-sm text-[var(--text)] hover:bg-[var(--surface-2)]"
                        title="Toggle theme"
                    >
                        {dark ? "Light" : "Dark"}
                    </button>
                    {onSend && (
                        <button
                            onClick={onSend}
                            disabled={!canSend}
                            className="px-3 py-1.5 rounded-lg bg-brand text-white disabled:opacity-40"
                            title="Ctrl/Cmd + Enter"
                        >
                            Send
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
}
