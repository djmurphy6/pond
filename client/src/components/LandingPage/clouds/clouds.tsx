"use client";

import { AnimatePresence, motion } from "framer-motion";

import './clouds.css'
import { useTheme } from "next-themes";
import { useEffect, useMemo, useState } from "react";
import { MOBILE_RATIO } from "@/components/ThemeToggle";

export default function Clouds() {

    const { theme, setTheme } = useTheme();
    const [mounted, setMounted] = useState(false);

    const [scaleRatio, setScaleRatio] = useState(1);

    useEffect(() => {
        setMounted(true);
    }, []);
    useEffect(() => {
        const updateScale = () => {
            const zoom = window.devicePixelRatio;
            const isMobile = /Mobi|Android/i.test(navigator.userAgent);
            setScaleRatio(isMobile ? MOBILE_RATIO : 1 / zoom);
        };
        updateScale();
        window.addEventListener("resize", updateScale);
        return () => window.removeEventListener("resize", updateScale);
    }, []);

    const clouds = useMemo(() => CLOUD_POSITIONS.map(({ top, left, scale }, i) => (
        <div
            key={i}
            style={{
                top: `${top}%`,
                left: `${left}%`,
                position: "absolute",
                transform: `scale(${scale * scaleRatio * 0.5})`,
            }}
        >
            <div
                className="cloud"
                style={{
                    animationDelay: `${(-i * 0.37) % 5}s`,
                    animationDuration: `${10 + i % 5}s`,
                }}
            />
        </div>

    )), [scaleRatio]);

    if (!mounted) return null;
    return (
        <AnimatePresence mode="wait">
            {theme !== "dark" && (
                <motion.div
                    style={{ zIndex: 0, }}
                    initial={{ opacity: 0, x: 0 }}
                    animate={{ opacity: 1, x: 0 }}
                    exit={{ opacity: 0, x: 0 }}
                    transition={{ duration: 0.3 }}
                >
                    {clouds}
                </motion.div>
            )}
        </AnimatePresence>
    )
}

const CLOUD_POSITIONS = [
    { top: 5, left: 2, scale: 0.8 },
    { top: 32, left: 86, scale: 1.8 },
    { top: 22, left: 20, scale: 1.8 },
    { top: 50, left: 7, scale: 1.2 },
    { top: 10, left: 72, scale: 1.2 },
    { top: 50, left: 67, scale: 1.2 },
    { top: 30, left: 5, scale: 1.2 },

];

