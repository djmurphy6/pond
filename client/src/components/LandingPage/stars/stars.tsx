"use client";

import { AnimatePresence, motion } from "framer-motion";
import { useEffect, useMemo, useState } from "react";
import { useTheme } from "next-themes";

import './stars.css'
import { MOBILE_RATIO } from "@/components/ThemeToggle";

export default function Stars() {
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


    const stars = useMemo(() => STAR_POSITIONS.map(({ top, left }, i) => {
        const delay = (i * 0.37) % 5;
        const duration = 1 + ((i * 0.61) % 3);
        const scale = (0.25 + ((i * 0.43) % 0.8) * scaleRatio);
        const opacity = 0.3 + ((i * 0.51) % 0.7);

        return (
            <div
                key={i}
                className={`star`}
                style={{
                    top: `${top}%`,
                    left: `${left}%`,
                    opacity,
                    scale,
                    animationDelay: `${delay}s`,
                    animationDuration: `${duration}s`,
                }}
            />
        );
    }), [scaleRatio]);

    if (!mounted) return null;
    return (
        <AnimatePresence mode="wait">
            {theme === 'dark' && (
                <motion.div
                    style={{ zIndex: 0 }}
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    transition={{ duration: 0.3 }}
                >
                    {stars}
                </motion.div>
            )}
        </AnimatePresence>
    )
}

const STAR_POSITIONS = [
    // top, left (avoiding ~40–60%)
    { top: 4, left: 10 }, { top: 8, left: 25 }, { top: 6, left: 70 }, { top: 10, left: 90 },
    { top: 12, left: 35 }, { top: 15, left: 5 }, { top: 15, left: 80 }, { top: 18, left: 75 },
    { top: 22, left: 20 }, { top: 22, left: 85 }, { top: 25, left: 8 }, { top: 25, left: 95 },
    { top: 28, left: 33 }, { top: 28, left: 67 }, { top: 30, left: 12 }, { top: 30, left: 88 },
    { top: 33, left: 28 }, { top: 33, left: 72 }, { top: 36, left: 10 }, { top: 36, left: 90 },
    // bottom half
    { top: 56, left: 10 }, { top: 56, left: 90 }, { top: 58, left: 30 }, { top: 58, left: 70 },
    { top: 60, left: 5 }, { top: 60, left: 95 }, { top: 63, left: 15 }, { top: 63, left: 85 },
    { top: 65, left: 28 }, { top: 65, left: 72 }, { top: 68, left: 12 }, { top: 68, left: 88 },
    { top: 70, left: 40 }, { top: 73, left: 60 }, { top: 75, left: 5 }, { top: 75, left: 80 },
    { top: 78, left: 25 }, { top: 78, left: 70 }, { top: 80, left: 15 }, { top: 80, left: 85 },
    { top: 84, left: 12 }, { top: 84, left: 88 }, { top: 86, left: 45 }, { top: 88, left: 55 },
    { top: 90, left: 20 }, { top: 90, left: 80 }, { top: 92, left: 30 }, { top: 92, left: 70 },
    { top: 96, left: 40 }, { top: 96, left: 60 },
];

