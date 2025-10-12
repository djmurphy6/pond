"use client";

//Next
import { ThemeProvider } from "next-themes";

//React and Other
import { useEffect } from "react";

//Internal
import ThemeToggle from "@/components/ThemeToggle";

export default function Page() {
  useEffect(() => {
    document.documentElement.removeAttribute("data-theme-initializing");
  }, []);

  return (
    <ThemeProvider attribute="class" defaultTheme="system" enableSystem>
      <ThemeToggle />
    </ThemeProvider>
  );
}
