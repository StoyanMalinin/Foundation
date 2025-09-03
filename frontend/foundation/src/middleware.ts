import { NextResponse, NextRequest } from 'next/server';
import { FoundationBackend } from "@/backend/foundation-backend";
import { jwtDecode } from "jwt-decode";

export async function middleware(request: NextRequest) {
    const notLoggedInRedirect = request.nextUrl.pathname.startsWith('/dashboard');
    
    const jwtRaw = request.cookies.get("jwt")?.value;
    const jwt = jwtRaw == undefined ? undefined : jwtDecode(jwtRaw);
    const jwtExp = jwt?.exp ?? 0;

    const bufferTimeMs = 1 * 60 * 1000; // 1 minute
    if (!jwt || Date.now() >= jwtExp * 1000 + bufferTimeMs) {
        const refreshToken = request.cookies.get("refresh_token")?.value;
        if (!refreshToken) {
            if (!notLoggedInRedirect) return NextResponse.next();
            return NextResponse.redirect(new URL("/auth/login", request.nextUrl.origin));
        }

        const jwtRefresh = await FoundationBackend.refreshToken(refreshToken);
        if (!jwtRefresh.ok) {
            if (!notLoggedInRedirect) return NextResponse.next();
            return NextResponse.redirect(new URL("/auth/login", request.nextUrl.origin));
        }

        const newCookiesFromBackend = jwtRefresh.headers.getSetCookie();
        const response = NextResponse.redirect(request.nextUrl);
        newCookiesFromBackend.forEach(cookie => {
            response.headers.append('Set-Cookie', cookie);
        });
        
        return response;
    }

    return NextResponse.next();
}

export const config = {
    matcher: [
        '/:path*',
    ],
};