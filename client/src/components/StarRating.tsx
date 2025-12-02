"use client";

import * as React from "react";
import { Star } from "lucide-react";
import { cn } from "@/lib/utils";

type StarRatingProps = {
    value: number;                 // current rating (0–max), can be decimal
    onChange?: (value: number) => void;
    max?: number;                  // number of stars
    readOnly?: boolean;
    className?: string;
    size?: number;                 // icon size
};

export default function StarRating({
    value,
    onChange,
    max = 5,
    readOnly = false,
    className,
    size = 20,
}: StarRatingProps) {
    const [hover, setHover] = React.useState<number | null>(null);

    const displayValue = hover ?? value;

    const handleClick = (v: number) => {
        if (readOnly || !onChange) return;
        onChange(v);
    };

    // const handleKeyDown = (e: React.KeyboardEvent<HTMLButtonElement>, v: number) => {
    //     if (readOnly || !onChange) return;

    //     if (e.key === "Enter" || e.key === " ") {
    //         e.preventDefault();
    //         onChange(v);
    //     }

    //     if (e.key === "ArrowRight" || e.key === "ArrowUp") {
    //         e.preventDefault();
    //         onChange(Math.min(max, value + 1));
    //     }

    //     if (e.key === "ArrowLeft" || e.key === "ArrowDown") {
    //         e.preventDefault();
    //         onChange(Math.max(0, value - 1));
    //     }
    // };
    const StarElement = readOnly ? "div" : "button";
    return (
        <div
            className={cn("flex items-center", className)}
            role="radiogroup"
            aria-label="Rating"
        >
            {Array.from({ length: max }).map((_, i) => {
                const starValue = i + 1;

                const diff = displayValue - i;
                const clamped = Math.min(Math.max(diff, 0), 1);
                const fillPercent = Math.round(clamped * 100);

                return (
                    <StarElement
                        key={starValue}
                        type="button"
                        role="radio"
                        aria-checked={Math.round(value) === starValue}
                        aria-label={`${starValue} star${starValue > 1 ? "s" : ""}`}
                        disabled={readOnly}
                        onClick={() => handleClick(starValue)}
                        onMouseEnter={() => !readOnly && setHover(starValue)}
                        onMouseLeave={() => !readOnly && setHover(null)}
                        // onKeyDown={(e) => handleKeyDown(e, starValue)}
                        className={cn(
                            "transition-transform focus:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background rounded-sm",
                            !readOnly && "cursor-pointer hover:scale-110",
                            i !== 0 && "pl-1"
                        )}
                    >
                        <div
                            className="relative"
                            style={{ width: size, height: size }}
                        >
                            {/* Empty star (background) */}
                            <Star
                                width={size}
                                height={size}
                                className="text-muted-foreground"
                            />

                            {/* Filled part */}
                            <div
                                className="absolute inset-0 overflow-hidden"
                                style={{ width: `${fillPercent}%` }}
                            >
                                <Star
                                    width={size}
                                    height={size}
                                    className="fill-[var(--uo-yellow)] text-[var(--uo-yellow)]"
                                />
                            </div>
                        </div>
                    </StarElement>
                );
            })}
        </div>
    );
}
