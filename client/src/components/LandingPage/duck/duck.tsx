"use client";

import { AnimatePresence, motion, useAnimate, useAnimation } from "framer-motion";
import "./duck.css";
import { useEffect, useState } from "react";
import { MOBILE_RATIO } from "@/components/ThemeToggle";
import { useTheme } from "next-themes";

export default function Duck() {

    const [scale, setScale] = useState(1);
    const [isMobile, setIsMobile] = useState(false);
    const { theme } = useTheme();
    const [mounted, setMounted] = useState(false);
    const [isWalking, setIsWalking] = useState(true);

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

        <motion.div
            className="duck"
            style={{ scaleX: -0.75 * scale, scaleY: 0.75 * scale, position: "absolute", bottom: '0px' }}
            animate={{
                x: [`-${(isMobile ? 400 : 1050) * scale}px`, `${(isMobile ? 400 : 1050) * scale}px`],
                y: isMobile ? ['-3rem'] : [`${-7 * scale}rem`, `${-4 * scale}rem`, `${-4 * scale}rem`, `${-7 * scale}rem`],
            }}
            transition={{
                duration: (isMobile ? 20 : 47.27),
                times: isMobile ? undefined : [0, 0.3, 0.35, 0.65, 0.7, 1],
                ease: "linear",
                repeat: Infinity,
            }}
            onUpdate={(latest) => {
                const leftEdge = -450 * scale;
                const rightEdge = 450 * scale;
                const valX = parseFloat(latest.x.toString());
                if (valX <= rightEdge && valX >= leftEdge && !isMobile) setIsWalking(false);
                else setIsWalking(true);
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

                <DuckLegs isWalking={isWalking} />

            </motion.div>
        </motion.div>
    );
}

export function DuckLegs({ isWalking }: { isWalking: boolean }) {
    const leftControls = useAnimation();
    const rightControls = useAnimation();

    useEffect(() => {
        const startWalking = async () => {
            await Promise.all([
                leftControls.start({ y: 0, rotate: 0, transition: { duration: 0.4, ease: "easeInOut" } }),
                rightControls.start({ y: 0, rotate: 0, transition: { duration: 0.4, ease: "easeInOut" } }),
            ]);
            leftControls.start({
                y: [0, 10, 0],
                rotate: [5, -2, 5],
                transition: { delay: 0.5, duration: 1, repeat: Infinity, ease: "easeInOut" },
            });
            rightControls.start({
                y: [0, 10, 0],
                rotate: [5, -2, 5],
                transition: { duration: 1, repeat: Infinity, ease: "easeInOut" },
            });
        };

        const stopWalking = async () => {
            leftControls.stop();
            rightControls.stop();
            await Promise.all([
                leftControls.start({ y: -15, rotate: 0, transition: { duration: 0.4, ease: "easeInOut" } }),
                rightControls.start({ y: -15, rotate: 0, transition: { duration: 0.4, ease: "easeInOut" } }),
            ]);
        };

        if (isWalking) startWalking();
        else stopWalking();
    }, [isWalking, leftControls, rightControls]);

    return (
        <>
            <motion.div className="leg-left" animate={leftControls} />
            <motion.div className="leg-right" animate={rightControls} />
        </>
    );
}
