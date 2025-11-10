"use client";

//Next
import Image from "next/image";
import Link from "next/link";
import { useTheme } from "next-themes";
import { useRouter } from "next/router";

//React and Other
import { useEffect, useState } from "react";
import {
    ImageIcon,
    Sofa,
    Shirt,
    Home,
    Laptop,
    GraduationCap,
    ChevronRight,
    Tag,
    MessageCircle,
    Check,
    Shield,
} from "lucide-react";

// API
import api from "@/api/WebService";

//ShadCN
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import { Select, SelectContent, SelectItem, SelectValue, SelectTrigger } from "@/components/ui/select";

//Internal
import ThemeToggle from "@/components/ThemeToggle";
import { CreateListingModal } from "@/components/CreateListingModal";
import { ErrorResponse, Listing } from "@/api/WebTypes";
import { toast } from "sonner";
import { MyAccountPopover } from "@/components/MyAccountPopover";
import { useUserInfoStore } from "@/stores/UserInfoStore";

export default function ListingCard({ item, index = 0 }: { item: Listing; index?: number }) {
    const [hasError, setHasError] = useState(false);
    const [imageLoading, setImageLoading] = useState(true);
    const { theme } = useTheme();

    // Only prioritize first 4 images (above the fold)
    const isPriority = index < 4;

    return (
        <Card className={`${theme !== 'dark' && 'hover:shadow-lg'} hover:underline hover:-translate-y-1 transition-all duration-300 ease-[cubic-bezier(0.25,1,0.5,1)] cursor-pointer`}>
            <Link href={`/dashboard/listing/${item.listingGU}`}>
                <div
                    className="relative h-40 w-full flex items-center justify-center overflow-hidden transition-colors duration-300"
                    style={{
                        backgroundColor: theme === "dark" ? "#111111" : "#ededed",
                        transition: "background-color 300ms ease-in-out",
                    }}
                >
                    {imageLoading && !hasError && item.picture1_url && (
                        <Skeleton className="absolute inset-0 rounded-none" />
                    )}
                    {!item.picture1_url || hasError ? (
                        <ImageIcon className="h-10 w-10 text-muted-foreground transition-colors duration-300" />
                    ) : (
                        <Image
                            src={item.picture1_url}
                            alt={item.title}
                            fill
                            className="object-cover transition-colors duration-300"
                            onError={() => {
                                setHasError(true);
                                setImageLoading(false);
                            }}
                            onLoad={() => setImageLoading(false)}
                            priority={isPriority}
                            loading={isPriority ? undefined : "lazy"}
                        />
                    )}
                </div>
                <CardContent className="p-3">
                    <p className="font-medium">${item.price.toLocaleString()}</p>
                    <p className="text-md text-muted-foreground">{item.title}</p>
                </CardContent>
            </Link>
        </Card>
    );
}