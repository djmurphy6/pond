"use client";

import { AnimatePresence, motion } from "framer-motion";
import { useTheme } from "next-themes";
import { useEffect, useState } from "react";
import "./sun.css";
import { MOBILE_RATIO, THEME_KEY } from "@/components/ThemeToggle";


export default function Sun() {
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

    const toggleTheme = () => {
        let newTheme = theme === "light" ? "dark" : "light";
        localStorage.setItem(THEME_KEY, newTheme);
        setTheme(newTheme);
    };

    return (
        <AnimatePresence mode="wait">
            {theme === "light" && (
                <motion.div
                    onClick={toggleTheme}
                    className="sun"
                    style={{ scale: 1.5 * scale, cursor: "pointer" }}
                    initial={{ x: 0 * scale, y: -850 * scale, }}
                    animate={{ x: 0, y: -300 * scale, }}
                    exit={{ x: 0 * scale, y: 700 * scale, }}
                    transition={{ duration: 0.3, ease: "easeInOut" }}
                />

            )}
        </AnimatePresence>
    );
}
