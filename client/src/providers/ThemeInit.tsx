"use client";

import { LoadAppConfig } from "@/api/WebService";
import { useTheme } from "next-themes";
import { useEffect } from "react";

export function ThemeInit() {
    const { theme } = useTheme();

    useEffect(() => {
        LoadAppConfig();
        document.documentElement.removeAttribute("data-theme-initializing");
    }, []);


    useEffect(() => {
        const color = theme !== "dark" ? "#ffffff" : "#0a0a0a"; // var(--background)
        let metaTag = document.querySelector('meta[name="theme-color"]');

        if (!metaTag) {
            metaTag = document.createElement("meta");
            metaTag.setAttribute("name", "theme-color");
            document.head.appendChild(metaTag);
        }

        metaTag.setAttribute("content", color);
    }, [theme]);

    return null;
}
