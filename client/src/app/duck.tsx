// Duck.tsx
"use client";

import { motion } from "framer-motion";
import "./duck.css";

export default function Duck() {
    return (
        <motion.div
            className="duck"
            animate={{
                y: [0, -8, 0], // gentle bobbing
            }}
            transition={{ duration: 0, repeat: Infinity, ease: "easeInOut" }}
        >
            <div className="duck-body" />
            <div className="feather" />

        </motion.div>
    );
}
