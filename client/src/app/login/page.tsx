"use client"


//Next
import Link from "next/link";
import { useRouter } from "next/navigation";

//React and Other
import { CSSProperties, useState } from "react";
import { Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardHeader, CardContent, CardFooter } from "@/components/ui/card";

//API
import { ErrorResponse } from "@/api/WebTypes";
import api from "@/api/WebService";

//Internal
import ThemeToggle from "@/components/ThemeToggle"


export default function LoginPage() {
    const router = useRouter();
    const [isLoading, setIsLoading] = useState(false);
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);

        console.log(email, password);

        let res = await api.Login({ email, password });

        if (res instanceof ErrorResponse) {
            alert(res.statusText);
        } else {
            console.log(JSON.stringify(res));
            router.push("/dashboard");
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
                    <h1 className="text-2xl font-bold text-center">Welcome Back</h1>
                    <p className="text-sm text-muted-foreground text-center">
                        Log in to your account
                    </p>
                </CardHeader>

                <CardContent>
                    <form onSubmit={handleLogin} className="space-y-4">
                        <div>
                            <Label style={styles.label} htmlFor="email">UO Email</Label>
                            <Input
                                id="email"
                                type="email"
                                placeholder="you@uoregon.edu"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                            />
                        </div>

                        <div>
                            <Label style={styles.label} htmlFor="password">Password</Label>
                            <Input
                                id="password"
                                type="password"
                                placeholder="••••••••"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                            />
                        </div>

                        <Button style={{ marginTop: 10, color: 'white', cursor: 'pointer' }} type="submit" className="w-full bg-[var(--uo-green)] hover:bg-[var(--uo-green)]/70" disabled={isLoading}>
                            {isLoading ? (
                                <>
                                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                    Signing in...
                                </>
                            ) : (
                                "Sign in"
                            )}
                        </Button>
                    </form>
                </CardContent>

                <CardFooter className="flex flex-col gap-2 text-sm text-center">
                    <Link href="/register" className="text-[var(--uo-yellow)] hover:underline">
                        Create an account
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