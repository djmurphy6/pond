"use client";

import { useEffect, useState } from "react";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Bookmark, ImageIcon } from "lucide-react";
import { toast } from "sonner";
import api from "@/api/WebService";
import { ErrorResponse, Listing } from "@/api/WebTypes";
import ListingCard from "./ListingCard";
import { Skeleton } from "./ui/skeleton";
import { Card, CardContent } from "./ui/card";
import { ScrollArea } from "./ui/scroll-area";

export function SavedListingsModal() {
    const [open, setOpen] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [savedListings, setSavedListings] = useState<Listing[]>([]);

    useEffect(() => {
        if (open) {
            fetchSavedListings();
        }
    }, [open]);

    const fetchSavedListings = async () => {
        setIsLoading(true);
        const res = await api.GetSavedListings();
        setIsLoading(false);

        if (res instanceof ErrorResponse) {
            toast.error("Failed to fetch saved listings: " + res.body?.error);
        } else {
            setSavedListings(res);
        }
    };

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button
                    variant="ghost"
                    style={{ cursor: 'pointer' }}
                    className="!p-0.5 !px- !py-0 w-full justify-between transition-colors duration-300 mb-3"
                >
                    <div className="h-7 w-7 bg-primary/20 rounded-full flex items-center justify-center transition-colors duration-300">
                        <Bookmark className="h-4 w-4 text-primary transition-colors duration-300" />
                    </div>
                    <span>Saved Listings</span>
                    <div className="w-5" /> {/* Spacer for alignment */}
                </Button>
            </DialogTrigger>

            <DialogContent className="sm:max-w-4xl max-h-[80vh]">
                <DialogHeader>
                    <DialogTitle>Saved Listings</DialogTitle>
                </DialogHeader>

                <ScrollArea className="h-[60vh] pr-4">
                    {isLoading ? (
                        <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                            {Array.from({ length: 6 }).map((_, i) => (
                                <Card key={i} className="transition-colors duration-300 py-0">
                                    <Skeleton className="h-40 w-full rounded-t-md transition-colors duration-300" />
                                    <CardContent className="p-3">
                                        <Skeleton className="h-4 w-3/4 mb-2 transition-colors duration-300" />
                                        <Skeleton className="h-4 w-1/2 transition-colors duration-300" />
                                    </CardContent>
                                </Card>
                            ))}
                        </div>
                    ) : savedListings.length === 0 ? (
                        <div className="flex flex-col items-center justify-center h-64 text-muted-foreground">
                            <Bookmark className="h-16 w-16 mb-4" />
                            <p className="text-lg">No saved listings</p>
                            <p className="text-sm">Start saving listings to see them here</p>
                        </div>
                    ) : (
                        <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                            {savedListings.map((item, index) => (
                                <ListingCard 
                                    key={item.listingGU} 
                                    item={item} 
                                    index={index}
                                />
                            ))}
                        </div>
                    )}
                </ScrollArea>
            </DialogContent>
        </Dialog>
    );
}

