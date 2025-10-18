"use client";

import { AnimatePresence, motion, useAnimation } from "framer-motion";
import "./duck.css";
import { useEffect, useState } from "react";
import { MOBILE_RATIO } from "@/components/ThemeToggle";
import { useTheme } from "next-themes";

export default function Duck() {

    const [scale, setScale] = useState(1);
    const [isMobile, setIsMobile] = useState(false);
    const { theme } = useTheme();
    const [mounted, setMounted] = useState(false);

    useEffect(() => setMounted(true), []);

    useEffect(() => {
        const updateScale = () => {
            const zoom = window.devicePixelRatio;
            const isMobile = /Mobi|Android/i.test(navigator.userAgent);
            setIsMobile(isMobile);
            setScale(isMobile ? MOBILE_RATIO : 1 / zoom);
        };
        updateScale();
        window.addEventListener("resize", updateScale);
        return () => window.removeEventListener("resize", updateScale);
    }, []);

    if (!mounted) return null;
    return (

        <motion.div
            className="duck"
            style={{ scaleX: -0.75 * scale, scaleY: 0.75 * scale, position: "absolute", bottom: '0px' }}
            animate={{
                x: [`-${(isMobile ? 400 : 1050) * scale}px`, `${(isMobile ? 400 : 1050) * scale}px`],
                y: [`${(isMobile ? -4 : -7) * scale}rem`],
            }}
            transition={{
                duration: (isMobile ? 20 : 47.27),
                ease: "linear",
                repeat: Infinity,
            }}
        >
            <motion.div
                animate={{
                    y: [0, 2, 0],
                }}
                transition={{ delay: 0.25, duration: 0.5, repeat: Infinity, ease: "easeInOut" }}
            >
                <motion.div
                    animate={{
                        y: [0, -4, 0],
                    }}
                    transition={{ duration: 0.5, repeat: Infinity, ease: "easeInOut" }}
                >
                    <div className="featherHead" />
                    <div className="featherHead2" />


                    <div className="duck-body" />

                    <motion.div
                        className="eye"
                        initial={{ rotate: theme === "light" ? -30 : 0 }}
                        animate={{ rotate: theme === "light" ? -30 : 0 }}
                    />

                    <AnimatePresence>
                        {theme === 'light' && (
                            <motion.div
                                initial={{ y: -20, opacity: 0.5 }}
                                animate={{ y: 0, opacity: 1 }}
                                exit={{ y: -20, opacity: 0 }}
                                transition={{ duration: 0.3, ease: "easeIn" }}
                            >
                                <div className="sun-glasses"></div>
                                <div className="sun-glasses-top"></div>
                                <div className="arch"></div>
                            </motion.div>
                        )}
                    </AnimatePresence>

                    <motion.div
                        className="beak"
                        animate={{
                            y: [0, -1, 0],
                        }}
                        transition={{ delay: 0, duration: 0.5, repeat: Infinity, ease: "easeInOut" }}
                    />
                    {/* Wing */}
                    <motion.div
                        className="feather"
                        animate={{
                            y: [0, -2, 0],
                            rotate: [0, -2, 0],
                        }}
                        transition={{ delay: 0.1, duration: 0.5, repeat: Infinity, ease: "easeInOut" }}
                    />
                </motion.div>




                <motion.div
                    className="leg-left"
                    animate={{
                        // x: [-5, 0, -5],
                        y: [0, 10, 0],
                        rotate: [5, -2, 5],
                    }}
                    transition={{ delay: 0.5, duration: 1, repeat: Infinity, ease: "easeInOut" }}
                />
                <motion.div
                    className="leg-right"
                    animate={{
                        // x: [-5, 0, -5],
                        y: [0, 10, 0],
                        rotate: [5, -2, 5],
                    }}
                    transition={{ duration: 1, repeat: Infinity, ease: "easeInOut" }}
                />

            </motion.div>
        </motion.div>
    );
}
