"use client"

//React
import React, { CSSProperties, useEffect, useState } from "react";
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
import { useTheme } from "next-themes";
import Water from "@/components/LandingPage/water/water";

/* 
            Z LAYOUT
          -------------
          Duck - 3
          Ground - 2
          Moon/Sun - 1
          Water - 1
          Stars/Clouds - 0
          BG - 0
          -------------
*/
const textBorder = 4;

export default function App() {
  return (
    <div style={{ zIndex: 0, background: "var(--landing-page-bg)", transition: "--landing-page-bg-color2 0.3s, --landing-page-bg-color 0.3s" }} className="relative min-h-[100dvh] flex flex-col items-center justify-center overflow-hidden">

      <Moon />
      <Sun />
      <Stars />
      <Clouds />

      <div className="relative z-10 flex flex-col items-center">
        <PondText />
        <motion.p
          style={{ color: "var(--muted-foreground)" }}
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

      <Water />
      <Ground />

    </div>
  );
}

function PondText() {
  const [mounted, setMounted] = useState(false);
  const { theme } = useTheme();

  useEffect(() => setMounted(true))

  if (!mounted) return null;
  return (
    <motion.h1
      style={{
        textShadow: theme === 'light' ? `${textBorder}px ${textBorder}px 0 #000` : "0px 0px 0px transparent"
      }}
      className="text-7xl sm:text-8xl font-extrabold text-white drop-shadow-lg"
      initial={{ y: -200, opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      transition={{ duration: 0.8, ease: "easeOut" }}
    >
      Pond
    </motion.h1>
  );
}

const styles: Record<string, CSSProperties> = {
  button: {
    cursor: 'pointer',
  }
}