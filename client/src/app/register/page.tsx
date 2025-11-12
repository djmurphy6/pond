"use client"

//Next
import Link from "next/link";
import { useRouter } from "next/navigation";

//React and Other
import { CSSProperties, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardHeader, CardContent, CardFooter } from "@/components/ui/card";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";

// API
import api from "@/api/WebService";


//Internal
import ThemeToggle from "@/components/ThemeToggle";
import { ErrorResponse } from "@/api/WebTypes";


export default function Register() {
    const router = useRouter();
    const [isLoading, setIsLoading] = useState(false);
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const handleRegister = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);

        let res = await api.Register({ email, password, username: name, });
        if (res instanceof ErrorResponse) {
            toast.error("Account Creation Error: " + res.body?.error);
        } else {
            console.log(JSON.stringify(res))
            toast.success("Account created! Please check your email for the verification code.")
            router.replace(`/verify?email=${encodeURIComponent(email)}`);
        }

        setIsLoading(false);
    };

    return (
        <div className="min-h-[100dvh] flex items-center justify-center bg-background">
            <div style={{ position: 'absolute', top: '10px', right: '10px' }}>
                <ThemeToggle />
            </div>
            <Card className="w-full max-w-sm shadow-lg border rounded-2xl">
                <CardHeader>
                    <h1 className="text-2xl font-bold text-center">Create Account</h1>
                    <p className="text-sm text-muted-foreground text-center">
                        Sign up with your uoregon.edu email
                    </p>
                </CardHeader>

                <CardContent>
                    <form onSubmit={handleRegister} className="space-y-4">
                        <div>
                            <Label style={styles.label} htmlFor="name">Full Name</Label>
                            <Input
                                id="name"
                                type="text"
                                placeholder="Your Name"
                                required
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                            />
                        </div>

                        <div>
                            <Label style={styles.label} htmlFor="email">UO Email</Label>
                            <Input
                                id="email"
                                type="email"
                                placeholder="you@uoregon.edu"
                                required
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                            />
                        </div>

                        <div>
                            <Label style={styles.label} htmlFor="password">Password</Label>
                            <Input
                                id="password"
                                type="password"
                                placeholder="••••••••"
                                required
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                            />
                        </div>

                        <Button style={{ marginTop: 10, color: 'white', cursor: 'pointer' }} type="submit" className="w-full bg-[var(--uo-green)] hover:bg-[var(--uo-green)]/70" disabled={isLoading}>
                            {isLoading ? (
                                <>
                                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                    Creating account...
                                </>
                            ) : (
                                "Create account"
                            )}
                        </Button>
                    </form>
                </CardContent>

                <CardFooter className="text-sm text-center">
                    Already have an account?
                    <Link style={{ paddingLeft: 5 }} replace href="/login?from=register" className="text-[var(--uo-yellow)]  hover:underline">
                        Sign in
                    </Link>
                </CardFooter>
            </Card>
        </div>
    );
}

const styles: Record<string, CSSProperties> = {
    label: {
        paddingBottom: 10
    }
}
