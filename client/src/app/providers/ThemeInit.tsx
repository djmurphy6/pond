"use client";
import { useEffect } from "react";

export function ThemeInit() {
    useEffect(() => {
        document.documentElement.removeAttribute("data-theme-initializing");
    }, []);

    return null;
}
