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
    AlertCircle,
    Menu,
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
import ListingCard from "@/components/ListingCard";
import { Sheet, SheetContent, SheetTitle } from "@/components/ui/sheet";

export default function DashboardPage() {
    const [listings, setListings] = useState<Listing[]>([]);
    const [mounted, setMounted] = useState(false);
    const [loading, setLoading] = useState(true);
    const { theme } = useTheme();
    const { userInfo } = useUserInfoStore();

    // Filtering and sorting state
    const [selectedCategories, setSelectedCategories] = useState<string[]>([]);
    const [sortOption, setSortOption] = useState<string>("date-desc");
    const [minPrice, setMinPrice] = useState<string>("");
    const [maxPrice, setMaxPrice] = useState<string>("");
    const [searchQuery, setSearchQuery] = useState<string>("");

    //Mobile
    const [showSidebar, setShowSidebar] = useState<boolean>(false);

    useEffect(() => {
        setMounted(true);
    }, []);

    // Fetch listings when filters change (with debounce for price inputs and search)
    useEffect(() => {
        if (!mounted) return;

        // Debounce price inputs and search to avoid too many API calls while typing
        const timeoutId = setTimeout(() => {
            GetListings();
        }, 300); // 300ms debounce

        return () => clearTimeout(timeoutId);
    }, [selectedCategories, sortOption, minPrice, maxPrice, searchQuery, mounted]);

    async function GetListings() {
        try {

            setLoading(true);

            // Parse sort option (e.g., "date-desc" -> sortBy: "date", sortOrder: "desc")
            const [sortBy, sortOrder] = sortOption.split('-');

            // Prepare filter parameters
            const filters = {
                categories: selectedCategories.length > 0 ? selectedCategories : undefined,
                minPrice: minPrice ? parseFloat(minPrice) : undefined,
                maxPrice: maxPrice ? parseFloat(maxPrice) : undefined,
                sortBy,
                sortOrder,
                searchQuery: searchQuery.trim() || undefined,
            };

            let res = await api.GetListings(filters);
            setLoading(false);
            if (res instanceof ErrorResponse) {
                toast.error(res.body?.error);
            } else {
                console.log(JSON.stringify(res))
                setListings(res);
            }
        } catch (error) {
            setLoading(false);
            toast.error("Failed to fetch listings. Please try again.");
        }
    }

    // Toggle category selection
    const toggleCategory = (category: string) => {
        setSelectedCategories(prev =>
            prev.includes(category)
                ? prev.filter(c => c !== category) //Remove if already selected
                : [...prev, category] // Add if not selected
        );
    };

    if (!mounted) return null;
    return (
        <div className="flex flex-col md:flex-row h-screen bg-background transition-colors duration-300">
            {/* Sidebar */}
            <div className="hidden md:flex h-full">
                <SideBar
                    theme={theme}
                    GetListings={GetListings}
                    toggleCategory={toggleCategory}
                    searchQuery={searchQuery}
                    setSearchQuery={setSearchQuery}
                    selectedCategories={selectedCategories}
                    setSelectedCategories={setSelectedCategories}
                    sortOption={sortOption}
                    setSortOption={setSortOption}
                    minPrice={minPrice}
                    setMinPrice={setMinPrice}
                    maxPrice={maxPrice}
                    setMaxPrice={setMaxPrice}
                />
            </div>

            <Sheet open={showSidebar} onOpenChange={setShowSidebar}>
                <SheetTitle className="sr-only">Pond</SheetTitle>
                <SheetContent side="left" className="w-64 h-full">
                    <SideBar
                        theme={theme}
                        GetListings={GetListings}
                        toggleCategory={toggleCategory}
                        searchQuery={searchQuery}
                        setSearchQuery={setSearchQuery}
                        selectedCategories={selectedCategories}
                        setSelectedCategories={setSelectedCategories}
                        sortOption={sortOption}
                        setSortOption={setSortOption}
                        minPrice={minPrice}
                        setMinPrice={setMinPrice}
                        maxPrice={maxPrice}
                        setMaxPrice={setMaxPrice}
                    />
                </SheetContent>
            </Sheet>

            <div className="flex md:hidden items-center justify-between p-4 border-b bg-muted/40 sticky top-0 z-20">
                <button onClick={() => setShowSidebar(true)} className="flex items-center gap-2">
                    <Menu className="h-8 w-8" />
                </button>
                <span className="font-bold text-2xl">Pond</span>
                <ThemeToggle />
            </div>

            {/* Main content */}
            <main className="flex-1 overflow-y-auto p-6 transition-colors duration-300">
                {loading ? (
                    <div className="grid grid-cols-2 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-6">
                        {Array.from({ length: 12 }).map((_, i) => (
                            <Card key={i} className="transition-colors duration-300 py-0">
                                <Skeleton className="h-40 w-full rounded-t-md transition-colors duration-300" />
                                <CardContent className="p-3">
                                    <Skeleton className="h-4 w-3/4 mb-2 transition-colors duration-300" />
                                    <Skeleton className="h-4 w-1/2 transition-colors duration-300" />
                                </CardContent>
                            </Card>
                        ))}
                    </div>
                ) : listings.length === 0 ? (
                    <div className="flex flex-col items-center justify-center h-64 text-muted-foreground">
                        <ImageIcon className="h-16 w-16 mb-4" />
                        <p className="text-lg">No listings found</p>
                        <p className="text-sm">Try adjusting your filters</p>
                    </div>
                ) : (
                    <div className="grid grid-cols-2 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-6">
                        {listings.map((item, index) => (
                            <ListingCard key={item.listingGU} item={item} index={index} />
                        ))}
                    </div>
                )}
            </main>
        </div>
    );
}

