//REACT and OTHER
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogFooter,
} from "@/components/ui/dialog";
import { toast } from "sonner";
import { useEffect, useState } from "react";
import { Label } from "@radix-ui/react-label";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { ImageIcon, X } from "lucide-react";

//API
import api from "@/api/WebService"
import { ErrorResponse, Listing, UpdateListingRequest } from "@/api/WebTypes";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";


export default function EditListingModal({ item, onClose, onSave }: { item?: Listing, onClose: () => void, onSave: () => void }) {
    const [title, setTitle] = useState(item?.title ?? "");
    const [description, setDescription] = useState(item?.description ?? "");
    const [price, setPrice] = useState(item?.price ?? "");
    const [condition, setCondition] = useState(item?.condition ?? "");
    const [category, setCategory] = useState(item?.category ?? "");
    const [photos, setPhotos] = useState<string[]>([]);

    const [loading, setLoading] = useState(false);


    useEffect(() => {
        if (item) {
            setTitle(item?.title ?? "");
            setDescription(item?.description ?? "");
            setPrice(item?.price ?? "");
            setCondition(item?.condition ?? "");
            setCategory(item?.category ?? "");
            let imgs = [];
            if (!!item.picture1_url) { imgs.push(item.picture1_url) }
            if (!!item.picture2_url) { imgs.push(item.picture2_url) }
            setPhotos(imgs);
        }
    }, [item])

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

    async function handleSave() {
        if (item) {
             // Map photos array to URLs and base64
            const picture1 = photos[0];
            const picture2 = photos[1];
            
            // Determine if photos are URLs or new base64 data
            const isPicture1New = picture1?.startsWith("data:");
            const isPicture2New = picture2?.startsWith("data:");
            const req = {
                listingGU: item.listingGU,
                body: {
                    title,
                    // If photo doesn't exist, send empty string to signal deletion
                    // If it's original URL, keep it; if it's new, backend will use base64
                    picture1_url: !picture1 ? "" : (isPicture1New ? item.picture1_url : picture1),
                    picture2_url: !picture2 ? "" : (isPicture2New ? item.picture2_url : picture2),
                    price: price === "" ? 0 : Number(price),
                    condition,
                    category,
                    description,
                    //add images when put mapping allows for it
                    picture1_base64: photos[0] || "",
                    picture2_base64: photos[1] || "",

                }
            } as UpdateListingRequest;

            setLoading(true);
            const res = await api.UpdateListing(req);
            setLoading(false);
            if (res instanceof ErrorResponse) {
                toast.error(res.body?.error);
            } else {
                toast.success("Listing updated!");
                onSave();
                onClose();
            }
        }
    }

    return (
        <Dialog open={!!item} onOpenChange={onClose}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Edit Listing</DialogTitle>
                </DialogHeader>

                <div className="space-y-4 py-2">
                    <Label htmlFor="title">Title</Label>
                    <Input className="mt-2" id="title" value={title} onChange={(e) => setTitle(e.target.value)} />

                    <Label htmlFor="description">Description</Label>
                    <Textarea className="mt-2" id="description" value={description} onChange={(e) => setDescription(e.target.value)} />

                    <Label htmlFor="price">Price</Label>
                    <Input
                        id="price"
                        type="text"
                        value={price ? `$ ${price}` : ""}
                        className="mt-2"
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

                    <Label htmlFor="condition">Condition</Label>
                    <Select value={condition} onValueChange={setCondition}>
                        <SelectTrigger className="w-full mt-2">
                            <SelectValue placeholder="Condition" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="New">New</SelectItem>
                            <SelectItem value="Used">Used</SelectItem>
                            <SelectItem value="Like New">Like New</SelectItem>
                        </SelectContent>
                    </Select>

                    <Label htmlFor="category">Category</Label>
                    <Select value={category} onValueChange={setCategory}>
                        <SelectTrigger className="w-full mt-2">
                            <SelectValue placeholder="Category" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="Furniture">Furniture</SelectItem>
                            <SelectItem value="Clothing">Clothing</SelectItem>
                            <SelectItem value="Housing">Housing</SelectItem>
                            <SelectItem value="Tech">Tech</SelectItem>
                            <SelectItem value="School">School</SelectItem>
                            <SelectItem value="Vehicles">Vehicles</SelectItem>
                            <SelectItem value="Misc">Misc</SelectItem>
                        </SelectContent>
                    </Select>

                    {/* Photos */}
                    <Label>Photos (max 2)</Label>
                    <div className="flex gap-3 mt-3 flex-wrap">
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
                    <Button style={{ cursor: 'pointer' }} className="bg-[var(--uo-green)] hover:bg-[var(--uo-green)]/70 text-white" onClick={handleSave} disabled={loading}>
                        {loading ? "Saving..." : "Save"}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
