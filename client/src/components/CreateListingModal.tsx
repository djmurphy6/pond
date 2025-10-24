"use client";

// API
import api from "@/api/WebService";

import { toast } from "sonner"

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
import { Loader2 } from "lucide-react";
import { CreateListingRequest, ErrorResponse } from "@/api/WebTypes";

// Type definition matching your ListingDTO
export interface ListingDTO {
    listingGU: string;
    userGU: string;
    description: string;
    picture1_url: string;
    picture2_url: string;
    price: number | null;
    condition: string;
}

export function CreateListingModal(props: { onSuccess?: () => void }) {
    const [open, setOpen] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

    const [title, setTitle] = useState<string>("");
    const [description, setDescription] = useState<string>("");
    const [picture1_url, setPicture1Url] = useState<string>();
    const [picture2_url, setPicture2Url] = useState<string>();
    const [price, setPrice] = useState<number | "">("");
    const [condition, setCondition] = useState<string>("");

    useEffect(() => {
        setTitle("");
        setDescription("");
        setPicture1Url("");
        setPicture2Url("");
        setPrice("");
        setCondition("");
    }, [open]);

    const handleSubmit = async () => {
        const listing: CreateListingRequest = {
            description,
            picture1_url,
            picture2_url,
            price: price === "" ? 0 : Number(price),
            condition,
            title,
        };

        console.log("Submitting listing:", listing);
        // TODO: send to backend
        setIsLoading(true);
        let res = await api.CreateListing(listing);
        setIsLoading(false);
        if (res instanceof ErrorResponse) {
            toast.error("Create Listing Error:" + res.body?.error);
        } else {
            //TOAST
            toast.success("Successfully created listing");
            props.onSuccess?.();
            setOpen(false);
        }
    };

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button
                    style={{ cursor: "pointer" }}
                    className="mb-4 w-full bg-[var(--uo-green)] hover:bg-[var(--uo-green)]/70 text-white transition-colors duration-300"
                >
                    + Create New Listing
                </Button>
            </DialogTrigger>

            <DialogContent className="sm:max-w-md">
                <DialogHeader>
                    <DialogTitle>Create New Listing</DialogTitle>
                </DialogHeader>

                <div className="grid gap-4 py-4">

                    {/* Title */}
                    <div className="grid grid-cols-4 items-center gap-2">
                        <Label htmlFor="title" className="text-right">
                            Title
                        </Label>
                        <Input
                            id="title"
                            value={title}
                            onChange={(e: any) => setTitle(e.target.value)}
                            placeholder="Title"
                            className="col-span-3"
                        />
                    </div>

                    {/* Description */}
                    <div className="grid grid-cols-4 items-center gap-2">
                        <Label htmlFor="description" className="text-right">
                            Description
                        </Label>
                        <Textarea
                            id="description"
                            value={description}
                            onChange={(e: any) => setDescription(e.target.value)}
                            placeholder="Describe your item"
                            className="col-span-3"
                        />
                    </div>

                    {/* Price */}
                    <div className="grid grid-cols-4 items-center gap-2">
                        <Label htmlFor="price" className="text-right">
                            Price ($)
                        </Label>
                        <Input
                            id="price"
                            type="number"
                            value={price}
                            onChange={(e) =>
                                setPrice(e.target.value ? Number(e.target.value) : "")
                            }
                            placeholder="100"
                            className="col-span-3"
                        />
                    </div>

                    {/* Condition */}
                    <div className="grid grid-cols-4 items-center gap-2">
                        <Label htmlFor="condition" className="text-right">
                            Condition
                        </Label>
                        <Input
                            id="condition"
                            value={condition}
                            onChange={(e) => setCondition(e.target.value)}
                            placeholder="New / Used / Like New"
                            className="col-span-3"
                        />
                    </div>
                </div>

                <DialogFooter>
                    <Button
                        onClick={handleSubmit}
                        className="
              bg-[var(--uo-green)] 
              text-white 
              hover:bg-[color-mix(in_srgb,var(--uo-green)_85%,black)] 
              transition-colors duration-300
            "
                    >
                        {isLoading ? (
                            <>
                                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                Submitting...
                            </>
                        ) : (
                            "Submit"
                        )}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