type SideBarProps = {
    theme?: string;
    GetListings: () => Promise<void>;
    toggleCategory: (category: string) => void;

    searchQuery: string;
    setSearchQuery: React.Dispatch<React.SetStateAction<string>>;

    selectedCategories: string[];
    setSelectedCategories: React.Dispatch<React.SetStateAction<string[]>>;

    sortOption: string
    setSortOption: React.Dispatch<React.SetStateAction<string>>

    minPrice: string
    setMinPrice: React.Dispatch<React.SetStateAction<string>>

    maxPrice: string
    setMaxPrice: React.Dispatch<React.SetStateAction<string>>
}

const SideBar = (props: SideBarProps) => {
    const { theme, GetListings, toggleCategory, searchQuery, setSearchQuery, selectedCategories, setSelectedCategories, sortOption, setSortOption, minPrice, setMinPrice, maxPrice, setMaxPrice } = props;

    const { userInfo } = useUserInfoStore()

    return (
        <aside className={`w-64 h-full border-r bg-muted/30 p-0 flex flex-col transition-colors duration-300 ${theme !== "dark" && "shadow-[2px_0_10px_rgba(0,0,0,0.15)]"}`}>
            {/* Top section */}
            <div className="flex items-center gap-2 justify-between p-4">
                <h2 className="text-xl font-semibold">Pond</h2>
                <div className="hidden md:block">
                    <ThemeToggle />
                </div>
            </div>
            <div className="p-4 overflow-y-auto">
                {/* Search + Account + Messaging */}
                <div className="mb-4">
                    <Input
                        placeholder="Search listings..."
                        className="mb-3 transition-colors duration-300"
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                    />

                    <MyAccountPopover />

                    <Link href="/dashboard/you/selling" className="w-full">
                        <Button
                            variant="ghost"
                            style={{ cursor: 'pointer' }}
                            className="!p-0.5 !px- !py-0 w-full justify-between transition-colors duration-300 mb-3"
                        >
                            <div className="h-7 w-7 bg-primary/20 rounded-full flex items-center justify-center transition-colors duration-300">
                                <Tag className="h-4 w-4 text-primary transition-colors duration-300" />
                            </div>
                            <span>Selling</span>
                            <ChevronRight className="transition-colors duration-300" />
                        </Button>
                    </Link>

                    <Link href="/dashboard/messaging" className="w-full">
                        <Button
                            variant="ghost"
                            style={{ cursor: 'pointer' }}
                            className="!p-0.5 !px- !py-0 w-full justify-between transition-colors duration-300"
                        >
                            <div className="h-7 w-7 bg-primary/20 rounded-full flex items-center justify-center transition-colors duration-300">
                                <MessageCircle className="h-4 w-4 text-primary transition-colors duration-300" />
                            </div>
                            <span>Messages</span>
                            <ChevronRight className="transition-colors duration-300" />
                        </Button>
                    </Link>

                    <Link href="/dashboard/you/reports" className="w-full">
                        <Button
                            variant="ghost"
                            style={{ cursor: 'pointer' }}
                            className="!p-0.5 !px- !py-0 w-full justify-between transition-colors duration-300 mt-3"
                        >
                            <div className="h-7 w-7 bg-primary/20 rounded-full flex items-center justify-center transition-colors duration-300">
                                <AlertCircle className="h-4 w-4 text-primary transition-colors duration-300" />
                            </div>
                            <span>Reports</span>
                            <ChevronRight className="transition-colors duration-300" />
                        </Button>
                    </Link>

                    {/* Admin Panel Link - Only visible to admins */}
                    {userInfo?.admin && (
                        <Link href="/dashboard/admin/reports" className="w-full">
                            <Button
                                variant="ghost"
                                style={{ cursor: 'pointer' }}
                                className="!p-0.5 !px- !py-0 w-full justify-between transition-colors duration-300 mb-3 mt-3"
                            >
                                <div className="h-7 w-7 bg-amber-500/20 rounded-full flex items-center justify-center transition-colors duration-300">
                                    <Shield className="h-4 w-4 text-amber-600 transition-colors duration-300" />
                                </div>
                                <span className="text-amber-600 dark:text-amber-400 font-medium">Admin Panel</span>
                                <ChevronRight className="text-amber-600 dark:text-amber-400 transition-colors duration-300" />
                            </Button>
                        </Link>
                    )}
                </div>


                {/* Create Listing */}
                <CreateListingModal onSuccess={GetListings} />

                <Separator className="my-4 transition-colors duration-300" />

                {/* Filters */}
                <div className="transition-colors duration-300">
                    <div className="space-y-6">
                        <div className="flex items-center justify-between">
                            <Label>Filters</Label>
                            {(selectedCategories.length > 0 || minPrice || maxPrice || searchQuery) && (
                                <Button
                                    variant="ghost"
                                    size="sm"
                                    onClick={() => {
                                        setSelectedCategories([]);
                                        setMinPrice("");
                                        setMaxPrice("");
                                        setSearchQuery("");
                                    }}
                                    className="h-6 text-xs"
                                >
                                    Clear
                                </Button>
                            )}
                        </div>

                        <div>
                            <Label className="mb-2">Sort by</Label>
                            <Select value={sortOption} onValueChange={setSortOption}>
                                <SelectTrigger className="w-full mt-2">
                                    <SelectValue placeholder="Sort by" />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="date-desc">Date Listed (Newest)</SelectItem>
                                    <SelectItem value="date-asc">Date Listed (Oldest)</SelectItem>
                                    <SelectItem value="price-asc">Price (Low → High)</SelectItem>
                                    <SelectItem value="price-desc">Price (High → Low)</SelectItem>
                                </SelectContent>
                            </Select>
                        </div>

                        <div>
                            <Label>Price</Label>
                            <div className="flex gap-2 mt-2">
                                <Input
                                    type="number"
                                    placeholder="Min"
                                    className="w-1/2 transition-colors duration-300"
                                    value={minPrice}
                                    onChange={(e) => setMinPrice(e.target.value)}
                                />
                                <Input
                                    type="number"
                                    placeholder="Max"
                                    className="w-1/2 transition-colors duration-300"
                                    value={maxPrice}
                                    onChange={(e) => setMaxPrice(e.target.value)}
                                />
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
                                ].map(({ name, icon: Icon }) => {
                                    const isSelected = selectedCategories.includes(name);
                                    return (
                                        <button
                                            key={name}
                                            onClick={() => toggleCategory(name)}
                                            className={`flex items-center gap-2 text-sm py-1 px-2 rounded-md hover:bg-muted transition-colors duration-300 ${isSelected ? 'bg-muted' : ''
                                                }`}
                                        >
                                            <Icon className="h-4 w-4 text-muted-foreground transition-colors duration-300" />
                                            <span className="flex-1 text-left">{name}</span>
                                            {isSelected && (
                                                <Check className="h-4 w-4 text-primary transition-colors duration-300" />
                                            )}
                                        </button>
                                    );
                                })}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </aside>
    )
}
