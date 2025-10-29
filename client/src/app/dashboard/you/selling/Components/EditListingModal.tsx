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


export default function EditListingModal({ item, onClose, onSave }: { item?: Listing, onClose: () => void, onSave: () => void }) {
    const [title, setTitle] = useState(item?.title ?? "");
    const [description, setDescription] = useState(item?.description ?? "");
    const [price, setPrice] = useState(item?.price ?? "");
    const [condition, setCondition] = useState(item?.condition ?? "");
    const [photos, setPhotos] = useState<string[]>([]);

    const [loading, setLoading] = useState(false);


    useEffect(() => {
        if (item) {
            setTitle(item?.title ?? "");
            setDescription(item?.description ?? "");
            setPrice(item?.price ?? "");
            setCondition(item?.condition ?? "");

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
            const req = {
                listingGU: item.listingGU,
                body: {
                    title,
                    price,
                    description
                    //add images when put mapping allows for it
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
                        value={price ? `$ ${Number(price).toLocaleString()}` : ""}
                        onChange={(e) => {
                            const raw = e.target.value.replace(/[^0-9.]/g, "");
                            setPrice(raw ? Number(raw) : "");
                        }}
                        placeholder="$ 0"
                        className="col-span-3 mt-2"
                    />

                    <Label htmlFor="condition">Condition</Label>
                    <Input className="mt-2" id="condition" value={condition} onChange={(e) => setCondition(e.target.value)} />

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
