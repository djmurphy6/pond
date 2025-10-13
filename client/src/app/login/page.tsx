"use client"

import ThemeToggle from "@/components/ThemeToggle"

//Next

//React and Other

//Internal

export default function Login() {
    return (
        <div className="relative min-h-screen flex flex-col items-center justify-center overflow-hidden bg-[var(--background)]">
            <h1 style={{ color: "var(--foreground)" }}>Login</h1>
            <ThemeToggle />
        </div>
    )
}