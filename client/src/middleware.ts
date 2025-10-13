//Next
import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

export function middleware(request: NextRequest) {
    const token = request.cookies.get("accessToken")?.value;

    // List of routes that require authentication
    const protectedRoutes = ["/dashboard"];

    const isProtected = protectedRoutes.some((path) =>
        request.nextUrl.pathname.startsWith(path)
    );

    if (isProtected && !token) {
        const loginUrl = new URL("/login", request.url);
        loginUrl.searchParams.set("from", request.nextUrl.pathname);
        return NextResponse.redirect(loginUrl);
    }

    return NextResponse.next();
}

export const config = {
    matcher: ["/((?!api|_next|static|favicon.ico).*)"],
};
