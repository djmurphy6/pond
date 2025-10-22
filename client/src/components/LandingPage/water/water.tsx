"use client";

import { motion } from "framer-motion";
import { useTheme } from "next-themes";
import { useEffect, useState } from "react";
import { MOBILE_RATIO, THEME_KEY } from "@/components/ThemeToggle";
import "./water.css";


export default function Water() {
    const { theme, setTheme } = useTheme();
    const [mounted, setMounted] = useState(false);

    const [scale, setScale] = useState(1);

    useEffect(() => {
        setTheme(localStorage.getItem(THEME_KEY) || "light");
        setMounted(true);
    }, []);
    useEffect(() => {
        const updateScale = () => {
            const zoom = window.devicePixelRatio;
            // const isMobile = /Mobi|Android/i.test(navigator.userAgent);
            const isMobile = window.matchMedia("(max-width: 768px)").matches;
            setScale(isMobile ? MOBILE_RATIO : 1 / zoom);
        };
        updateScale();
        window.addEventListener("resize", updateScale);
        return () => window.removeEventListener("resize", updateScale);
    }, []);

    if (!mounted) return null;
    return (
        <>
            <motion.div
                style={{
                    height: `35vh`,
                }}
                className="water-back"
            />
            <motion.div
                style={{
                    height: `35vh`,
                }}
                className="water-mid"
            />
            <motion.div
                style={{
                    height: `35vh`,
                }}
                animate={{ background: theme !== "dark" ? "linear-gradient(to top, var(--water-front) 50%, var(--water-front) 100%)" : "linear-gradient(to top, oklch(0.3466 0.1156 257.57) 10%, var(--water-front) 50%, var(--water-front) 100%)" }}
                transition={{ duration: 0.3, ease: "easeInOut" }}
                className="water-front"
            />
        </>

    );
}
