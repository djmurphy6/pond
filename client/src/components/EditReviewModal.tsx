"use client";

import { useEffect, useState } from "react";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";
import api from "@/api/WebService";
import { ErrorResponse, Review } from "@/api/WebTypes";
import StarRating from "./StarRating";

interface EditReviewModalProps {
    review?: Review;
    onClose: () => void;
    onSave: () => void;
}

export default function EditReviewModal({ review, onClose, onSave }: EditReviewModalProps) {
    const [rating, setRating] = useState(0);
    const [comment, setComment] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    useEffect(() => {
        if (review) {
            setRating(review.rating);
            setComment(review.comment);
        }
    }, [review]);

    const handleSave = async () => {
        if (!review) return;
        if (rating === 0 || comment.trim().length === 0) {
            toast.error("Please provide a rating and a comment");
            return;
        }

        setIsLoading(true);
        // The API wrapper handles putting reviewGU in the URL,
        // while the body contains the fields to update.
        const res = await api.UpdateReview({
            reviewGU: review.reviewGU,
            rating: rating,
            comment: comment
        });
        setIsLoading(false);

        if (res instanceof ErrorResponse) {
            toast.error(res.body?.error || "Failed to update review");
        } else {
            toast.success("Review updated successfully");
            onSave(); // Trigger parent refresh (UserDetailsModal)
            onClose();
        }
    };

    return (
        <Dialog open={!!review} onOpenChange={(open) => !open && onClose()}>
            <DialogContent className="sm:max-w-md">
                <DialogHeader>
                    <DialogTitle>Edit Review</DialogTitle>
                </DialogHeader>

                <div className="space-y-4 py-4">
                    <div className="flex flex-col gap-2">
                        <span className="text-sm font-medium">Rating</span>
                        <StarRating
                            value={rating}
                            onChange={setRating}
                            size={30}
                            max={5}
                        />
                    </div>

                    <div className="flex flex-col gap-2">
                        <span className="text-sm font-medium">Comment</span>
                        <Textarea
                            value={comment}
                            onChange={(e) => setComment(e.target.value)}
                            placeholder="Update your experience..."
                            className="resize-none min-h-[100px]"
                            maxLength={500}
                        />
                        <span className="text-xs text-muted-foreground text-right">
                            {comment.length}/500
                        </span>
                    </div>
                </div>

                <DialogFooter>
                    <Button
                        variant="outline"
                        onClick={onClose}
                        className="cursor-pointer"
                        disabled={isLoading}
                    >
                        Cancel
                    </Button>
                    <Button
                        onClick={handleSave}
                        disabled={isLoading}
                        className="bg-[var(--uo-green)] hover:bg-[var(--uo-green)]/70 text-white cursor-pointer"
                    >
                        {isLoading ? (
                            <>
                                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                Saving...
                            </>
                        ) : (
                            "Save Changes"
                        )}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}