"use client"

import React, { CSSProperties, useEffect } from "react";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import ThemeToggle from "@/components/ThemeToggle";

export default function App() {
  return (
    <div className="relative min-h-screen flex flex-col items-center justify-center overflow-hidden bg-[var(--background)]">
      <div style={{ position: 'absolute', top: '10px', right: '10px' }}>
        <ThemeToggle />
      </div>
      <div className="relative z-10 flex flex-col items-center">
        <motion.h1
          className="text-7xl sm:text-8xl font-extrabold text-primary drop-shadow-lg"
          initial={{ y: -200, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ duration: 0.8, ease: "easeOut" }}
        >
          Pawnd
        </motion.h1>
        <motion.p
          className="mt-3 text-lg text-muted-foreground"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.8, duration: 0.8 }}
        >
          Buy • Sell • Connect — The UO Student Marketplace
        </motion.p>

        {/* Buttons */}
        <motion.div
          className="mt-8 flex gap-4"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 1.6, duration: 0.6 }}
        >
          <Button
            style={{ ...styles.button, backgroundColor: 'var(--uo-green)', color: 'white' }}
            onClick={() => { window.location.href = "/register"; }}
            className="px-6 py-3 text-lg rounded-full shadow-md text-white 
             hover:-translate-y-1 hover:shadow-[0_0_15px_var(--uo-green)] 
             transition-all duration-300 ease-in-out"
          >
            Get Started
          </Button>
          <Button
            style={{ ...styles.button, backgroundColor: 'var(--uo-yellow)', color: 'var(--background)' }}
            onClick={() => { window.location.href = "/login"; }}
            className="px-6 py-3 text-lg rounded-full shadow-md text-white 
             hover:-translate-y-1 hover:shadow-[0_0_15px_var(--uo-yellow)] 
             transition-all duration-300 ease-in-out"
          >
            Login
          </Button>
        </motion.div>
      </div>
    </div>
  );
}

function Cloud({ className, delay = 0 }: { className?: string; delay?: number }) {
  return (
    <motion.div
      className={`absolute w-24 h-12 bg-white rounded-full blur-sm ${className}`}
      initial={{ x: -100, opacity: 0 }}
      animate={{ x: 1000, opacity: [0.8, 1, 0.8] }}
      transition={{ repeat: Infinity, duration: 30, delay, ease: "linear" }}
    />
  );
}

const styles: Record<string, CSSProperties> = {
  button: {
    cursor: 'pointer',
  }
}