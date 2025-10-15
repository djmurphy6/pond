//Next
import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

export function middleware(request: NextRequest) {
    const token = request.cookies.get("refreshToken")?.value;

    // List of routes that require authentication
    const protectedRoutes = ["/dashboard"];

    const isProtected = protectedRoutes.some((path) =>
        request.nextUrl.pathname.startsWith(path)
    );

    const isLogin = request.nextUrl.pathname.startsWith("/login") && (request.nextUrl.searchParams.get("from") !== "register");

    if (isProtected && !token) {
        const loginUrl = new URL("/login", request.url);
        loginUrl.searchParams.set("from", request.nextUrl.pathname);
        return NextResponse.redirect(loginUrl);
    } else if (isLogin && token) {
        return NextResponse.redirect(new URL("/dashboard", request.url));
    }

    return NextResponse.next();
}

export const config = {
    matcher: ["/((?!api|_next|static|favicon.ico).*)"],
};
