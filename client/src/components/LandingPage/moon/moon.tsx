"use client";

import { AnimatePresence, motion } from "framer-motion";
import { useTheme } from "next-themes";
import { useEffect, useState } from "react";
import "./moon.css";
import { MOBILE_RATIO, THEME_KEY } from "@/components/ThemeToggle";

export default function Moon() {
    const { theme, setTheme } = useTheme();
    const [mounted, setMounted] = useState(false);

    const [scale, setScale] = useState(1);

    useEffect(() => {
        setTheme(localStorage.getItem(THEME_KEY) === "dark" ? "dark" : "light");
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
        let newTheme = theme !== "dark" ? "dark" : "light";
        localStorage.setItem(THEME_KEY, newTheme);
        setTheme(newTheme);
    };

    return (
        <AnimatePresence mode="wait">
            {theme === "dark" && (
                <motion.div
                    onClick={toggleTheme}
                    className="moon"
                    style={{ scale: 1.5 * scale, cursor: "pointer" }}
                    initial={{ x: 0 * scale, y: -850 * scale, }}
                    animate={{ x: 0, y: -300 * scale, }}
                    exit={{ x: 0 * scale, y: 700 * scale, }}
                    transition={{ duration: 0.3, ease: "easeInOut" }}
                >
                    {/* moon craters */}
                    {[...Array(11)].map((_, i) => (
                        <div
                            key={i}
                            className="hole"
                            style={i === 3 ? { transform: "scale(0.75)" } : {}}
                        />
                    ))}
                </motion.div>
            )}
        </AnimatePresence>
    );
}
