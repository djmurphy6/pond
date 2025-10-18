"use client";

import { motion } from "framer-motion";
import "./duck.css";
import { useEffect, useState } from "react";
import { MOBILE_RATIO } from "@/components/ThemeToggle";

export default function Duck() {

    const [scale, setScale] = useState(1);

    useEffect(() => {
        const updateScale = () => {
            const zoom = window.devicePixelRatio;
            const isMobile = /Mobi|Android/i.test(navigator.userAgent);
            setScale(isMobile ? MOBILE_RATIO : 1 / zoom);
        };
        updateScale();
        window.addEventListener("resize", updateScale);
        return () => window.removeEventListener("resize", updateScale);
    }, []);

    return (

        <motion.div
            className="duck"
            style={{ scaleX: -0.75 * scale, scaleY: 0.75 * scale, position: "absolute" }}
            animate={{
                x: [`-${550 * scale}px`, `${550 * scale}px`],
                y: [`${250 * scale}px`],
            }}
            transition={{
                duration: 25,
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
                    <div className="eye"></div>
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
