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
} from "lucide-react";

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

interface Listing {
    id: number;
    title: string;
    price: number;
    image?: string;
    category: string;
    condition: string;
}

export default function DashboardPage() {
    const [listings, setListings] = useState<Listing[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Simulate API delay
        setTimeout(() => {
            setListings([
                { id: 1, title: "Mini Fridge", price: 35, category: "Tech", condition: "Used" },
                { id: 2, title: "Panasonic Bike", price: 150, category: "Tech", condition: "Used" },
                { id: 3, title: "Tie-dye UO Jersey", price: 65, category: "Clothing", condition: "Like New" },
                { id: 4, title: "LG Smart TV", price: 150, category: "Tech", condition: "Used" },
                { id: 5, title: "Small Desk with Storage", price: 45, category: "Furniture", condition: "Good" },
                { id: 6, title: "Golden Pothos", price: 20, category: "Housing", condition: "New" },
                { id: 7, title: "Olympus Stylus Film Camera", price: 109, category: "Tech", condition: "Used" },
                { id: 8, title: "Set of Plates & Bowls", price: 15, category: "Housing", condition: "Good" },
            ]);
            setLoading(false);
        }, 1500);
    }, []);

    return (
        <div className="flex h-screen bg-muted/10 transition-colors duration-300">
            {/* Sidebar */}
            <aside className="w-64 border-r bg-background p-4 flex flex-col transition-colors duration-300">
                {/* Top section */}
                <div className="flex items-center gap-2 mb-6 justify-between">
                    <h2 className="text-lg font-semibold">Pond</h2>
                    <ThemeToggle />
                </div>

                {/* Search + Account */}
                <div className="mb-4">
                    <Input placeholder="Search" className="mb-3 transition-colors duration-300" />
                    <Button
                        variant="outline"
                        className="w-full justify-between transition-colors duration-300"
                    >
                        <div className="h-7 w-7 bg-primary/20 rounded-full flex items-center justify-center transition-colors duration-300">
                            <ImageIcon className="h-4 w-4 text-primary transition-colors duration-300" />
                        </div>
                        <span>Your Account</span>
                        <ChevronRight className="h-4 w-4 transition-colors duration-300" />
                    </Button>
                </div>

                {/* Create Listing */}
                <Button
                    style={{ cursor: "pointer" }}
                    className="mb-4 w-full bg-[var(--uo-green)] text-white hover:bg-[rgba(0,0,0,0.6)] transition-colors duration-300"
                >
                    + Create New Listing
                </Button>

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
                        {Array.from({ length: 8 }).map((_, i) => (
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
                            <ListingCard key={item.id} item={item} />
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
        <Card className="hover:shadow-md transition-shadow transition-colors duration-300 cursor-pointer">
            <div
                className="relative h-40 w-full flex items-center justify-center rounded-t-md overflow-hidden transition-colors duration-300"
                style={{
                    backgroundColor: theme === "dark" ? "#1f1f1f" : "#f4f4f4",
                    transition: "background-color 300ms ease-in-out",
                }}
            >
                {!item.image || hasError ? (
                    <ImageIcon className="h-10 w-10 text-muted-foreground transition-colors duration-300" />
                ) : (
                    <Image
                        src={item.image}
                        alt={item.title}
                        fill
                        className="object-cover transition-colors duration-300"
                        onError={() => setHasError(true)}
                        priority
                    />
                )}
            </div>
            <CardContent className="p-3">
                <p className="font-medium">{item.title}</p>
                <p className="text-sm text-muted-foreground">${item.price}</p>
            </CardContent>
        </Card>
    );
}
