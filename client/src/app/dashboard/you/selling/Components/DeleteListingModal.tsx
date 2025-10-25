//REACT AND OTHER
import {
  AlertDialog,
  AlertDialogContent,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogCancel,
  AlertDialogAction,
} from "@/components/ui/alert-dialog";
import { toast } from "sonner";

//API
import api from "@/api/WebService"
import { ErrorResponse, Listing } from "@/api/WebTypes";

export default function DeleteListingModal({ item, onClose, onDelete }: { item?: Listing, onClose: () => void, onDelete: () => void }) {
  async function handleDelete() {
    if (item) {
        const res = await api.DeleteListing({listingGU: item.listingGU});
        if (res instanceof ErrorResponse) {
            toast.error(res.body?.error);
        } else {
            toast.success("Listing deleted!");
            onDelete();
            onClose();
        }
    }
  }

  return (
    <AlertDialog open={!!item} onOpenChange={onClose}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Delete this listing?</AlertDialogTitle>
          <AlertDialogDescription>
            This action cannot be undone. Your listing will be permanently removed.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel className="cursor-pointer">Cancel</AlertDialogCancel>
          <AlertDialogAction onClick={handleDelete} className="bg-destructive hover:bg-destructive/70 text-white cursor-pointer">
            Delete
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
