"use client";

import { useEffect, useState } from "react";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
    DialogFooter,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Loader2, X, ImageIcon } from "lucide-react";
import { toast } from "sonner";
import api from "@/api/WebService";
import { CreateListingRequest, ErrorResponse, Listing } from "@/api/WebTypes";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "./ui/select";
import { Separator } from "./ui/separator";
import ListingCard from "./ListingCard";

type UserDetailsModalProps = {
    userGU: string;
    username: string,
    avatar_url?: string,
    onSuccess?: () => void
}

export function UserDetailsModal(props: UserDetailsModalProps) {
    const { userGU, username, avatar_url, onSuccess } = props;

    const [open, setOpen] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

    const [listings, setListings] = useState<Listing[]>([]);

    useEffect(() => {
        async function fetchListings() {
            setIsLoading(true);
            const response = await api.GetSpecificUserListings({ userGU: userGU });
            setIsLoading(false);
            if (response instanceof ErrorResponse) {
                toast.error(response.body?.error);
                return;
            } else {
                setListings(response);
            }
        }
        fetchListings();
    }, [userGU]);

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <button onClick={() => { }} className="flex flex-row items-center hover:underline cursor-pointer">
                    <div className="h-10 w-10 bg-primary/20 rounded-full flex items-center justify-center transition-colors duration-300">
                        {avatar_url ? (
                            <img
                                src={avatar_url}
                                alt="Profile"
                                className="w-full h-full object-cover rounded-full"
                            />
                        ) : (
                            <ImageIcon className="h-4 w-4 text-primary transition-colors duration-300" />
                        )}
                    </div>
                    <span className="px-4 items-center">{username}</span>
                </button>
            </DialogTrigger>

            <DialogContent className="sm:max-w-3xl">
                <div className="w-32 h-32 rounded-full overflow-hidden flex items-center justify-center border-3 border-primary">
                    {avatar_url ? (
                        <img
                            src={avatar_url}
                            alt="Profile"
                            className="w-full h-full object-cover"
                        />
                    ) : (
                        <ImageIcon style={{ width: '40px', height: '40px' }} className="text-primary" />
                    )}
                </div>
                <DialogTitle className="text-3xl font-bold">{username}'s Profile</DialogTitle>
                <span className="text-muted-foreground"><span className="font-bold text-primary">{listings.length}</span> active listing{listings.length === 1 ? '' : 's'}</span>

                <Separator className="" />

                <span className="text-primary">{username}'s Listings</span>

                <div className="grid grid-cols-3 gap-4">
                    {listings.map((listing) => (
                        <ListingCard key={listing.listingGU} item={listing} />
                    ))}
                </div>

            </DialogContent>
        </Dialog>
    );
}
