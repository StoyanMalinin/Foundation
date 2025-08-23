import { NextResponse, NextRequest } from 'next/server';
import { FoundationBackend } from "@/backend/foundation-backend";

export async function middleware(request: NextRequest) {
    const authCheck = await FoundationBackend.checkAuth(request.headers.get("cookie") ?? "");

    if (!authCheck.ok) {
        return NextResponse.redirect(new URL("/auth/login", request.url));
    }
    if (authCheck.status == 204) {
        return NextResponse.next();
    }

    const newCookiesFromBackend = authCheck.headers.getSetCookie();
    const response = NextResponse.redirect(request.nextUrl);
    newCookiesFromBackend.forEach(cookie => {
        response.headers.append('Set-Cookie', cookie);
    });
    
    return response;
}

export const config = {
    matcher: [
        '/dashboard/:path*',
    ],
};