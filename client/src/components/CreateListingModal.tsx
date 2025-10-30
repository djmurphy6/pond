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
import { CreateListingRequest, ErrorResponse } from "@/api/WebTypes";

export function CreateListingModal(props: { onSuccess?: () => void }) {
    const [open, setOpen] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [price, setPrice] = useState<number | string>("");
    const [condition, setCondition] = useState("");

    // Photos
    const [photos, setPhotos] = useState<string[]>([]);

    useEffect(() => {
        if (!open) {
            setTitle("");
            setDescription("");
            setPrice("");
            setCondition("");
            setPhotos([]);
        }
    }, [open]);

    const handlePhotoUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;

        if (photos.length >= 2) {
            toast.error("You can only upload up to 2 photos.");
            return;
        }

        const reader = new FileReader();
        reader.onloadend = () => {
            setPhotos((prev) => [...prev, reader.result as string]);
        };
        reader.readAsDataURL(file);
    };

    const removePhoto = (index: number) => {
        setPhotos((prev) => prev.filter((_, i) => i !== index));
    };

    const handleSubmit = async () => {
        const listing: CreateListingRequest = {
            description,
            picture1_base64: photos[0] || "",
            picture2_base64: photos[1] || "",
            price: price === "" ? 0 : Number(price),
            condition,
            title,
        };

        console.log("Submitting listing:", listing);

        setIsLoading(true);
        let res = await api.CreateListing(listing);
        setIsLoading(false);

        if (res instanceof ErrorResponse) {
            toast.error("Create Listing Error:" + res.body?.error);
        } else {
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
                    className="mb-0 w-full bg-[var(--uo-green)] hover:bg-[var(--uo-green)]/70 text-white transition-colors duration-300"
                >
                    + Create New Listing
                </Button>
            </DialogTrigger>

            <DialogContent className="sm:max-w-md">
                <DialogHeader>
                    <DialogTitle>Create New Listing</DialogTitle>
                </DialogHeader>

                <div className="space-y-4 py-2">
                    {/* Title */}
                    <Label htmlFor="title">Title</Label>
                    <Input
                        id="title"
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        placeholder="Name your item..."
                    />

                    {/* Description */}
                    <Label htmlFor="description">Description</Label>
                    <Textarea
                        id="description"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        placeholder="Describe your item..."
                    />

                    {/* Price */}
                    <Label htmlFor="price">Price</Label>
                    <Input
                        id="price"
                        type="text"
                        value={price ? `$ ${price}` : ""}
                        onChange={(e) => {
                            let raw = e.target.value.replace(/[^0-9.]/g, "");
                            const parts = raw.split(".");
                            if (parts.length > 2) raw = parts[0] + "." + parts.slice(1).join("");

                            setPrice(raw);
                        }}
                        onBlur={(e) => {
                            let raw = e.target.value.replace(/[^0-9.]/g, "");
                            if (raw.includes('.')) {
                                const parts = raw.split(".");
                                if (parts[1] === "") {
                                    setPrice(raw + '0');
                                }
                            }
                        }}
                        placeholder="$ 0"
                    />


                    {/* Condition */}
                    <Label htmlFor="condition">Condition</Label>
                    <Input
                        id="condition"
                        value={condition}
                        onChange={(e) => setCondition(e.target.value)}
                        placeholder="New / Used / Like New"
                    />

                    {/* Photos */}
                    <Label>Photos (max 2)</Label>
                    <div className="flex gap-3 flex-wrap">
                        {photos.map((src, i) => (
                            <div
                                key={i}
                                className="relative w-[100px] h-[100px] rounded-md overflow-hidden border border-border"
                            >
                                <img
                                    src={src}
                                    alt={`Photo ${i + 1}`}
                                    className="w-full h-full object-cover"
                                />
                                <button
                                    onClick={() => removePhoto(i)}
                                    className="absolute top-1 right-1 bg-black/50 hover:bg-black/70 text-white rounded-full p-0.5"
                                >
                                    <X className="h-4 w-4" />
                                </button>
                            </div>
                        ))}

                        {/* Add photo placeholders */}
                        {photos.length < 2 && (
                            <label
                                htmlFor="photo-upload"
                                className="w-[100px] h-[100px] rounded-md flex flex-col items-center justify-center border border-dashed border-muted-foreground/40 cursor-pointer hover:bg-muted transition-colors"
                            >
                                <ImageIcon className="h-6 w-6 text-muted-foreground" />
                                <span className="text-xs text-muted-foreground mt-1">
                                    Add photo
                                </span>
                                <input
                                    id="photo-upload"
                                    type="file"
                                    accept="image/*"
                                    className="hidden"
                                    onChange={handlePhotoUpload}
                                />
                            </label>
                        )}
                    </div>
                </div>

                <DialogFooter>
                    <Button
                        onClick={handleSubmit}
                        disabled={isLoading}
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
