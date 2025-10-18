"use client";

import { AnimatePresence, motion } from "framer-motion";
import { useTheme } from "next-themes";
import { useEffect, useState } from "react";
import "./sun.css";

const THEME_KEY = "theme-storage";

export default function Sun() {
    const { theme, setTheme } = useTheme();
    const [mounted, setMounted] = useState(false);
    useEffect(() => {
        setTheme(localStorage.getItem(THEME_KEY) || "light");
        setMounted(true);
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
                    style={{ transform: "scale(1.5)", cursor: "pointer" }}
                    initial={{ x: -200, y: -200, scale: 0.8, }}
                    animate={{ x: 0, y: 0, scale: 1, }}
                    exit={{ x: 200, y: 200, scale: 0.8, }}
                    transition={{ duration: 0.3, ease: "easeInOut" }}
                />

            )}
        </AnimatePresence>
    );
}
