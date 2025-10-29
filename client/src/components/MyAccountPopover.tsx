"use client";

import { useState } from "react";
import {
  Popover,
  PopoverTrigger,
  PopoverContent,
} from "@/components/ui/popover";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Camera, ChevronRight, ImageIcon, Loader2 } from "lucide-react";
import { toast } from "sonner";
import api from "@/api/WebService";
import { CreateListingRequest, ErrorResponse } from "@/api/WebTypes";
import { Separator } from "@/components/ui/separator";


export function MyAccountPopover(props: { onSuccess?: () => void }) {
  const [open, setOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const [photo, setPhoto] = useState<string | null>(null);

  const handlePhotoUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onloadend = () => {
      setPhoto(reader.result as string);
    };
    reader.readAsDataURL(file);
  };

  const handleSubmit = async () => {
    // setIsLoading(true);
    // let res = await api.CreateListing(listing);
    // setIsLoading(false);
    // if (res instanceof ErrorResponse) {
    //   toast.error("Create Listing Error:" + res.body?.error);
    // } else {
    //   toast.success("Successfully created listing");
    //   props.onSuccess?.();
    //   setOpen(false);
    // }
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      {/* ─ Trigger button ─ */}
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
        {/* Photo Button */}
        <div className="flex justify-center">
          <Button
            className="cursor-pointer"
            variant="outline"
            size="icon-lg"
            style={{ width: "100px", height: "100px", borderRadius: "50px" }}
          >
            <label
              htmlFor="photo-upload"
              className="cursor-pointer relative w-[100px] h-[100px] rounded-full border border-input flex items-center justify-center overflow-hidden bg-transparent"
            >

              {photo ? (
                <img
                  src={photo}
                  alt="Profile"
                  className="w-full h-full object-cover"
                />
              ) : (
                <ImageIcon style={{ width: '40px', height: '40px' }} className="text-primary" />
              )}

              <input
                id="photo-upload"
                type="file"
                accept="image/*"
                className="hidden"
                onChange={handlePhotoUpload}
              />
            </label>
          </Button>
        </div>
        <Button
          variant='secondary'
          style={{
            width: 40,
            height: 40,
            opacity: 1,
            borderWidth: 1
          }}
          className="cursor-pointer absolute top-20 right-25 text-primary rounded-full"

        >
          <label
            htmlFor="photo-upload"
            className="cursor-pointer"
          >
            <Camera style={{ width: 25, height: 25 }} />
          </label>
        </Button>

        <Separator className="my-4 transition-colors duration-300" />

        <div className="space-y-5">
          <div className="space-y-3">
            <Label htmlFor="username">Username</Label>
            <Input id="username" placeholder="Enter username" />
          </div>

          <div className="space-y-3">
            <Label htmlFor="bio">Bio</Label>
            <Textarea id="bio" placeholder="Write something..." />
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
