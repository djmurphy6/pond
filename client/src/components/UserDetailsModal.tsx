"use client";

import { useEffect, useState } from "react";
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
import { CreateListingRequest, ErrorResponse, Listing } from "@/api/WebTypes";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "./ui/select";
import { Separator } from "./ui/separator";
import ListingCard from "./ListingCard";
import { useUserInfoStore } from "@/stores/UserInfoStore";

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
    const { userInfo } = useUserInfoStore();

    // Check if viewing own profile
    const isOwnProfile = userInfo?.userGU === userGU;

    useEffect(() => {
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
        fetchListings();
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

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <button onClick={() => { }} className="flex flex-row items-center hover:underline cursor-pointer">
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
                    <span className="px-4 items-center">{username}</span>
                </button>
            </DialogTrigger>

            <DialogContent className="sm:max-w-3xl">
                <div className="flex flex-row items-start gap-6">
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

                <Separator className="" />

                <span className="text-primary">{username}'s Listings</span>

                <div className="grid grid-cols-3 gap-4">
                    {listings.map((listing) => (
                        <ListingCard key={listing.listingGU} item={listing} />
                    ))}
                </div>

            </DialogContent>
        </Dialog>
    );
}
