"use client"

//React
import React, { CSSProperties, useEffect } from "react";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
//Internal
import ThemeToggle from "@/components/ThemeToggle";
import Duck from "../components/LandingPage/duck/duck";

import Moon from "@/components/LandingPage/moon/moon";
import Sun from "@/components/LandingPage/sun/sun";
import Stars from "@/components/LandingPage/stars/stars";
import Ground from "@/components/LandingPage/ground/ground";
import Clouds from "@/components/LandingPage/clouds/clouds";

/* 
  Z LAYOUT
-------------
Duck - 3
Ground - 2
Moon/Sun - 1
Stars/Clouds - 0
BG - 0
-------------
*/

export default function App() {
  return (
    <div style={{ zIndex: 0 }} className="relative min-h-[100dvh] flex flex-col items-center justify-center overflow-hidden bg-[var(--landing-page-bg)]">

      <Moon />
      <Sun />
      <Stars />
      <Clouds />

      <div className="relative z-10 flex flex-col items-center">
        <motion.h1
          className="text-7xl sm:text-8xl font-extrabold text-primary drop-shadow-lg"
          initial={{ y: -200, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ duration: 0.8, ease: "easeOut" }}
        >
          Pond
        </motion.h1>
        <motion.p
          className="mt-3 text-lg text-muted-foreground text-center"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.8, duration: 0.8 }}
        >
          Buy • Sell • Connect — The UO Student Marketplace
        </motion.p>

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
             hover:-translate-y-1 hover:shadow-[0_0_10px_var(--uo-green)] 
             transition-all duration-300 ease-in-out"
          >
            Get Started
          </Button>
          <Button
            style={{ ...styles.button, backgroundColor: 'var(--uo-yellow)', color: 'var(--background)' }}
            onClick={() => { window.location.href = "/login"; }}
            className="px-6 py-3 text-lg rounded-full shadow-md text-white 
             hover:-translate-y-1 hover:shadow-[0_0_10px_var(--uo-yellow)] 
             transition-all duration-300 ease-in-out"
          >
            Login
          </Button>
        </motion.div>
      </div>

      <Duck />

      <Ground />

    </div>
  );
}

const styles: Record<string, CSSProperties> = {
  button: {
    cursor: 'pointer',
  }
}