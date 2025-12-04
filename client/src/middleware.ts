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
