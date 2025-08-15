"use client";

import { useEffect, useState } from "react";
import { JWTManager } from "./jwt/jwt-manager";
import { redirect } from "next/navigation";

export default function AuthWrapper({ children }: { children: React.ReactNode }) {
    const [isAuthenticated, setIsAuthenticated] = useState<boolean | null>(null);
    useEffect(() => {
        const f = async function() {
            const token = await JWTManager.getTokenRaw();
            console.log("Token:", token);
            setIsAuthenticated(token != null);
        };

        f();
    }, []);
    // useEffect(() => {
    //     if (isAuthenticated === false) {
    //         redirect("/auth/login");
    //     }
    // }, [isAuthenticated]);

    if (isAuthenticated === null) return <>Loading...</>;
    else if (isAuthenticated) return <>{children}</>;
    else return <></>
}