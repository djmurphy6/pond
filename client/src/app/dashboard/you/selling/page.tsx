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
    ArrowLeft,
    Menu,
} from "lucide-react";
import { MoreVertical, Pencil, Trash2 } from "lucide-react";

// API
import api from "@/api/WebService";
import { ErrorResponse, Listing } from "@/api/WebTypes";

//ShadCN
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import {
    DropdownMenu,
    DropdownMenuTrigger,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuSeparator,
} from "@/components/ui/dropdown-menu";
import { toast } from "sonner";

//Internal
import ThemeToggle from "@/components/ThemeToggle";
import { CreateListingModal } from "@/components/CreateListingModal";
import EditListingModal from "./Components/EditListingModal";
import DeleteListingModal from "./Components/DeleteListingModal";
import { MyAccountPopover } from "@/components/MyAccountPopover";
import { Sheet, SheetContent, SheetTitle } from "@/components/ui/sheet";
import MobileHeader from "@/components/MobileHeader";
import { SideBarAside } from "@/components/SideBarAside";

export default function SellingPage() {
    const [listings, setListings] = useState<Listing[]>([]);
    const [mounted, setMounted] = useState(false);
    const [loading, setLoading] = useState(true);
    const { theme } = useTheme();

    const [editItem, setEditItem] = useState<Listing | undefined>();
    const [deleteItem, setDeleteItem] = useState<Listing | undefined>();

    const [showSidebar, setShowSidebar] = useState(false);

    useEffect(() => {
        setMounted(true);
    }, []);

    useEffect(() => {
        if (mounted) {
            GetMyListings();
        }
    }, [mounted]);

    async function GetMyListings() {
        setLoading(true);
        let res = await api.GetMyListings();
        setLoading(false);
        if (res instanceof ErrorResponse) {
            toast.error(res.body?.error);
        } else {
            console.log(JSON.stringify(res))
            setListings(res);
        }
        // setListings([{
        //     title: 'test',
        //     price: 20,
        //     description: 'something random that costs $20 because im testing this description field',
        //     userGU: "",
        //     listingGU: "l",
        //     picture1_url: "",
        //     picture2_url: "",
        //     condition: "Used"
        // }])
        setLoading(false);
    }

    const SideBar = () => (
        <SideBarAside>
            <Button variant={'link'} style={{ color: 'gray', justifyContent: 'flex-start' }} className="!p-0 !px-0 !py-0 hover:underline hover:bg-none cursor-pointer">
                <Link style={{ flexDirection: 'row' }} className="flex items-center gap-1" href="/dashboard">
                    <ArrowLeft />
                    back to dashboard
                </Link>
            </Button>

            {/* Top section */}
            <div className="flex items-center gap-2 mb-6 justify-between">
                <h2 className="text-xl font-semibold">Manage Listings</h2>
                {/* <ThemeToggle /> */}
            </div>

            {/* Account */}
            <div className="mb-4">
                <MyAccountPopover />
            </div>

            {/* Create Listing */}
            <CreateListingModal onSuccess={GetMyListings} />
        </SideBarAside>
    )

    if (!mounted) return null;
    return (
        <div className={`flex flex-col md:flex-row h-screen ${theme !== "dark" ? "bg-[#ffffff]" : "bg-muted/30"} transition-colors duration-300`}>
            {/* Sidebar */}
            <div className="hidden md:flex">
                <SideBar />
            </div>

            <Sheet open={showSidebar} onOpenChange={setShowSidebar}>
                <SheetTitle className="sr-only">My Listings</SheetTitle>
                <SheetContent side="left" className="w-64">
                    <SideBar />
                </SheetContent>
            </Sheet>

            <MobileHeader onPress={setShowSidebar} />

            {/* Main content */}
            <main className="flex-1 overflow-y-auto p-6 bg-background transition-colors duration-300">
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
                            <ListingCard key={item.listingGU} item={item} openEditModal={setEditItem} openDeleteModal={setDeleteItem} />
                        ))}
                    </div>
                )}
            </main>

            <EditListingModal
                item={editItem}
                onClose={() => setEditItem(undefined)}
                onSave={GetMyListings}
            />

            <DeleteListingModal
                item={deleteItem}
                onClose={() => setDeleteItem(undefined)}
                onDelete={GetMyListings}
            />

        </div>
    );
}

function ListingCard({ item, openEditModal, openDeleteModal }: { item: Listing, openEditModal: (item: Listing) => void, openDeleteModal: (item: Listing) => void }) {
    const [hasError, setHasError] = useState(false);
    const { theme } = useTheme();

    return (
        <Card
            className={`${theme !== 'dark' && 'hover:shadow-lg'} relative group hover:-translate-y-1 transition-all duration-300 ease-[cubic-bezier(0.25,1,0.5,1)]`}
        >
            <Button
                variant="outline"
                className="absolute left-2 top-2 opacity-100 group-hover:opacity-100 transition-opacity duration-300 cursor-pointer"
                onClick={() => openEditModal(item)}
            >
                <Pencil className="h-4 w-4 mr-2" /> Edit
            </Button>
            <Button
                variant="outline"
                className="absolute right-2 top-2 opacity-100 group-hover:opacity-100 transition-opacity duration-300 cursor-pointer"
                onClick={() => openDeleteModal(item)}
            >
                <Trash2 className="h-4 w-4 mr-2 text-destructive" /> Delete
            </Button>

            {/* Listing thumbnail */}
            <div
                className="relative mt-7 h-40 w-full flex items-center justify-center overflow-hidden transition-colors duration-300"
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

