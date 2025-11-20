import React from "react";
import { cn } from "@/lib/utils"; // if you're using shadcn utils
import { useTheme } from "next-themes";

interface SideBarAsideProps {
  className?: string;
  children?: React.ReactNode;
}

export function SideBarAside({ className, children }: SideBarAsideProps) {
  const { theme } = useTheme();

  return (
    <aside
      className={cn(
        "w-64 border-r p-4 flex flex-col transition-colors duration-300 min-h-screen",
        theme !== "dark"
          ? "bg-[#ffffff] md:shadow-[2px_0_10px_rgba(0,0,0,0.15)]"
          : "bg-muted/30",
        className
      )}
    >
      {children}
    </aside>
  );
}
