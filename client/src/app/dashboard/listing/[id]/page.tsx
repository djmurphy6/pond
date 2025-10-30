"use client"

//Next
import Image from "next/image";
import Link from "next/link";
import { useParams } from "next/navigation";
import { useTheme } from "next-themes";

//React
import { Bookmark, MessageCircle, MoreHorizontal, Share2, X } from "lucide-react";
import { useEffect, useState } from "react";
import { toast } from "sonner";

//API
import api from "@/api/WebService";
import { ErrorResponse, Listing } from "@/api/WebTypes";

//ShadCN
import { Button } from "@/components/ui/button";
import ImageViewer from "@/components/ImageViewer";
import ThemeToggle from "@/components/ThemeToggle";
import { Separator } from "@/components/ui/separator";

export default function ListingPage() {
    const params = useParams() as { id: string };
    const [listing, setListing] = useState<Listing>();
    const { theme } = useTheme();
    const [mounted, setMounted] = useState(false);
    const [copied, setCopied] = useState(false);

    useEffect(() => {
        (async () => {
            let res = await api.GetSpecificListing({ listingGU: params.id })
            if (res instanceof ErrorResponse) {
                toast.error(res.body?.error);
            } else {
                console.log(res);
                setListing(res);
            }
        })();
        setMounted(true);
    }, []);

    const handleCopy = async () => {
        try {
            if (!copied) {
                await navigator.clipboard.writeText(window.location.href);
                setCopied(true);
                setTimeout(() => setCopied(false), 2000);
                toast.success("Link Copied!");
            }
        } catch (err) {
            console.error("Failed to copy URL:", err);
        }
    };

    if (!listing || !mounted) return null;

    const images = [
        listing.picture1_url,
        listing.picture2_url,
    ].filter(item => item !== "" && item !== null && item !== undefined);

    return (
        <div className="flex h-screen bg-background transition-colors duration-300">
            <Button variant={"ghost"} style={{ zIndex: 2, top: 10, left: 10, height: 50, width: 50, backgroundColor: 'var(--accent)', }} className="absolute border rounded-full cursor-pointer hover:opacity-80">
                <Link href={'/dashboard'}>
                    <X style={{ height: 25, width: 25, }} />
                </Link>
            </Button>

            <ImageViewer images={images} />
            <aside style={{ zIndex: 1 }} className={`w-80 border-l bg-muted/10 p-4 flex flex-col transition-colors duration-300 ${theme !== "dark" && "shadow-[-2px_0_10px_rgba(0,0,0,0.15)]"}`}>
                {/* Top section */}
                <div className="flex items-center gap-2 mb-0 justify-between">
                    <h2 className="text-xl font-semibold">Pond</h2>
                    <ThemeToggle />
                </div>


                {/* Listing Title and Price */}
                <div className="my-6">
                    <h2 className="text-xl font-bold leading-tight">{listing.title}</h2>
                    <div className="flex items-center gap-2 mt-1">
                        <span className="text-xl font-semibold">${listing.price.toLocaleString()}</span>
                        {/* <span className="text-muted-foreground line-through">$4,000</span> */}
                    </div>
                    {/* can add the time its been uploaded for */}
                    {/* <p className="text-sm text-muted-foreground mt-1"></p> */}
                </div>


                {/* Buttons */}
                <div className="flex gap-2 mb-6">
                    <Button className="cursor-pointer flex-1 text-white bg-[var(--uo-green)] hover:bg-[var(--uo-green)]/70">
                        <MessageCircle className="mr-2 h-4 w-4" /> Message
                    </Button>
                    <Button variant="outline" size="icon" className="cursor-pointer">
                        <Bookmark className="h-4 w-4" />
                    </Button>
                    <Button onClick={handleCopy} variant="outline" size="icon" className="cursor-pointer">
                        <Share2 className="h-4 w-4" />
                    </Button>
                    {/* More Options */}
                    {/* <Button variant="outline" size="icon">
                        <MoreHorizontal className="h-4 w-4" />
                    </Button> */}
                </div>


                <Separator className="my-4" />


                {/* Details Section */}
                <div className="space-y-4">
                    <div>
                        <h3 className="text-lg font-semibold">Details</h3>
                        <div className="flex justify-between text-sm mt-1">
                            <span className="text-muted-foreground">Condition</span>
                            <span>{listing.condition}</span>
                        </div>
                    </div>


                    <p className="text-sm leading-relaxed text-foreground">
                        {listing.description}
                    </p>
                </div>
            </aside>
        </div>
    );
}
