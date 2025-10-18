"use client"

import { motion } from "framer-motion";
import { useTheme } from "next-themes";

export default function Ground() {
    const { theme } = useTheme();

    return (
        <motion.div style={{
            position: 'absolute',
            top: '90%',
            left: '50%',
            transform: 'translate(-50%, -50%)',
            width: '100%',
            height: '20%',
            zIndex: 2
        }}
            animate={{
                background: theme === "light" ? "linear-gradient(to top, #0a330dff 0%, #2b7415ff 40%, #7dfc15ff 100%)" : "linear-gradient(to top, #0a3330ff 0%, #0e4d3aff 40%, #0dad78ff 100%)",
            }}
            transition={{ duration: 0.3, ease: "easeInOut" }}
        />
    )
}