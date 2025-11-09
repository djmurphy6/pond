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
import { Loader2, X, ImageIcon } from "lucide-react";
import { toast } from "sonner";
import api from "@/api/WebService";
import { CreateListingRequest, ErrorResponse } from "@/api/WebTypes";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "./ui/select";

type UserDetailsModalProps = {
    username: string,
    avatar_url?: string,
    onSuccess?: () => void
}

export function UserDetailsModal(props: UserDetailsModalProps) {
    const { username, avatar_url, onSuccess } = props;

    const [open, setOpen] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

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

                <div className="space-y-4 py-2">

                </div>
            </DialogContent>
        </Dialog>
    );
}
