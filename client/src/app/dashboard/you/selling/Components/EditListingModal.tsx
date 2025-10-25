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

//API
import api from "@/api/WebService"
import { ErrorResponse, Listing, UpdateListingRequest } from "@/api/WebTypes";

import { Label } from "@radix-ui/react-label";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";

export default function EditListingModal({ item, onClose, onSave }: { item?: Listing, onClose: () => void, onSave: () => void }) {
    const [title, setTitle] = useState(item?.title ?? "");
    const [description, setDescription] = useState(item?.description ?? "");
    const [price, setPrice] = useState(item?.price ?? "");
    const [condition, setCondition] = useState(item?.condition ?? "");

    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (item) {
            setTitle(item?.title ?? "")
            setDescription(item?.description ?? "")
            setPrice(item?.price ?? "")
            setCondition(item?.condition ?? "")
        }
    }, [item])

    async function handleSave() {
        if (item) {
            const req = {
                listingGU: item.listingGU,
                body: {
                    title,
                    price,
                    description
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
