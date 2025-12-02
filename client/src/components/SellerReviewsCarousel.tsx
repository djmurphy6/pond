"use client";

import * as React from "react";
import { Card, CardHeader, CardContent, CardFooter } from "@/components/ui/card";
import { ScrollArea, ScrollBar } from "@/components/ui/scroll-area";
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";
import StarRating from "@/components/StarRating";
import { Review } from "@/api/WebTypes";

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
};

export default function SellerReviewsCarousel({
    title = "Seller reviews",
    reviews,
}: Props) {
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

                        return (
                            <Card
                                key={review.reviewGU}
                                className="flex-shrink-0 w-[300px] rounded-2xl border bg-muted/50 shadow-sm"
                            >
                                {/* HEADER */}
                                <CardHeader className="flex flex-row items-center gap-3">
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
                                            {review.reviewerName}
                                        </span>
                                        <span className="text-xs text-muted-foreground">
                                            {formatDate(review.timestamp)}
                                        </span>
                                    </div>
                                </CardHeader>

                                {/* CONTENT */}
                                <CardContent className="space-y-2 pb-0">
                                    <StarRating value={review.rating} readOnly size={18} />

                                    {/* <p className="text-xs text-muted-foreground">
                                        Notable: Communication · Pricing · Item Description
                                    </p> */}

                                    {review.comment && (
                                        <p className="mt-2 text-sm leading-snug">
                                            {review.comment}
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
