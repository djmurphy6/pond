"use client"

//Next
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";

//React and Other
import { CSSProperties, useState, useEffect, Suspense } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { InputOTP, InputOTPGroup, InputOTPSlot } from "@/components/ui/input-otp";
import { REGEXP_ONLY_DIGITS } from "input-otp";
import { Card, CardHeader, CardContent, CardFooter } from "@/components/ui/card";
import { Loader2, Mail } from "lucide-react";
import { toast } from "sonner";

// API
import api from "@/api/WebService";

//Internal
import ThemeToggle from "@/components/ThemeToggle";
import { ErrorResponse } from "@/api/WebTypes";


function VerifyContent() {
    const router = useRouter();
    const searchParams = useSearchParams();
    const [isLoading, setIsLoading] = useState(false);
    const [verificationCode, setVerificationCode] = useState("");
    const [email, setEmail] = useState("");

    useEffect(() => {
        // Get email from URL params if it exists
        const emailParam = searchParams.get('email');
        if (emailParam) {
            setEmail(emailParam);
        } else {
            // Redirect to register if no email provided
            toast.error("No email provided. Please register first.");
            router.replace("/register");
        }
    }, [searchParams, router]);

    const handleVerify = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);

        let res = await api.VerifyUser({ email, verificationCode });
        if (res instanceof ErrorResponse) {
            toast.error("Verification Error: " + res.body?.error);
        } else {
            toast.success("Account verified successfully! You can now log in.");
            router.replace("/login");
        }

        setIsLoading(false);
    };

    return (
        <div className="min-h-[100dvh] flex items-center justify-center bg-muted/20">
            <div style={{ position: 'absolute', top: '10px', right: '10px' }}>
                <ThemeToggle />
            </div>
            <Card className="w-full max-w-sm shadow-lg border rounded-2xl">
                <CardHeader>
                    <div className="flex flex-col items-center gap-2">
                        <div className="w-16 h-16 rounded-full bg-[var(--uo-green)]/10 flex items-center justify-center">
                            <Mail className="w-8 h-8 text-[var(--uo-green)]" />
                        </div>
                        <h1 className="text-2xl font-bold text-center">Verify Your Email</h1>
                        <p className="text-sm text-muted-foreground text-center">
                            We've sent a verification code to<br />
                            <span className="font-medium text-foreground">{email}</span>
                        </p>
                    </div>
                </CardHeader>

                <CardContent>
                    <form onSubmit={handleVerify} className="space-y-4">
                        <div>
                            <div className="flex justify-center">
                                <InputOTP
                                    maxLength={6}
                                    value={verificationCode}
                                    onChange={(value) => setVerificationCode(value)}
                                    pattern={REGEXP_ONLY_DIGITS}
                                >
                                    <InputOTPGroup>
                                        <InputOTPSlot index={0} />
                                        <InputOTPSlot index={1} />
                                        <InputOTPSlot index={2} />
                                    </InputOTPGroup>
                                    <InputOTPGroup>
                                        <InputOTPSlot index={3} />
                                        <InputOTPSlot index={4} />
                                        <InputOTPSlot index={5} />
                                    </InputOTPGroup>
                                </InputOTP>
                            </div>
                            <p className="text-xs text-muted-foreground mt-2 text-center">
                                Code expires in 15 minutes
                            </p>
                        </div>

                        <Button
                            style={{ marginTop: 10, color: 'white', cursor: 'pointer' }}
                            type="submit"
                            className="w-full bg-[var(--uo-green)] hover:bg-[var(--uo-green)]/70"
                            disabled={isLoading || verificationCode.length !== 6}
                        >
                            {isLoading ? (
                                <>
                                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                    Verifying...
                                </>
                            ) : (
                                "Verify Account"
                            )}
                        </Button>
                    </form>

                    {/* TODO: Implement resend functionality */}
                    {/* <div className="mt-4 text-center">
                        <button 
                            className="text-sm text-[var(--uo-yellow)] hover:underline"
                            onClick={handleResendCode}
                        >
                            Didn't receive the code? Resend
                        </button>
                    </div> */}
                </CardContent>

                <CardFooter className="text-sm text-center flex-col gap-2">
                    <p className="text-muted-foreground">
                        Already verified?
                    </p>
                    <Link href="/login" className="text-[var(--uo-yellow)] hover:underline">
                        Sign in to your account
                    </Link>
                </CardFooter>
            </Card>
        </div>
    );
}

export default function VerifyPage() {
    return (
        <Suspense fallback={
            <div className="min-h-[100dvh] flex items-center justify-center bg-muted/20">
                <Loader2 className="w-8 h-8 animate-spin" />
            </div>
        }>
            <VerifyContent />
        </Suspense>
    );
}

const styles: Record<string, CSSProperties> = {
    label: {
        paddingBottom: 10
    }
}

