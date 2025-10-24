//REACT and OTHER
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogFooter,
} from "@/components/ui/dialog";
import { toast } from "sonner";
import { useState } from "react";

//API
import api from "@/api/WebService"
import { ErrorResponse, Listing } from "@/api/WebTypes";

import { Label } from "@radix-ui/react-label";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

export default function EditListingModal({ item, onClose, onSave }: { item?: Listing, onClose: () => void, onSave: () => void }) {
    const [title, setTitle] = useState(item?.title ?? "");
    const [price, setPrice] = useState(item?.price ?? "");
    const [loading, setLoading] = useState(false);

    async function handleSave() {
        // setLoading(true);
        // const res = await api.EditListing(item.listingGU, { title, price });
        // setLoading(false);
        // if (res instanceof ErrorResponse) toast.error(res.body?.error);
        // else {
        //   toast.success("Listing updated!");
        //   onSave();
        //   onClose();
        // }
    }

    return (
        <Dialog open={!!item} onOpenChange={onClose}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Edit Listing</DialogTitle>
                </DialogHeader>

                <div className="space-y-4 py-2">
                    <Label htmlFor="title">Title</Label>
                    <Input id="title" value={title} onChange={(e) => setTitle(e.target.value)} />

                    <Label htmlFor="price">Price</Label>
                    <Input id="price" type="number" value={price} onChange={(e) => setPrice(e.target.value)} />
                </div>

                <DialogFooter>
                    <Button onClick={handleSave} disabled={loading}>
                        {loading ? "Saving..." : "Save"}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
