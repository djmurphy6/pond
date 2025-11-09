"use client";

//React
import { useEffect, useState } from "react";

//Shad CN
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
    DialogFooter,
} from "@/components/ui/dialog";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Loader2, AlertTriangle } from "lucide-react";
import { toast } from "sonner";

//API
import api from "@/api/WebService";
import { ErrorResponse, ReportReason } from "@/api/WebTypes";

//Internal
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "./ui/select";
import { Separator } from "./ui/separator";

type ReportModalProps = {
    listingId: string;
    listingTitle: string;
    open?: boolean;
    onOpenChange?: (open: boolean) => void;
}

const REPORT_REASON_LABELS: Record<ReportReason, string> = {
    [ReportReason.INAPPROPRIATE_CONTENT]: "Inappropriate Content",
    [ReportReason.SPAM]: "Spam",
    [ReportReason.FRAUDULENT]: "Fraudulent/Scam",
    [ReportReason.MISLEADING_INFORMATION]: "Misleading Information",
    [ReportReason.PROHIBITED_ITEM]: "Prohibited Item",
    [ReportReason.DUPLICATE_LISTING]: "Duplicate Listing",
    [ReportReason.OFFENSIVE_LANGUAGE]: "Offensive Language",
    [ReportReason.OTHER]: "Other"
};

export function ReportListingModal(props: ReportModalProps) {

    const { listingId, listingTitle, open: controlledOpen, onOpenChange } = props;

    const [internalOpen, setInternalOpen] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [reason, setReason] = useState<string>("");
    const [message, setMessage] = useState("");

    // Use controlled state if provided, otherwise use internal state
    const open = controlledOpen !== undefined ? controlledOpen : internalOpen;
    const setOpen = onOpenChange || setInternalOpen;

    useEffect(() => {
        if (!open) {
            setReason("");
            setMessage("");
        }
    }, [open]);

    async function SubmitReport() {
        if (!reason) {
            toast.error("Please select a reason");
            return;
        }

        setIsLoading(true);
        let res = await api.CreateReport({
            listingGU: listingId,
            reason: reason,
            message: message
        });
        setIsLoading(false);
        
        if (res instanceof ErrorResponse) {
            toast.error(res.body?.error || "Failed to submit report");
        } else {
            toast.success("Report submitted successfully. We'll review it shortly.");
            setOpen(false);
        }
    }

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            {/* Only render trigger button if not controlled externally */}
            {controlledOpen === undefined && (
                <DialogTrigger asChild>
                    <Button variant="outline" className="cursor-pointer">
                        <AlertTriangle className="mr-2 h-4 w-4" /> Report
                    </Button>
                </DialogTrigger>
            )}

            <DialogContent showCloseButton={true} className="sm:max-w-md">
                <DialogHeader>
                    <DialogTitle className="text-center font-bold tracking-wide">Report Listing</DialogTitle>
                </DialogHeader>

                <Separator className="my-2" />

                <div className="mb-2">
                    <Label className="block text-sm font-medium text-muted-foreground mb-1">Listing</Label>
                    <p className="text-base font-medium">{listingTitle}</p>
                </div>

                <div className="flex flex-col space-y-4">
                    <div className="flex flex-col space-y-2">
                        <Label htmlFor="reason">Reason for reporting *</Label>
                        <Select value={reason} onValueChange={setReason}>
                            <SelectTrigger>
                                <SelectValue placeholder="Select a reason" />
                            </SelectTrigger>
                            <SelectContent>
                                {Object.entries(REPORT_REASON_LABELS).map(([key, label]) => (
                                    <SelectItem key={key} value={key}>
                                        {label}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    </div>

                    <div className="flex flex-col space-y-2">
                        <Label htmlFor="message">Additional details (optional)</Label>
                        <Textarea
                            className="resize-none"
                            id="message"
                            value={message}
                            onChange={(e) => setMessage(e.target.value)}
                            placeholder="Provide additional information about this report..."
                            rows={4}
                            maxLength={1000}
                        />
                        <span className="text-xs text-muted-foreground">
                            {message.length}/1000 characters
                        </span>
                    </div>
                </div>

                <DialogFooter className="mt-4">
                    <Button
                        variant="outline"
                        onClick={() => setOpen(false)}
                        className="cursor-pointer tracking-wide flex-1"
                    >
                        Cancel
                    </Button>
                    <Button
                        onClick={SubmitReport}
                        disabled={isLoading || !reason}
                        className={`cursor-pointer flex-1 bg-red-600 hover:bg-red-700 text-white`}
                    >
                        {isLoading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
                        Submit Report
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    )
}

