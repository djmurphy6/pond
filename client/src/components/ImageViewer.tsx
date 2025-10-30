"use client";

import { Button } from "@/components/ui/button";
import { ChevronLeft, ChevronRight } from "lucide-react";
import Image from "next/image";
import { useState } from "react";

export default function ImageViewer({ images }: { images: string[] }) {
    const [index, setIndex] = useState(0);

    const prevImage = () => setIndex((i) => (i - 1 + images.length) % images.length);
    const nextImage = () => setIndex((i) => (i + 1) % images.length);

    return (
        <div className="relative w-screen h-screen bg-black flex flex-col items-center justify-center bg-background overflow-hidden">
            {/* Main image */}
            <div className="relative w-full h-full  bg-background flex items-center justify-center">
                {(images.length > 0) && (
                    <Image
                        src={images[index]}
                        alt={`Listing image ${index + 1}`}
                        fill
                        priority
                        className="py-20 object-contain transition-all duration-300 ease-in-out select-none"
                    />
                )}

                {/* Left / Right controls */}
                {images.length > 1 && (
                    <>
                        <Button
                            variant="ghost"
                            size="icon"
                            onClick={prevImage}
                            className="
                                relative cursor-pointer absolute left-6 top-1/2 -translate-y-1/2
                                text-white bg-black/50 overflow-hidden
                                before:absolute before:inset-0 before:bg-black/0
                                hover:before:bg-black/20 before:transition-colors
                            "
                        >
                            <ChevronLeft className="h-10 w-10 relative z-10" />
                        </Button>

                        <Button
                            variant="ghost"
                            size="icon"
                            onClick={nextImage}
                            className="
                                relative cursor-pointer absolute right-6 top-1/2 -translate-y-1/2
                                text-white bg-black/50 overflow-hidden
                                before:absolute before:inset-0 before:bg-black/0
                                hover:before:bg-black/20 before:transition-colors
                            "
                        >
                            <ChevronRight className="h-10 w-10 relative z-10" />
                        </Button>


                    </>
                )}
            </div>

            {/* Thumbnail indicators */}
            {images.length > 1 && (
                <div className="absolute bottom-6 flex gap-3">
                    {images.map((img, i) => (
                        <button
                            key={i}
                            onClick={() => setIndex(i)}
                            className={`relative w-20 h-14 rounded-md overflow-hidden border transition-all ${i === index ? "border-white scale-105" : "border-transparent opacity-70"
                                }`}
                        >
                            <Image src={img} alt={`Thumbnail ${i + 1}`} fill className="object-cover" />
                        </button>
                    ))}
                </div>
            )}
        </div>
    );
}
