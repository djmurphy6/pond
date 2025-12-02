"use client";

import { useEffect, useRef, useState } from "react";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
    DialogFooter,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Loader2, X, ImageIcon, UserPlus, UserMinus } from "lucide-react";
import { toast } from "sonner";
import api from "@/api/WebService";
import { CreateListingRequest, ErrorResponse, GetUserRatingStatsResponse, Listing, Review } from "@/api/WebTypes";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "./ui/select";
import { Separator } from "./ui/separator";
import { Skeleton } from "./ui/skeleton";
import { Card, CardContent } from "./ui/card";
import ListingCard from "./ListingCard";
import { useUserInfoStore } from "@/stores/UserInfoStore";
import { ScrollArea } from "./ui/scroll-area";
import StarRating from "./StarRating";
import SellerReviewsCarousel from "./SellerReviewsCarousel";

type UserDetailsModalProps = {
    userGU: string;
    username: string,
    avatar_url?: string,
    onSuccess?: () => void
}

export function UserDetailsModal(props: UserDetailsModalProps) {
    const { userGU, username, avatar_url, onSuccess } = props;

    const [open, setOpen] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [isFollowing, setIsFollowing] = useState(false);
    const [isFollowLoading, setIsFollowLoading] = useState(false);
    const [isStatusLoading, setIsStatusLoading] = useState(true);
    const [followerCount, setFollowerCount] = useState(0);
    const [followingCount, setFollowingCount] = useState(0);

    const [listings, setListings] = useState<Listing[]>([]);
    const [userStats, setUserStats] = useState<GetUserRatingStatsResponse | undefined>();
    const [reviews, setReviews] = useState<Review[]>([]);
    const { userInfo } = useUserInfoStore();

    //Leaving a review
    const [userReviewRating, setUserReviewRating] = useState(0);
    // const [comment, setComment] = useState("");
    //for performance, dont want to seperate component just to use state
    const commentRef = useRef<HTMLTextAreaElement>(null);

    // Check if viewing own profile
    const isOwnProfile = userInfo?.userGU === userGU;

    async function fetchListings() {
        setIsLoading(true);
        const response = await api.GetSpecificUserListings({ userGU: userGU });
        setIsLoading(false);
        if (response instanceof ErrorResponse) {
            toast.error(response.body?.error);
            return;
        } else {
            setListings(response);
        }
    }

    async function fetchUserStats() {
        setIsLoading(true);
        const response = await api.GetUserRatingStats(userGU);
        setIsLoading(false);
        if (response instanceof ErrorResponse) {
            toast.error(response.body?.error);
            return;
        } else {
            console.log("USERSTATS:", response);
            setUserStats(response);
        }
    }

    async function fetchUserReviews() {
        setIsLoading(true);
        const response = await api.GetReviews(userGU);
        setIsLoading(false);
        if (response instanceof ErrorResponse) {
            toast.error(response.body?.error);
            return;
        } else {
            setReviews(response);
        }
    }

    useEffect(() => {
        fetchListings();
        fetchUserStats();
        fetchUserReviews();
    }, [userGU]);

    // Fetch following status and counts when modal opens
    useEffect(() => {
        if (open && !isOwnProfile) {
            setIsStatusLoading(true);
            async function fetchFollowingStatus() {
                const response = await api.CheckFollowingStatus({ userId: userGU });
                if (!(response instanceof ErrorResponse)) {
                    setIsFollowing(response.following);
                }
                setIsStatusLoading(false);
            }
            fetchFollowingStatus();
        } else if (open && isOwnProfile) {
            // If viewing own profile, no need to check following status
            setIsStatusLoading(false);
        }
    }, [open, userGU, isOwnProfile]);

    useEffect(() => {
        if (open) {
            async function fetchFollowCounts() {
                const response = await api.GetFollowCounts({ userId: userGU });
                if (!(response instanceof ErrorResponse)) {
                    setFollowerCount(response.followers);
                    setFollowingCount(response.following);
                }
            }
            fetchFollowCounts();
        }
    }, [open, userGU]);

    const handleFollowToggle = async () => {
        setIsFollowLoading(true);
        try {
            if (isFollowing) {
                const response = await api.UnfollowUser({ userId: userGU });
                if (response instanceof ErrorResponse) {
                    toast.error(response.body?.error || "Failed to unfollow");
                } else {
                    setIsFollowing(false);
                    setFollowerCount(prev => Math.max(0, prev - 1));
                    toast.success(`Unfollowed ${username}`);
                }
            } else {
                const response = await api.FollowUser({ userId: userGU });
                if (response instanceof ErrorResponse) {
                    toast.error(response.body?.error || "Failed to follow");
                } else {
                    setIsFollowing(true);
                    setFollowerCount(prev => prev + 1);
                    toast.success(`Now following ${username}`);
                }
            }
        } catch (error) {
            toast.error("An error occurred");
        } finally {
            setIsFollowLoading(false);
        }
    };

    const handleSubmit = async () => {
        const comment = commentRef.current?.value || "";

        if (userReviewRating === 0 || comment.length === 0) {
            toast.error("Please select a rating and add a comment");
            return;
        }

        setIsLoading(true);
        const response = await api.CreateReview({
            revieweeGU: userGU,
            rating: userReviewRating,
            comment: comment,
        });
        setIsLoading(false);
        if (response instanceof ErrorResponse) {
            toast.error(response.body?.error || "Failed to submit review");
        } else {
            toast.success("Review submitted successfully");
            fetchUserReviews();
        }
    };

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <button onClick={() => { }} className="flex flex-row w-full items-center hover:underline cursor-pointer">
                    <div className="h-10 w-10 bg-primary/20 rounded-full flex items-center justify-center transition-colors duration-300">
                        {avatar_url ? (
                            <img
                                src={avatar_url}
                                alt="Profile"
                                className="w-full h-full object-cover rounded-full"
                            />
                        ) : (
                            <ImageIcon className="h-4 w-4 text-primary transition-colors duration-300" />
                        )}
                    </div>
                    <div className="flex flex-col">
                        <span className="px-4 items-center">{username}</span>
                        {(userStats && userStats?.totalReviews > 0) && (<StarRating className="px-4 items-center pointer-events-none" value={userStats?.averageRating || 0} readOnly size={15} />)}
                    </div>
                </button>
            </DialogTrigger>

            <DialogContent className="max-w-full sm:max-w-3xl max-h-screen overflow-y-auto">
                <div className="flex flex-col md:flex-row items-center gap-6">
                    <div className="w-32 h-32 rounded-full overflow-hidden flex items-center justify-center border-3 border-primary flex-shrink-0">
                        {avatar_url ? (
                            <img
                                src={avatar_url}
                                alt="Profile"
                                className="w-full h-full object-cover"
                            />
                        ) : (
                            <ImageIcon style={{ width: '40px', height: '40px' }} className="text-primary" />
                        )}
                    </div>
                    <div className="flex-1">
                        <DialogTitle className="text-3xl font-bold mb-2">{username}'s Profile</DialogTitle>
                        <div className="flex items-center gap-4 mb-3">
                            <span className="text-muted-foreground">
                                <span className="font-bold text-primary">{listings.length}</span> active listing{listings.length === 1 ? '' : 's'}
                            </span>
                            <span className="text-muted-foreground">
                                <span className="font-bold text-primary">{followerCount}</span> follower{followerCount === 1 ? '' : 's'}
                            </span>
                            <span className="text-muted-foreground">
                                <span className="font-bold text-primary">{followingCount}</span> following
                            </span>
                        </div>
                        {!isOwnProfile && (
                            <Button
                                onClick={handleFollowToggle}
                                disabled={isFollowLoading || isStatusLoading}
                                variant={isFollowing ? "outline" : "default"}
                                size="sm"
                                className="gap-2"
                            >
                                {isStatusLoading || isFollowLoading ? (
                                    <Loader2 className="h-4 w-4 animate-spin" />
                                ) : isFollowing ? (
                                    <>
                                        <UserMinus className="h-4 w-4" />
                                        Unfollow
                                    </>
                                ) : (
                                    <>
                                        <UserPlus className="h-4 w-4" />
                                        Follow
                                    </>
                                )}
                            </Button>
                        )}
                    </div>
                </div>

                {(userStats && userStats?.totalReviews > 0) && (
                    <>
                        <Separator className="" />
                        <DialogTitle className="text-xl">Seller ratings</DialogTitle>
                        <div className="flex flex-col items-start gap-2">
                            <StarRating className="-mt-2" value={userStats.averageRating ?? 0} readOnly max={5} size={35} />
                            <span className="text-muted-foreground">Based on {userStats.totalReviews} ratings</span>
                        </div>
                    </>
                )}

                {reviews.length > 0 && ( //reviews.length > 0
                    <>
                        <Separator className="" />
                        <SellerReviewsCarousel
                            reviews={reviews}
                        // reviews={[
                        //     {
                        //         reviewGU: "rev-001",
                        //         reviewerGU: "user-101",
                        //         revieweeGU: "seller-001",
                        //         rating: 5,
                        //         comment: "Fantastic experience. Super friendly and very responsive.",
                        //         timestamp: "2025-11-05T14:23:00Z",
                        //         updatedAt: "2025-11-05T14:23:00Z",
                        //         reviewerName: "CJ Martinez",
                        //         reviewerAvatar: "https://i.pravatar.cc/150?img=32",
                        //     },
                        //     {
                        //         reviewGU: "rev-002",
                        //         reviewerGU: "user-102",
                        //         revieweeGU: "seller-001",
                        //         rating: 4.8,
                        //         comment:
                        //             "Great seller! Item exactly as described. Would absolutely recommend.",
                        //         timestamp: "2025-11-04T11:10:00Z",
                        //         updatedAt: "2025-11-04T11:10:00Z",
                        //         reviewerName: "Brad Thompson",
                        //         reviewerAvatar: "https://i.pravatar.cc/150?img=15",
                        //     },
                        //     {
                        //         reviewGU: "rev-003",
                        //         reviewerGU: "user-103",
                        //         revieweeGU: "seller-001",
                        //         rating: 4.6,
                        //         comment: "Smooth transaction and quick communication.",
                        //         timestamp: "2025-11-02T09:45:00Z",
                        //         updatedAt: "2025-11-02T09:45:00Z",
                        //         reviewerName: "Ashley Peterson",
                        //         reviewerAvatar: "https://i.pravatar.cc/150?img=47",
                        //     },
                        //     {
                        //         reviewGU: "rev-004",
                        //         reviewerGU: "user-104",
                        //         revieweeGU: "seller-001",
                        //         rating: 5,
                        //         comment:
                        //             "Couldn't have gone better. Seller was on time and super easy to work with!",
                        //         timestamp: "2025-11-01T16:05:00Z",
                        //         updatedAt: "2025-11-01T16:05:00Z",
                        //         reviewerName: "Michael Chen",
                        //         reviewerAvatar: "https://i.pravatar.cc/150?img=8",
                        //     },
                        //     {
                        //         reviewGU: "rev-005",
                        //         reviewerGU: "user-105",
                        //         revieweeGU: "seller-001",
                        //         rating: 4.2,
                        //         comment: "Item was good overall. Small scuff but still worth the price.",
                        //         timestamp: "2025-10-29T13:15:00Z",
                        //         updatedAt: "2025-10-29T13:15:00Z",
                        //         reviewerName: "Jessica Lee",
                        //         reviewerAvatar: "https://i.pravatar.cc/150?img=28",
                        //     },
                        //     {
                        //         reviewGU: "rev-006",
                        //         reviewerGU: "user-106",
                        //         revieweeGU: "seller-001",
                        //         rating: 3.9,
                        //         comment: "Communication could have been faster but still a fair deal.",
                        //         timestamp: "2025-10-27T18:40:00Z",
                        //         updatedAt: "2025-10-27T18:40:00Z",
                        //         reviewerName: "Daniel Reyes",
                        //         reviewerAvatar: "https://i.pravatar.cc/150?img=52",
                        //     },
                        // ]}
                        />
                    </>
                )}

                {userStats?.canReview && ( //userStats?.canReview
                    <>
                        <Separator className="" />
                        <span className="text-xl font-semibold">Leave a review</span>
                        <StarRating size={30} max={5} value={userReviewRating} onChange={setUserReviewRating} />
                        <Textarea
                            ref={commentRef}
                            className="resize-none min-h-[15vh]"
                            id="comment"
                            maxLength={500}
                            onChange={(e) => {
                                console.log(commentRef.current?.value)
                            }}
                            placeholder="Type your review comment here..."
                        />
                        <Button onClick={handleSubmit} style={{ color: 'white', cursor: 'pointer' }} type="submit" className="w-full bg-[var(--uo-green)] hover:bg-[var(--uo-green)]/70" disabled={isLoading}>
                            {isLoading ? (
                                <>
                                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                    Submitting...
                                </>
                            ) : (
                                "Submit"
                            )}
                        </Button>
                    </>
                )}


                <Separator className="" />

                <span className="text-primary">{username}'s Listings</span>

                {isLoading ? (
                    <ScrollArea className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        {Array.from({ length: 6 }).map((_, i) => (
                            <Card key={i} className="transition-colors duration-300">
                                <Skeleton className="h-40 w-full rounded-t-md transition-colors duration-300" />
                                <CardContent className="p-3">
                                    <Skeleton className="h-4 w-3/4 mb-2 transition-colors duration-300" />
                                    <Skeleton className="h-4 w-1/2 transition-colors duration-300" />
                                </CardContent>
                            </Card>
                        ))}
                    </ScrollArea>
                ) : listings.length === 0 ? (
                    <div className="flex flex-col items-center justify-center h-32 text-muted-foreground">
                        <ImageIcon className="h-12 w-12 mb-2" />
                        <p>No active listings</p>
                    </div>
                ) : (
                    <ScrollArea className="max-h-[50vh] w-full">
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                            {listings.map((listing) => (
                                <ListingCard key={listing.listingGU} item={listing} />
                            ))}
                        </div>
                    </ScrollArea>
                )}

            </DialogContent>
        </Dialog>
    );
}
