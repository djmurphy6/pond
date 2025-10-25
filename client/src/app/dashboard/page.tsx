"use client";

//Next
import Image from "next/image";
import Link from "next/link";
import { useTheme } from "next-themes";

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

//Internal
import ThemeToggle from "@/components/ThemeToggle";
import { CreateListingModal } from "@/components/CreateListingModal";
import { ErrorResponse, Listing } from "@/api/WebTypes";
import { toast } from "sonner";

export default function DashboardPage() {
    const [listings, setListings] = useState<Listing[]>([]);
    const [mounted, setMounted] = useState(false);
    const [loading, setLoading] = useState(true);
    const { theme } = useTheme();

    useEffect(() => {
        setMounted(true);
    }, []);

    useEffect(() => {
        if (mounted) {
            GetListings();
        }
    }, [mounted]);

    async function GetListings() {
        setLoading(true);
        let res = await api.GetListings();
        setLoading(false);
        if (res instanceof ErrorResponse) {
            toast.error(res.body?.error);
        } else {
            console.log(JSON.stringify(res))
            setListings(res);
        }
        setLoading(false);
    }

    if (!mounted) return null;
    return (
        <div className="flex h-screen bg-background transition-colors duration-300">
            {/* Sidebar */}
            <aside className={`w-64 border-r bg-muted/10 p-4 flex flex-col transition-colors duration-300 ${theme !== "dark" && "shadow-[2px_0_10px_rgba(0,0,0,0.15)]"}`}>
                {/* Top section */}
                <div className="flex items-center gap-2 mb-6 justify-between">
                    <h2 className="text-xl font-semibold">Pond</h2>
                    <ThemeToggle />
                </div>

                {/* Search + Account */}
                <div className="mb-4">
                    <Input placeholder="Search" className="mb-3 transition-colors duration-300" />

                    <Button
                        variant="ghost"
                        style={{ cursor: 'pointer' }}
                        className="!p-0.5 !px- !py-0 w-full justify-between transition-colors duration-300 mb-3"
                    >
                        <div className="h-7 w-7 bg-primary/20 rounded-full flex items-center justify-center transition-colors duration-300">
                            <ImageIcon className="h-4 w-4 text-primary transition-colors duration-300" />
                        </div>
                        <span>My Account</span>
                        <ChevronRight className="transition-colors duration-300" />
                    </Button>

                    <Button
                        variant="ghost"
                        style={{ cursor: 'pointer' }}
                        className="!p-0.5 !px- !py-0 w-full justify-between transition-colors duration-300"

                    >
                        <div className="h-7 w-7 bg-primary/20 rounded-full flex items-center justify-center transition-colors duration-300">
                            <Tag className="h-4 w-4 text-primary transition-colors duration-300" />
                        </div>
                        <a href="dashboard/you/selling">Selling</a>
                        <ChevronRight className="transition-colors duration-300" />
                    </Button>
                </div>

                {/* Create Listing */}
                <CreateListingModal onSuccess={GetListings} />

                <Separator className="my-4 transition-colors duration-300" />

                {/* Filters */}
                <ScrollArea className="flex-1 pr-2 transition-colors duration-300">
                    <div className="space-y-6">
                        <div>
                            <Label>Sort by</Label>
                            <select className="w-full border mt-1 rounded-md bg-background p-2 text-sm transition-colors duration-300">
                                <option>Price (Low → High)</option>
                                <option>Price (High → Low)</option>
                                <option>Date Listed</option>
                            </select>
                        </div>

                        <div>
                            <Label>Price</Label>
                            <div className="flex gap-2 mt-2">
                                <Input placeholder="Min" className="w-1/2 transition-colors duration-300" />
                                <Input placeholder="Max" className="w-1/2 transition-colors duration-300" />
                            </div>
                        </div>

                        {/* Categories */}
                        <div>
                            <Label>Category</Label>
                            <div className="flex flex-col gap-1 mt-2">
                                {[
                                    { name: "Furniture", icon: Sofa },
                                    { name: "Clothing", icon: Shirt },
                                    { name: "Housing", icon: Home },
                                    { name: "Tech", icon: Laptop },
                                    { name: "School", icon: GraduationCap },
                                ].map(({ name, icon: Icon }) => (
                                    <Link
                                        key={name}
                                        href={`/dashboard/${name.toLowerCase()}`}
                                        className="flex items-center gap-2 text-sm py-1 px-2 rounded-md hover:bg-muted transition-colors duration-300"
                                    >
                                        <Icon className="h-4 w-4 text-muted-foreground transition-colors duration-300" />
                                        {name}
                                    </Link>
                                ))}
                            </div>
                        </div>
                    </div>
                </ScrollArea>
            </aside>

            {/* Main content */}
            <main className="flex-1 overflow-y-auto p-6 transition-colors duration-300">
                {loading ? (
                    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-6">
                        {Array.from({ length: 12 }).map((_, i) => (
                            <Card key={i} className="transition-colors duration-300">
                                <Skeleton className="h-40 w-full rounded-t-md transition-colors duration-300" />
                                <CardContent className="p-3">
                                    <Skeleton className="h-4 w-3/4 mb-2 transition-colors duration-300" />
                                    <Skeleton className="h-4 w-1/2 transition-colors duration-300" />
                                </CardContent>
                            </Card>
                        ))}
                    </div>
                ) : (
                    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-6">
                        {listings.map((item) => (
                            <ListingCard key={item.listingGU} item={item} />
                        ))}
                    </div>
                )}
            </main>
        </div>
    );
}

function ListingCard({ item }: { item: Listing }) {
    const [hasError, setHasError] = useState(false);
    const { theme } = useTheme();

    return (
        <Card className={`${theme !== 'dark' && 'hover:shadow-lg'} hover:underline hover:-translate-y-1 transition-all duration-300 ease-[cubic-bezier(0.25,1,0.5,1)] cursor-pointer`}>
            <div
                className="relative h-40 w-full flex items-center justify-center overflow-hidden transition-colors duration-300"
                style={{
                    backgroundColor: theme === "dark" ? "#111111" : "#ededed",
                    transition: "background-color 300ms ease-in-out",
                }}
            >
                {!item.picture1_url || hasError ? (
                    <ImageIcon className="h-10 w-10 text-muted-foreground transition-colors duration-300" />
                ) : (
                    <Image
                        src={item.picture1_url}
                        alt={item.title}
                        fill
                        className="object-cover transition-colors duration-300"
                        onError={() => setHasError(true)}
                        priority
                    />
                )}
            </div>
            <CardContent className="p-3">
                <p className="font-medium">${item.price.toLocaleString()}</p>
                <p className="text-md text-muted-foreground">{item.title}</p>
            </CardContent>
        </Card>
    );
}
