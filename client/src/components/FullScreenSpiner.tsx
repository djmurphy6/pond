"use client";

import { Loader2 } from "lucide-react";
import { cn } from "@/lib/utils";

export default function FullScreenSpinner({ className }: { className?: string }) {
  return (
    <div
      className={cn(
        "absolute inset-0 flex items-center justify-center bg-background/80 backdrop-blur-sm",
        className
      )}
    >
      <div className="flex flex-col items-center gap-2">
        <Loader2 className="h-12 w-12 animate-spin text-[var(--uo-green)]" />
        <p className="text-sm text-muted-foreground">Loading...</p>
      </div>
    </div>
  );
}
