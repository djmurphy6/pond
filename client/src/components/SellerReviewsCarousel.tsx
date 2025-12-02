"use client";

import * as React from "react";
import { Card, CardHeader, CardContent } from "@/components/ui/card";
import { ScrollArea, ScrollBar } from "@/components/ui/scroll-area";
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";
import StarRating from "@/components/StarRating";
import { Review } from "@/api/WebTypes";
import { useUserInfoStore } from "@/stores/UserInfoStore";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import { MoreHorizontal, Pencil, Trash2 } from "lucide-react";

function formatDate(timestamp: string) {
    const d = new Date(timestamp);
    return d.toLocaleDateString("en-US", {
        month: "long",
        day: "numeric",
        year: "numeric",
    });
}

type Props = {
    title?: string;
    reviews: Review[];
    onEdit?: (review: Review) => void;
    onDelete?: (review: Review) => void;
};

export default function SellerReviewsCarousel({
                                                  title = "Seller reviews",
                                                  reviews,
                                                  onEdit,
                                                  onDelete
                                              }: Props) {
    const { userInfo } = useUserInfoStore();

    return (
        <section className="space-y-3 overflow-hidden">
            <h2 className="text-xl font-semibold">
                {title}{" "}
                <span className="text-xl text-muted-foreground">({reviews.length})</span>
            </h2>

            <ScrollArea className="w-full">
                <div className="flex gap-4 pb-4">
                    {reviews.map((review) => {
                        const initials = review.reviewerName
                            ? review.reviewerName.length > 2
                                ? review.reviewerName[0].toUpperCase()
                                : review.reviewerName.toUpperCase()
                            : "";

                        // FIX: Handle potential case mismatch from backend (reviewerGu vs reviewerGU)
                        const reviewerID = (review as any).reviewerGu || review.reviewerGU;

                        const isOwner = userInfo?.userGU === reviewerID;
                        const isAdmin = userInfo?.admin;
                        const canModify = isOwner || isAdmin;

                        return (
                            <Card
                                key={review.reviewGU}
                                className="flex-shrink-0 w-[300px] rounded-2xl border bg-muted/50 shadow-sm relative group"
                            >
                                {/* HEADER */}
                                <CardHeader className="flex flex-row items-start justify-between gap-3 pb-2">
                                    <div className="flex items-center gap-3">
                                        <Avatar className="h-10 w-10">
                                            {review.reviewerAvatar ? (
                                                <AvatarImage
                                                    src={review.reviewerAvatar}
                                                    alt={review.reviewerName}
                                                />
                                            ) : (
                                                <AvatarFallback>{initials}</AvatarFallback>
                                            )}
                                        </Avatar>

                                        <div className="flex flex-col">
                                            <span className="font-semibold leading-tight">
                                                {review.reviewerName || "User"}
                                            </span>
                                            <span className="text-xs text-muted-foreground">
                                                {formatDate(review.timestamp)}
                                            </span>
                                        </div>
                                    </div>

                                    {/* Edit/Delete Menu */}
                                    {canModify && (
                                        <DropdownMenu>
                                            <DropdownMenuTrigger asChild>
                                                <Button
                                                    variant="ghost"
                                                    size="icon"
                                                    className="h-8 w-8 -mr-2 cursor-pointer opacity-0 group-hover:opacity-100 transition-opacity"
                                                >
                                                    <MoreHorizontal className="h-4 w-4" />
                                                    <span className="sr-only">Open menu</span>
                                                </Button>
                                            </DropdownMenuTrigger>
                                            <DropdownMenuContent align="end">
                                                {isOwner && onEdit && (
                                                    <DropdownMenuItem
                                                        onClick={() => onEdit(review)}
                                                        className="cursor-pointer"
                                                    >
                                                        <Pencil className="mr-2 h-4 w-4" />
                                                        Edit
                                                    </DropdownMenuItem>
                                                )}
                                                {onDelete && (
                                                    <DropdownMenuItem
                                                        onClick={() => onDelete(review)}
                                                        className="cursor-pointer text-destructive focus:text-destructive"
                                                    >
                                                        <Trash2 className="mr-2 h-4 w-4" />
                                                        Delete
                                                    </DropdownMenuItem>
                                                )}
                                            </DropdownMenuContent>
                                        </DropdownMenu>
                                    )}
                                </CardHeader>

                                {/* CONTENT */}
                                <CardContent className="space-y-2">
                                    <StarRating value={review.rating} readOnly size={18} />

                                    {review.comment && (
                                        <p className="mt-2 text-sm leading-snug break-words whitespace-pre-wrap">
                                            {review.comment}
                                        </p>
                                    )}

                                    {/* Show "Edited" label if updated */}
                                    {review.updatedAt && review.updatedAt !== review.timestamp && (
                                        <p className="text-[10px] text-muted-foreground italic text-right">
                                            Edited
                                        </p>
                                    )}
                                </CardContent>
                            </Card>
                        );
                    })}
                </div>

                <ScrollBar orientation="horizontal" />
            </ScrollArea>
        </section>
    );
}