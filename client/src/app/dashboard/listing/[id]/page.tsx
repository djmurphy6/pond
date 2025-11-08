"use client"

//Next
import Image from "next/image";
import Link from "next/link";
import { useParams } from "next/navigation";
import { useTheme } from "next-themes";

//React
import { Bookmark, MessageCircle, MoreHorizontal, Share2, X, Pencil, Trash2, Shield } from "lucide-react";
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

//Internal
import { useUserInfoStore } from "@/stores/UserInfoStore";
import EditListingModal from "../../you/selling/Components/EditListingModal";
import DeleteListingModal from "../../you/selling/Components/DeleteListingModal";
import { useRouter } from "next/navigation";
import { MessageSellerModal } from "@/components/MessageSellerModal";

export default function ListingPage() {
    const params = useParams() as { id: string };
    const [listing, setListing] = useState<Listing>();
    const { theme } = useTheme();
    const [mounted, setMounted] = useState(false);
    const [copied, setCopied] = useState(false);
    const { userInfo } = useUserInfoStore();
    const router = useRouter();

    const [editItem, setEditItem] = useState<Listing | undefined>();
    const [deleteItem, setDeleteItem] = useState<Listing | undefined>();

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

    const refetchListing = async () => {
        let res = await api.GetSpecificListing({ listingGU: params.id });
        if (res instanceof ErrorResponse) {
            toast.error(res.body?.error);
        } else {
            setListing(res);
        }
    };

    const handleDeleteSuccess = () => {
        toast.success("Listing deleted successfully");
        router.push("/dashboard");
    };

    // Check if current user is owner or admin
    const isOwner = userInfo?.userGU === listing?.userGU;
    const isAdmin = userInfo?.admin === true;
    const canModify = isOwner || isAdmin;

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
                    <div className="flex items-center gap-2">
                        <h2 className="text-xl font-semibold">Pond</h2>
                        {isAdmin && (
                            <div className="flex items-center gap-1 px-2 py-0.5 bg-amber-500/20 text-amber-600 dark:text-amber-400 rounded-md text-xs font-medium">
                                <Shield className="h-3 w-3" />
                                Admin
                            </div>
                        )}
                    </div>
                    <ThemeToggle />
                </div>


                {/* Listing Title and Price */}
                <div className="my-6">
                    <h2 className="text-2xl font-bold leading-tight">{listing.title}</h2>
                    <div className="flex items-center gap-2 mt-1">
                        <span className="text-lg font-semibold">${listing.price.toLocaleString()}</span>
                    </div>
                    {listing.username && (
                        <p className="text-sm text-muted-foreground mt-2">
                            Listed by <span className="font-medium text-foreground">@{listing.username}</span>
                        </p>
                    )}
                </div>


                {/* Buttons */}
                <div className="flex gap-2 mb-2">
                    <MessageSellerModal
                        listingId={params.id}
                        username={listing.username}
                        image={listing.picture1_url}
                        title={listing.title}
                        price={listing.price}
                    />
                    <Button variant="outline" size="icon" className="cursor-pointer">
                        <Bookmark className="h-4 w-4" />
                    </Button>
                    <Button onClick={handleCopy} variant="outline" size="icon" className="cursor-pointer">
                        <Share2 className="h-4 w-4" />
                    </Button>
                </div>

                {/* Admin/Owner Controls */}
                {canModify && (
                    <>
                        <Separator className="my-2" />
                        <div className="space-y-2">
                            {isAdmin && !isOwner && (
                                <p className="text-xs text-amber-600 dark:text-amber-400 font-medium mb-2">
                                    Admin Controls
                                </p>
                            )}
                            <Button
                                variant="outline"
                                className="w-full cursor-pointer"
                                onClick={() => setEditItem(listing)}
                            >
                                <Pencil className="mr-2 h-4 w-4" /> Edit Listing
                            </Button>
                            <Button
                                variant="outline"
                                className="w-full cursor-pointer text-destructive hover:text-destructive"
                                onClick={() => setDeleteItem(listing)}
                            >
                                <Trash2 className="mr-2 h-4 w-4" /> Delete Listing
                            </Button>
                        </div>
                    </>
                )}


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

            <EditListingModal
                item={editItem}
                onClose={() => setEditItem(undefined)}
                onSave={refetchListing}
            />

            <DeleteListingModal
                item={deleteItem}
                onClose={() => setDeleteItem(undefined)}
                onDelete={handleDeleteSuccess}
            />
        </div>
    );
}
