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
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Loader2, X, ImageIcon, MessageCircle } from "lucide-react";
import { toast } from "sonner";

//API
import api from "@/api/WebService";
import { ErrorResponse } from "@/api/WebTypes";

//Internal
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "./ui/select";
import { Separator } from "./ui/separator";
import { useUserInfoStore } from "@/stores/UserInfoStore";

type MessageModalProps = {
    onSuccess?: (roomGU?: string, message?: string) => void,
    listingId: string,
    username: string,
    image: string,
    title: string,
    price: number
}

export function MessageSellerModal(props: MessageModalProps) {

    const { onSuccess, listingId, username, image, title, price } = props;

    const [open, setOpen] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [message, setMessage] = useState("");
    const { userInfo } = useUserInfoStore();

    useEffect(() => {
        if (!open) {
            setMessage("");
        }
    }, [open]);

    async function SendMessage() {
        setIsLoading(true);
        let res = await api.InitChatRoom({ listingGU: listingId, buyerGU: userInfo?.userGU || "", });
        setIsLoading(false);
        if (res instanceof ErrorResponse) {
            toast.error(res.body?.error);
        } else {
            toast.success("Chat room created successfully");
            setOpen(false);
            onSuccess?.(res.roomId, message);
        }
    }

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button className="cursor-pointer flex-1 text-white bg-[var(--uo-green)] hover:bg-[var(--uo-green)]/70">
                    <MessageCircle className="mr-2 h-4 w-4" /> Message
                </Button>
            </DialogTrigger>

            <DialogContent showCloseButton={true} className="sm:max-w-md">
                <DialogHeader>
                    <DialogTitle className="text-center font-bold tracking-wide">Message {username}</DialogTitle>
                </DialogHeader>

                <Separator className="my-2" />

                <div className="mb-2 flex flex-row items-center">
                    <div className="h-15 w-15 bg-primary/20 rounded-lg flex items-center justify-center transition-colors duration-300">
                        {image ? (
                            <img
                                src={image}
                                alt="Profile"
                                className="w-full h-full object-cover rounded-lg"
                            />
                        ) : (
                            <ImageIcon className="h-4 w-4 text-primary transition-colors duration-300" />
                        )}
                    </div>
                    <div>
                        <Label className="pl-5 block text-lg font-medium text-foreground">{title}</Label>
                        <span className="pl-5 block text-muted-foreground">$ {price.toLocaleString()}</span>
                    </div>
                </div>

                <div className="flex flex-col space-y-2">
                    <Textarea
                        className="resize-none"
                        id="message"
                        value={message}
                        onChange={(e) => setMessage(e.target.value)}
                        placeholder="Type your message to the seller here..."
                    />
                    <span className="text-xs text-muted-foreground">Don't share your email, phone number or financial information.</span>
                </div>

                <DialogFooter className="mt-2">
                    <Button
                        variant="default"
                        onClick={() => setOpen(false)}
                        className="cursor-pointer tracking-wide flex-1 text-[var(--uo-link)] bg-[none] hover:bg-[none] hover:text-[var(--uo-link)]/70"
                    >
                        Cancel
                    </Button>
                    <Button
                        onClick={SendMessage}
                        disabled={isLoading || !message}
                        className={`cursor-pointer flex-1 text-white bg-[var(--uo-green)] hover:bg-[var(--uo-green)]/70`}
                    >
                        {isLoading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
                        Send Message
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    )
}