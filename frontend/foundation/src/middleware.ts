import { NextResponse, NextRequest } from 'next/server';

export async function middleware(request: NextRequest) {
    const authCheck = await fetch("https://localhost:6969/check-auth", {
        method: "GET",
        credentials: "include",
        headers: {
            "Cookie": request.headers.get("cookie") ?? "",
        }
    });

    if (!authCheck.ok) {
        return NextResponse.redirect(new URL("/auth/login", request.url));
    }
    if (authCheck.status == 204) {
        return NextResponse.next();
    }

    const newCookiesFromBackend = authCheck.headers.getSetCookie();
    return NextResponse.redirect(request.nextUrl, {
        headers: {
            'Set-Cookie': newCookiesFromBackend.join("; ")
        }
    });
}

export const config = {
    matcher: [
        '/dashboard/:path*',
    ],
};