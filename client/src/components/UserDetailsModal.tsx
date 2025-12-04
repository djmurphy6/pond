"use client";

import { ReactNode, useEffect, useRef, useState } from "react";
import {
    Dialog,
    DialogContent,
    DialogTitle,
    DialogTrigger,
} from "@/components/ui/dialog";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";
import { Loader2, ImageIcon, UserPlus, UserMinus } from "lucide-react";
import { toast } from "sonner";
import api from "@/api/WebService";
import { ErrorResponse, GetUserRatingStatsResponse, Listing, Review } from "@/api/WebTypes";
import { Separator } from "./ui/separator";
import { Skeleton } from "./ui/skeleton";
import { Card, CardContent } from "./ui/card";
import ListingCard from "./ListingCard";
import { useUserInfoStore } from "@/stores/UserInfoStore";
import { ScrollArea } from "./ui/scroll-area";
import StarRating from "./StarRating";
import SellerReviewsCarousel from "./SellerReviewsCarousel";
import EditReviewModal from "./EditReviewModal";
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from "@/components/ui/alert-dialog";

type UserDetailsModalProps = {
    userGU: string;
    username: string,
    avatar_url?: string,
    onSuccess?: () => void,
    children?: ReactNode,
}

export function UserDetailsModal(props: UserDetailsModalProps) {
    const { userGU, username, avatar_url, onSuccess, children } = props;

    const [open, setOpen] = useState(false);

    // isLoading controls the LISTINGS skeleton state only
    const [isLoading, setIsLoading] = useState(false);

    // Separate loading state for reviews actions if needed, or rely on toast
    const [isStatusLoading, setIsStatusLoading] = useState(true);
    const [isFollowLoading, setIsFollowLoading] = useState(false);

    const [isFollowing, setIsFollowing] = useState(false);
    const [followerCount, setFollowerCount] = useState(0);
    const [followingCount, setFollowingCount] = useState(0);

    const [listings, setListings] = useState<Listing[]>([]);
    const [userStats, setUserStats] = useState<GetUserRatingStatsResponse | undefined>();
    const [reviews, setReviews] = useState<Review[]>([]);
    const { userInfo } = useUserInfoStore();

    // Leaving a review
    const [userReviewRating, setUserReviewRating] = useState(0);
    const [isSubmittingReview, setIsSubmittingReview] = useState(false);
    const commentRef = useRef<HTMLTextAreaElement>(null);

    // Edit/Delete State
    const [reviewToEdit, setReviewToEdit] = useState<Review | undefined>();
    const [reviewToDelete, setReviewToDelete] = useState<Review | undefined>();

    const isOwnProfile = userInfo?.userGU === userGU;

    // --- DATA FETCHING ---

    async function fetchListings() {
        setIsLoading(true);
        const response = await api.GetSpecificUserListings({ userGU: userGU });
        setIsLoading(false);
        if (response instanceof ErrorResponse) {
            toast.error(response.body?.error);
        } else {
            setListings(response);
        }
    }

    async function fetchUserStats() {
        // Do NOT set isLoading here to avoid flickering listings
        const response = await api.GetUserRatingStats(userGU);
        if (response instanceof ErrorResponse) {
            console.error("Failed to fetch user stats");
        } else {
            setUserStats(response);
        }
    }

    async function fetchUserReviews() {
        // Do NOT set isLoading here
        const response = await api.GetReviews(userGU);
        if (response instanceof ErrorResponse) {
            console.error("Failed to fetch reviews");
        } else {
            setReviews(response);
        }
    }

    const refreshReviewData = async () => {
        // Run both in parallel and wait for them
        await Promise.all([fetchUserStats(), fetchUserReviews()]);
        // Trigger parent refresh if provided
        if (onSuccess) onSuccess();
    };

    // Initial Load
    useEffect(() => {
        if (open) {
            fetchListings();
            fetchUserStats();
            fetchUserReviews();
        }
    }, [userGU, open]);

    // Following Status
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
            setIsStatusLoading(false);
        }
    }, [open, userGU, isOwnProfile]);

    // Follow Counts
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

    // --- HANDLERS ---

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
                    if (onSuccess) onSuccess();
                }
            } else {
                const response = await api.FollowUser({ userId: userGU });
                if (response instanceof ErrorResponse) {
                    toast.error(response.body?.error || "Failed to follow");
                } else {
                    setIsFollowing(true);
                    setFollowerCount(prev => prev + 1);
                    toast.success(`Now following ${username}`);
                    if (onSuccess) onSuccess();
                }
            }
        } catch (error) {
            toast.error("An error occurred");
        } finally {
            setIsFollowLoading(false);
        }
    };

    const handleSubmitReview = async () => {
        const comment = commentRef.current?.value || "";

        if (userReviewRating === 0 || comment.length === 0) {
            toast.error("Please select a rating and add a comment");
            return;
        }

        setIsSubmittingReview(true);
        const response = await api.CreateReview({
            revieweeGU: userGU,
            rating: userReviewRating,
            comment: comment,
        });
        setIsSubmittingReview(false);

        if (response instanceof ErrorResponse) {
            toast.error(response.body?.error || "Failed to submit review");
        } else {
            toast.success("Review submitted successfully");
            if (commentRef.current) commentRef.current.value = "";
            setUserReviewRating(0);
            refreshReviewData();
        }
    };

    const handleDeleteReview = async () => {
        if (!reviewToDelete) return;

        // Determine deletion endpoint (Admin vs Owner)
        const reviewerID = (reviewToDelete as any).reviewerGu || reviewToDelete.reviewerGU;
        const isOwner = userInfo?.userGU === reviewerID;
        const isAdmin = userInfo?.admin;

        let res;
        if (isAdmin && !isOwner) {
            res = await api.AdminDeleteReview(reviewToDelete.reviewGU);
        } else {
            res = await api.DeleteReview(reviewToDelete.reviewGU);
        }

        if (res instanceof ErrorResponse) {
            toast.error(res.body?.error || "Failed to delete review");
        } else {
            toast.success("Review deleted");
            // Important: Await the refresh before clearing the modal state
            // this ensures the UI doesn't flash old data
            await refreshReviewData();
        }
        setReviewToDelete(undefined);
    };

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                {children ? (
                    children
                ) : (
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
                )}
            </DialogTrigger>

            <DialogContent className="max-w-full sm:max-w-3xl [&::-webkit-scrollbar]:hidden max-h-[90vh] overflow-y-auto">
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

                {reviews.length > 0 && (
                    <>
                        <Separator className="" />
                        <SellerReviewsCarousel
                            reviews={reviews}
                            onEdit={setReviewToEdit}
                            onDelete={setReviewToDelete}
                        />
                    </>
                )}

                {userStats?.canReview && (
                    <>
                        <Separator className="" />
                        <span className="text-xl font-semibold">Leave a review</span>
                        <StarRating size={30} max={5} value={userReviewRating} onChange={setUserReviewRating} />
                        <Textarea
                            ref={commentRef}
                            className="resize-none min-h-[15vh]"
                            id="comment"
                            maxLength={500}
                            placeholder="Type your review comment here..."
                        />
                        <Button
                            onClick={handleSubmitReview}
                            style={{ color: 'white', cursor: 'pointer' }}
                            type="submit"
                            className="w-full bg-[var(--uo-green)] hover:bg-[var(--uo-green)]/70"
                            disabled={isSubmittingReview}
                        >
                            {isSubmittingReview ? (
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
                    <ScrollArea className="max-h-[50vh] w-full">
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                            {Array.from({ length: 6 }).map((_, i) => (
                                <Card key={i} className="transition-colors duration-300">
                                    <Skeleton className="h-40 w-full rounded-t-md transition-colors duration-300" />
                                    <CardContent className="p-3">
                                        <Skeleton className="h-4 w-3/4 mb-2 transition-colors duration-300" />
                                        <Skeleton className="h-4 w-1/2 transition-colors duration-300" />
                                    </CardContent>
                                </Card>
                            ))}
                        </div>
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

                {/* Edit Modal */}
                <EditReviewModal
                    review={reviewToEdit}
                    onClose={() => setReviewToEdit(undefined)}
                    onSave={refreshReviewData}
                />

                {/* Delete Confirmation Alert */}
                <AlertDialog open={!!reviewToDelete} onOpenChange={() => setReviewToDelete(undefined)}>
                    <AlertDialogContent>
                        <AlertDialogHeader>
                            <AlertDialogTitle>Delete Review?</AlertDialogTitle>
                            <AlertDialogDescription>
                                Are you sure you want to delete this review? This action cannot be undone.
                            </AlertDialogDescription>
                        </AlertDialogHeader>
                        <AlertDialogFooter>
                            <AlertDialogCancel className="cursor-pointer">Cancel</AlertDialogCancel>
                            <AlertDialogAction
                                onClick={handleDeleteReview}
                                className="bg-destructive text-white hover:bg-destructive/90 cursor-pointer"
                            >
                                Delete
                            </AlertDialogAction>
                        </AlertDialogFooter>
                    </AlertDialogContent>
                </AlertDialog>

            </DialogContent>
        </Dialog>
    );
}