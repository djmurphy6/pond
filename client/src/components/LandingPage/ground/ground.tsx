"use client"

import { motion } from "framer-motion";
import { useTheme } from "next-themes";

import "./ground.css"
import { useEffect, useState } from "react";
import { MOBILE_RATIO } from "@/components/ThemeToggle";

export default function Ground() {
    const [scale, setScale] = useState(1);
    const [isMobile, setIsMobile] = useState(false);
    const { theme } = useTheme();
    const [mounted, setMounted] = useState(false);

    useEffect(() => setMounted(true), []);

    useEffect(() => {
        const updateScale = () => {
            const zoom = window.devicePixelRatio;
            // const isMobile = /Mobi|Android/i.test(navigator.userAgent);
            const isMobile = window.matchMedia("(max-width: 768px)").matches;
            setIsMobile(isMobile);
            setScale(isMobile ? MOBILE_RATIO : 1 / zoom);
        };
        updateScale();
        window.addEventListener("resize", updateScale);
        return () => window.removeEventListener("resize", updateScale);
    }, []);


    if (!mounted) return null;
    return (
        <>
            <motion.div style={{
                position: 'absolute',
                bottom: `${-100 * scale}px`,
                left: `${250 * scale}px`,
                transform: 'translate(-50%, -50%)',
                width: `${500 * scale}px`,
                height: `${200 * scale}px`,
                overflow: 'revert',
                zIndex: 2,
                borderRadius: "0% 20% 0% 0% / 100% 50% 100% 100%"
            }}
                animate={{
                    background: theme === "light" ? "linear-gradient(to top, #0a330dff 0%, #37941bff 40%, #7dfc15ff 100%)" : "linear-gradient(to top, #0a3330ff 0%, #0e4d3aff 40%, #0dad78ff 100%)",
                }}
                transition={{ duration: 0.3, ease: "easeInOut" }}
            />
            <motion.div style={{
                position: 'absolute',
                bottom: `${-100 * scale}px`,
                right: `-${250 * scale}px`,
                transform: 'translate(-50%, -50%)',
                width: `${500 * scale}px`,
                height: `${200 * scale}px`,
                overflow: 'revert',
                zIndex: 2,
                borderRadius: "20% 0% 0% 0% / 50% 100% 100% 100%"
            }}
                animate={{
                    background: theme === "light" ? "linear-gradient(to top, #0a330dff 0%, #37941bff 40%, #7dfc15ff 100%)" : "linear-gradient(to top, #0a3330ff 0%, #0e4d3aff 40%, #0dad78ff 100%)",
                }}
                transition={{ duration: 0.3, ease: "easeInOut" }}
            />


            {!isMobile && (<motion.div
                style={{ scale: scale, left: 0, bottom: 0 }}
                initial={{ x: 20 * scale, y: 200 * scale }}
                animate={{
                    x: 20 * scale,
                    y: 30 * scale,
                    background: theme === "light" ? "linear-gradient(to top, #0a330dff 0%, #37941bff 10%, #7dfc15ff 100%)" : "linear-gradient(to top, #0a3330ff 0%, #0e4d3aff 10%, #0dad78ff 100%)",
                }}
                transition={{ duration: 0.3, ease: "easeInOut" }}
                className="hill"
            />)}
            {!isMobile && (<motion.div
                style={{ scale: scale, left: 0, bottom: 0 }}
                initial={{ x: 190 * scale, y: 200 * scale }}
                animate={{
                    x: 190 * scale,
                    y: -30 * scale,
                    background: theme === "light" ? "linear-gradient(to top, #0a330dff 0%, #37941bff 10%, #7dfc15ff 100%)" : "linear-gradient(to top, #0a3330ff 0%, #0e4d3aff 10%, #0dad78ff 100%)",
                }}
                transition={{ duration: 0.3, ease: "easeInOut" }}
                className="hill"
            />)}





            <motion.div
                style={{ scale: scale, right: 0, bottom: 0 }}
                initial={{ x: -130 * scale, y: 200 * scale }}
                animate={{
                    x: -130 * scale,
                    y: -50 * scale,
                    background: theme === "light" ? "linear-gradient(to top, #0a330dff 0%, #37941bff 10%, #7dfc15ff 100%)" : "linear-gradient(to top, #0a3330ff 0%, #0e4d3aff 10%, #0dad78ff 100%)",
                }}
                transition={{ duration: 0.3, ease: "easeInOut" }}
                className="hill"
            />
            <motion.div
                style={{ scale: scale, right: 0, bottom: 0 }}
                initial={{ x: -30 * scale, y: 200 * scale }}
                animate={{
                    x: -30 * scale,
                    y: 10 * scale,
                    background: theme === "light" ? "linear-gradient(to top, #0a330dff 0%, #37941bff 10%, #7dfc15ff 100%)" : "linear-gradient(to top, #0a3330ff 0%, #0e4d3aff 10%, #0dad78ff 100%)",
                }}
                transition={{ duration: 0.3, ease: "easeInOut" }}
                className="hill"
            />
            <motion.div
                style={{ scale: scale, right: 0, bottom: 0 }}
                initial={{ x: -200 * scale, y: 200 * scale }}
                animate={{
                    x: -200 * scale,
                    y: 50 * scale,
                    background: theme === "light" ? "linear-gradient(to top, #0a330dff 0%, #37941bff 10%, #7dfc15ff 100%)" : "linear-gradient(to top, #0a3330ff 0%, #0e4d3aff 10%, #0dad78ff 100%)",
                }}
                transition={{ duration: 0.3, ease: "easeInOut" }}
                className="hill"
            />
        </>
    )
}