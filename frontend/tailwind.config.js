/** @type {import('tailwindcss').Config} */
export default {
    content: ["./index.html", "./src/**/*.{ts,tsx,js,jsx,html}"],
    theme: {
        extend: {
            colors: {
                method: { get: "#00C853", post: "#2962FF", put: "#FFA000", delete: "#D50000" },
                brand: { primary: "#00B4D8" }
            }
        }
    },
    plugins: []
}