"use cleint";

import { useEffect, useState } from "react";
import { redirect } from "next/navigation";

export default function AuthWrapper({children}: {children: React.ReactNode}) {
    const [isAuthenticated, setIsAuthenticated] = useState<boolean | null>(null);
    useEffect(() => {
        const f = async function() {
            const authCheck = await fetch("https://localhost:6969/check-auth", {
                method: "GET",
                credentials: "include",
            });

            setIsAuthenticated(authCheck.status == 204);
        };

        f();
    }, []);
    useEffect(() => {
        if (isAuthenticated === false) {
            redirect("/auth/login");
        }
    }, [isAuthenticated]);

    if (isAuthenticated === null) return <>Loading...</>;
    else if (isAuthenticated) return <>{children}</>;
    else return <></>
}``