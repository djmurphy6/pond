//Next
import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

export function middleware(request: NextRequest) {
    // NOTE: Cookie-based auth check disabled for cross-domain setup (Vercel frontend + Render backend)
    // The refreshToken cookie from Render backend is domain-specific and not accessible to Vercel frontend
    // Auth checks are now handled client-side in protected pages

    return NextResponse.next();
}

export const config = {
    matcher: ["/((?!api|_next|static|favicon.ico).*)"],
};

// export function middleware(request: NextRequest) {
//     const token = request.cookies.get("refreshToken")?.value;

//     // List of routes that require authentication
//     const protectedRoutes = ["/dashboard", "/dashboard/you/listings"];

//     const isProtected = protectedRoutes.some((path) =>
//         request.nextUrl.pathname.startsWith(path)
//     );

//     const fromRegister = (request.nextUrl.searchParams.get("from") === "register")
//     const unAuthorized = (request.nextUrl.searchParams.get("error") === "unauthorized");

//     const isLogin = request.nextUrl.pathname.startsWith("/login") && !unAuthorized && !fromRegister;

//     if (isProtected && !token) {
//         const loginUrl = new URL("/login", request.url);

//         if (unAuthorized) {
//             loginUrl.searchParams.set("error", "unauthorized");
//         }
//         if (fromRegister) {
//             loginUrl.searchParams.set("from", "register");
//         }

//         return NextResponse.redirect(loginUrl);
//     } else if (isLogin && token) {
//         return NextResponse.redirect(new URL("/dashboard", request.url));
//     }

//     return NextResponse.next();
// }

// export const config = {
//     matcher: ["/((?!api|_next|static|favicon.ico).*)"],
// };
