"use client";

import { useState } from "react";
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
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Loader2, AlertTriangle } from "lucide-react";
import { toast } from "sonner";
import api, { appConfig, SaveAppConfig } from "@/api/WebService";
import { ErrorResponse, DeleteAccountRequest } from "@/api/WebTypes";
import { useRouter } from "next/navigation";
import { useUserInfoStore } from "@/stores/UserInfoStore";

export function DeleteAccountModal({ 
  open, 
  onOpenChange 
}: { 
  open: boolean; 
  onOpenChange: (open: boolean) => void;
}) {
  const [loading, setLoading] = useState(false);
  const [confirmText, setConfirmText] = useState("");
  const router = useRouter();
  const { setUserInfo } = useUserInfoStore();

  const CONFIRM_TEXT = "DELETE";

  async function handleDelete() {
    if (confirmText !== CONFIRM_TEXT) {
      toast.error(`Please type "${CONFIRM_TEXT}" to confirm`);
      return;
    }

    setLoading(true);
    const request: DeleteAccountRequest = {};

    const res = await api.DeleteAccount(request);

    if (res instanceof ErrorResponse) {
      toast.error(res.body?.error || "Failed to delete account");
      setLoading(false);
    } else {
      // Clear user data and tokens
      setUserInfo(undefined);
      appConfig.access_token = undefined;
      SaveAppConfig();
      
      // Clear sessionStorage and localStorage
      sessionStorage.clear();
      localStorage.removeItem("AppConfig");
      
      toast.success("Account deleted successfully");
      
      // Navigate to landing page
      router.push("/");
      onOpenChange(false);
    }
  }

  return (
    <AlertDialog open={open} onOpenChange={!loading ? onOpenChange : undefined}>
      <AlertDialogContent>
        {loading && (
          <div className="absolute inset-0 bg-background/80 backdrop-blur-sm flex items-center justify-center z-50 rounded-lg">
            <div className="flex flex-col items-center gap-3">
              <Loader2 className="h-8 w-8 animate-spin text-destructive" />
              <p className="text-sm font-medium">Deleting account...</p>
              <p className="text-xs text-muted-foreground">This may take a moment</p>
            </div>
          </div>
        )}
        <AlertDialogHeader>
          <AlertDialogTitle className="flex items-center gap-2 text-destructive">
            <AlertTriangle className="h-5 w-5" />
            Delete Account
          </AlertDialogTitle>
          <AlertDialogDescription className="space-y-3 pt-2">
            <p>
              This action cannot be undone. This will permanently delete your account and remove all of your data including:
            </p>
            <ul className="list-disc list-inside space-y-1 text-sm">
              <li>All your listings</li>
              <li>All your messages and chat rooms</li>
              <li>All your saved listings</li>
              <li>All your followers and following relationships</li>
            </ul>
            <p className="pt-2">
              To confirm, please type <strong>{CONFIRM_TEXT}</strong> below:
            </p>
            <div className="pt-2">
              <Label htmlFor="confirm-delete">Confirmation</Label>
              <Input
                id="confirm-delete"
                value={confirmText}
                onChange={(e) => setConfirmText(e.target.value)}
                placeholder={CONFIRM_TEXT}
                disabled={loading}
                className="mt-2"
              />
            </div>
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel 
            onClick={() => setConfirmText("")} 
            disabled={loading}
            className="cursor-pointer"
          >
            Cancel
          </AlertDialogCancel>
          <AlertDialogAction
            onClick={handleDelete}
            disabled={loading || confirmText !== CONFIRM_TEXT}
            className="bg-destructive hover:bg-destructive/70 text-white cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Deleting...
              </>
            ) : (
              "Delete Account"
            )}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}

