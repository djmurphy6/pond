"use client";

import { useState, useEffect } from "react";
import {
  Popover,
  PopoverTrigger,
  PopoverContent,
} from "@/components/ui/popover";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Camera, ChevronRight, Loader2, ImageIcon, User } from "lucide-react";
import { toast } from "sonner";
import api from "@/api/WebService";
import { ErrorResponse, UpdateUserRequest, UploadAvatarRequest } from "@/api/WebTypes";
import { Separator } from "@/components/ui/separator";
import { useUserInfoStore } from "@/stores/UserInfoStore";


export function MyAccountPopover(props: { onSuccess?: () => void }) {
  const [open, setOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isUploadingAvatar, setIsUploadingAvatar] = useState(false);

  const { userInfo, setUserInfo } = useUserInfoStore();

  const [username, setUsername] = useState("");
  const [bio, setBio] = useState("");
  const [avatarPreview, setAvatarPreview] = useState<string | null>(null);

  // Load current user info when popover opens
  useEffect(() => {
    if (open && userInfo) {
      setUsername(userInfo.username || "");
      setBio(userInfo.bio || "");
      setAvatarPreview(userInfo.avatar_url || null);
    }
  }, [open, userInfo]);

  const handlePhotoUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Validate file type
    if (!file.type.startsWith('image/')) {
      toast.error("Please upload an image file");
      return;
    }

    // Validate file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      toast.error("Image must be less than 5MB");
      return;
    }

    const reader = new FileReader();
    reader.onloadend = async () => {
      const base64String = reader.result as string;
      setAvatarPreview(base64String);

      // Upload immediately
      setIsUploadingAvatar(true);
      const uploadRequest: UploadAvatarRequest = {
        avatar_base64: base64String
      };

      const res = await api.UploadAvatar(uploadRequest);
      setIsUploadingAvatar(false);

      if (res instanceof ErrorResponse) {
        toast.error("Avatar upload failed: " + res.body?.error);
        setAvatarPreview(userInfo?.avatar_url || null); // Revert
      } else {
        toast.success("Avatar uploaded successfully!");
        // Update user info store
        if (userInfo) {
          setUserInfo({ ...userInfo, avatar_url: res.avatar_url });
        }
      }
    };
    reader.readAsDataURL(file);
  };

  const handleSubmit = async () => {
    setIsLoading(true);

    const updateRequest: UpdateUserRequest = {
      username: username !== userInfo?.username ? username : undefined,
      bio: bio !== userInfo?.bio ? bio : undefined,
    };

    // Only send update if something changed
    if (!updateRequest.username && updateRequest.bio === undefined) {
      toast.info("No changes to save");
      setIsLoading(false);
      return;
    }

    const res = await api.UpdateUserProfile(updateRequest);
    setIsLoading(false);

    if (res instanceof ErrorResponse) {
      toast.error("Update failed: " + res.body?.error);
    } else {
      toast.success("Profile updated successfully!");
      // Update user info store
      setUserInfo({
        userGU: res.userGU,
        username: res.username,
        email: res.email,
        avatar_url: res.avatar_url,
        bio: res.bio,
        userScore: userInfo?.userScore,
        admin: res.admin,
      });
      props.onSuccess?.();
      setOpen(false);
    }
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
      <Button
          variant="ghost"
          style={{ cursor: "pointer" }}
          className="!p-0.5 w-full justify-between transition-colors duration-300 mb-3"
        >
          <div className="h-7 w-7 bg-primary/20 rounded-full flex items-center justify-center transition-colors duration-300">
            <ImageIcon className="h-4 w-4 text-primary transition-colors duration-300" />
          </div>
          <span>My Account</span>
          <ChevronRight color="#00000000" className="transition-colors duration-300" />
        </Button>
      </PopoverTrigger>

      {/* ─ Popover content ─ */}
      <PopoverContent
        align="start"
        className="w-80 p-4 space-y-4 rounded-lg shadow-md"
      >

        {/* Avatar Section */}
        <div className="relative mb-6 flex justify-center">
          <div
            className="w-24 h-24 rounded-full bg-muted flex items-center justify-center overflow-hidden relative border-2 border-border"
            style={{
              backgroundImage: avatarPreview ? `url(${avatarPreview})` : undefined,
              backgroundSize: 'cover',
              backgroundPosition: 'center',
            }}
          >
            {!avatarPreview && (
              <span className="text-3xl font-semibold text-muted-foreground">
                {username.charAt(0).toUpperCase() || '?'}
              </span>
            )}
            {isUploadingAvatar && (
              <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
                <Loader2 className="h-6 w-6 animate-spin text-white" />
              </div>
            )}
          </div>

          <input
            id="photo-upload"
            type="file"
            accept="image/*"
            className="hidden"
            onChange={handlePhotoUpload}
            disabled={isUploadingAvatar}
          />

          <Button
            variant="secondary"
            size="icon"
            className="absolute bottom-0 rounded-full border-2"
            disabled={isUploadingAvatar}
            asChild
          >
            <label htmlFor="photo-upload" className="cursor-pointer flex items-center justify-center w-10 h-10">
              <Camera className="h-4 w-4" />
            </label>
          </Button>
        </div>

        <Separator className="my-4 transition-colors duration-300" />

        <div className="space-y-5">
          <div className="space-y-3">
            <Label htmlFor="username">Username</Label>
            <Input
              id="username"
              placeholder="Enter username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={isLoading}
            />
          </div>

          <div className="space-y-3">
            <Label htmlFor="bio">Bio</Label>
            <Textarea
              id="bio"
              placeholder="Write something about yourself..."
              value={bio}
              onChange={(e) => setBio(e.target.value)}
              rows={3}
              disabled={isLoading}
            />
          </div>
        </div>

        <Button
          onClick={handleSubmit}
          className="
            cursor-pointer
            bg-[var(--uo-green)] 
            text-white 
            hover:bg-[color-mix(in_srgb,var(--uo-green)_85%,black)] 
            w-full transition-colors duration-300
          "
        >
          {isLoading ? (
            <>
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              Saving...
            </>
          ) : (
            "Save"
          )}
        </Button>
      </PopoverContent>
    </Popover>
  );
}
