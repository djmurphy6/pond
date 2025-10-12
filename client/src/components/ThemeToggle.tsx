"use client";
//Next
import { useTheme } from "next-themes";

//React and Other
import { useEffect, useState } from "react";
import { Moon, Sun } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";

const THEME_KEY = "theme-storage";

export default function ThemeToggle() {
    const { theme, setTheme } = useTheme();
    const [mounted, setMounted] = useState(false);
    useEffect(() => {
        setTheme(localStorage.getItem(THEME_KEY) || "light");
        setMounted(true);
    }, []);
    if (!mounted) return null;

    const toggle = () => {
        let newTheme = theme === "light" ? "dark" : "light";
        localStorage.setItem(THEME_KEY, newTheme);
        setTheme(newTheme);
    };

    return (
        <button
            onClick={toggle}
            className="
            p-2 rounded-full border 
            transition-colors duration-300
            shadow-md dark:shadow-none
            bg-[var(--secondary)]
            "
            style={{ cursor: 'pointer' }}
        >
            <AnimatePresence mode="wait" initial={false}>
                {theme === "light" ? (
                    <motion.div
                        key="moon"
                        initial={{ rotate: -90, opacity: 0 }}
                        animate={{ rotate: 0, opacity: 1 }}
                        exit={{ rotate: 90, opacity: 0 }}
                        transition={{ duration: 0.15 }}
                    >
                        <Moon />
                    </motion.div>
                ) : (
                    <motion.div
                        key="sun"
                        initial={{ rotate: 90, opacity: 0 }}
                        animate={{ rotate: 0, opacity: 1 }}
                        exit={{ rotate: -90, opacity: 0 }}
                        transition={{ duration: 0.15 }}
                    >
                        <Sun />
                    </motion.div>
                )}
            </AnimatePresence>
        </button>
    );
}
