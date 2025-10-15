"use client";

import { LoadAppConfig } from "@/api/WebService";
import { useEffect } from "react";

export function ThemeInit() {
    useEffect(() => {
        LoadAppConfig();
        document.documentElement.removeAttribute("data-theme-initializing");
    }, []);

    return null;
}
