import { NextResponse, NextRequest } from 'next/server';
import { FoundationBackend } from "@/backend/foundation-backend";
import { jwtDecode } from "jwt-decode";

export async function middleware(request: NextRequest) {
    const jwtRaw = request.cookies.get("jwt")?.value;
    if (!jwtRaw) {
        return NextResponse.redirect(new URL("/auth/login", request.url));
    }

    const jwt = jwtDecode(jwtRaw);
    if (jwt.exp && Date.now() >= jwt.exp * 1000 + 1 * 60 * 1000) { // 1 minute buffer time
        const refreshToken = request.cookies.get("refresh_token")?.value || "";
        const jwtRefresh = await FoundationBackend.refreshToken(refreshToken);
        console.log(jwtRefresh.status);

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
        '/dashboard/:path*',
    ],
};